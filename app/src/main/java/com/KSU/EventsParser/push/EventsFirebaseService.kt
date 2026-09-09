package com.KSU.EventsParser.push

import android.util.Log
import com.KSU.EventsParser.EventsApp
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch

private const val TAG = "EventsFcm"

class EventsFirebaseService : FirebaseMessagingService() {

    // Служба живе недовго, тож область власна й скасовується разом із нею.
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    /**
     * Токен ротується сам по собі (перевстановлення, очищення даних, відновлення
     * з бекапу). Старий токен після цього перестає працювати МОВЧКИ, тому
     * реєструвати новий треба саме тут, а не лише при першому запуску.
     *
     * У FCM 25.x цей колбек називається onRegistered — він замінив застарілий
     * onNewToken; перевизначати обидва не треба.
     */
    override fun onRegistered(token: String) {
        super.onRegistered(token)
        Log.d(TAG, "onRegistered: токен оновлено")

        val repository = (application as? EventsApp)?.container?.deviceRepository ?: return
        scope.launch {
            // Якщо користувач вимкнув сповіщення, нав'язувати підписку не можна.
            if (repository.isEnabled()) {
                repository.register(token)
            }
        }
    }

    /**
     * Викликається ЛИШЕ коли застосунок на передньому плані.
     *
     * У payload сервера є блок `notification`, тому в згорнутому застосунку
     * сповіщення показує система, а `onMessageReceived` не спрацьовує зовсім —
     * дані приходять в extras Intent'а запуску (див. MainActivity).
     */
    override fun onMessageReceived(message: RemoteMessage) {
        super.onMessageReceived(message)

        val eventId = message.data[PushDeepLink.EXTRA_EVENT_ID]
        Log.d(TAG, "onMessageReceived: event_id=$eventId data=${message.data.keys}")

        AppNotifications.show(
            context = this,
            eventId = eventId,
            title = message.notification?.title,
            body = message.notification?.body,
        )
    }
}
