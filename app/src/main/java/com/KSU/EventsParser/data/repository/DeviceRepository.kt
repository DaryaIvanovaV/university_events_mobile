package com.KSU.EventsParser.data.repository

import android.util.Log
import com.KSU.EventsParser.core.config.AppConfig
import com.KSU.EventsParser.data.local.NotificationPreferences
import com.KSU.EventsParser.data.remote.EventsApi
import com.KSU.EventsParser.data.remote.dto.DeviceRegistrationDto
import com.KSU.EventsParser.domain.model.DataResult
import com.google.android.gms.tasks.Task
import com.google.firebase.messaging.FirebaseMessaging
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.coroutines.withContext
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException

private const val TAG = "DeviceRepository"

/**
 * Підписка пристрою на push.
 *
 * Підписка виконується ДВІЧІ і це не дублювання:
 *
 *  1. `POST /devices/register` — штатний шлях за контрактом. Наразі на сервері
 *     він фактично нічого не робить: FCM_CREDENTIALS_PATH порожній, тож
 *     firebase-admin не ініціалізований і виклик лише пише попередження в лог.
 *  2. `FirebaseMessaging.subscribeToTopic()` — підписка з боку клієнта. Їй
 *     потрібен лише google-services.json на пристрої, і вона працює вже зараз.
 *
 * Тому «push не приходить» саме зараз — це очікувано й не є вадою клієнта.
 * Перевіряти шлях доставки треба тестовим повідомленням із Firebase Console.
 */
class DeviceRepository(
    private val api: EventsApi,
    private val preferences: NotificationPreferences,
    private val messaging: FirebaseMessaging = FirebaseMessaging.getInstance(),
) {

    val enabled = preferences.enabled

    suspend fun isEnabled(): Boolean = preferences.isEnabled()

    /**
     * Поточний токен пристрою. `null`, якщо Firebase не зміг його видати.
     *
     * getToken() у FCM 25.x позначено застарілим на користь пари
     * `register()` + `FirebaseMessagingService.onRegistered()`. Але `register()`
     * повертає `Task<Void>`, а токен віддає лише через колбек сервісу, і той
     * НЕ спрацьовує, якщо токен для цього пристрою вже випущено. Нам же токен
     * потрібен просто зараз — щоб надіслати його в /devices/register та
     * /devices/unregister. Тому метод лишається, з явним придушенням
     * попередження; ротацію токена ловить onRegistered у сервісі.
     */
    @Suppress("DEPRECATION")
    suspend fun currentToken(): String? = withContext(Dispatchers.IO) {
        runCatching { messaging.token.await() }
            .onFailure { Log.w(TAG, "Не вдалося отримати FCM-токен", it) }
            .getOrNull()
    }

    /**
     * Вмикає сповіщення: підписка на тему з боку клієнта + реєстрація на сервері.
     *
     * Помилка серверної реєстрації НЕ скасовує вибір користувача: клієнтська
     * підписка вже діє, а сервер можна повідомити пізніше.
     */
    suspend fun enableNotifications(): DataResult<Unit> {
        preferences.setEnabled(true)
        subscribeToTopic()

        val token = currentToken() ?: return DataResult.Success(Unit)
        return register(token)
    }

    /** Вимикає сповіщення: відписка з боку клієнта + POST /devices/unregister. */
    suspend fun disableNotifications(): DataResult<Unit> {
        preferences.setEnabled(false)
        unsubscribeFromTopic()

        val token = currentToken() ?: return DataResult.Success(Unit)
        return unregister(token)
    }

    /**
     * Реєструє токен на сервері.
     *
     * Викликати не лише при першому запуску, а й при КОЖНІЙ ротації токена
     * (`onNewToken`): старий токен перестає працювати мовчки.
     */
    suspend fun register(token: String): DataResult<Unit> = withContext(Dispatchers.IO) {
        runCatchingApi {
            val response = api.registerDevice(
                DeviceRegistrationDto(fcmToken = token, topics = listOf(AppConfig.fcmTopic)),
            )
            Log.d(TAG, "register: status=${response.status} topics=${response.topics}")
            Unit
        }
    }

    suspend fun unregister(token: String): DataResult<Unit> = withContext(Dispatchers.IO) {
        runCatchingApi {
            val response = api.unregisterDevice(
                DeviceRegistrationDto(fcmToken = token, topics = listOf(AppConfig.fcmTopic)),
            )
            Log.d(TAG, "unregister: status=${response.status} topics=${response.topics}")
            Unit
        }
    }

    private suspend fun subscribeToTopic() {
        runCatching { messaging.subscribeToTopic(AppConfig.fcmTopic).await() }
            .onSuccess { Log.d(TAG, "Підписано на тему ${AppConfig.fcmTopic}") }
            .onFailure { Log.w(TAG, "Не вдалося підписатися на тему", it) }
    }

    private suspend fun unsubscribeFromTopic() {
        runCatching { messaging.unsubscribeFromTopic(AppConfig.fcmTopic).await() }
            .onSuccess { Log.d(TAG, "Відписано від теми ${AppConfig.fcmTopic}") }
            .onFailure { Log.w(TAG, "Не вдалося відписатися від теми", it) }
    }
}

/**
 * Перетворює Task із Google Play Services на suspend-виклик.
 * Написано вручну, щоб не тягнути заради одного місця
 * kotlinx-coroutines-play-services.
 */
private suspend fun <T> Task<T>.await(): T = suspendCancellableCoroutine { continuation ->
    addOnSuccessListener { result -> continuation.resume(result) }
    addOnFailureListener { error -> continuation.resumeWithException(error) }
    addOnCanceledListener { continuation.cancel() }
}
