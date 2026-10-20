package com.KSU.EventsParser.core.config

import com.KSU.EventsParser.BuildConfig
import java.time.DateTimeException
import java.time.LocalTime
import java.time.ZoneId

/**
 * Єдина точка доступу до налаштувань збірки.
 *
 * Значення надходять із gradle.properties → buildConfigField → BuildConfig.
 * Ніде більше в коді не повинно бути ані URL, ані назви теми, ані розмірів
 * сторінок — усе читається звідси.
 */
object AppConfig {

    /**
     * Базова адреса API.
     * debug   — http://10.0.2.2:8000 (машина розробника з погляду емулятора);
     * release — заглушка, замінити на реальний хост.
     * Для фізичного пристрою треба LAN-IP машини та `uvicorn --host 0.0.0.0`.
     */
    val baseUrl: String = BuildConfig.BASE_URL

    /** Тема FCM. Сервер валідує назву за [a-zA-Z0-9-_.~%]+, інакше 422. */
    val fcmTopic: String = BuildConfig.FCM_TOPIC

    /** Розмір сторінки GET /events. Сервер дозволяє 1…500. */
    val feedPageSize: Int = BuildConfig.FEED_PAGE_SIZE

    /** Розмір сторінки GET /events/sync. Максимум на сервері — 1000. */
    val syncPageSize: Int = BuildConfig.SYNC_PAGE_SIZE

    /**
     * Часовий пояс подій. Дзеркалить APP_TIMEZONE на сервері: `date` і `time`
     * події — локальні дата й час у цьому поясі, а не в поясі пристрою.
     *
     * На старих пристроях без оновленої бази часових поясів назви «Europe/Kyiv»
     * може не бути: тоді беремо пояс пристрою, щоб не впасти під час запуску.
     */
    val eventZone: ZoneId = try {
        ZoneId.of(BuildConfig.EVENT_TIME_ZONE)
    } catch (_: DateTimeException) {
        ZoneId.systemDefault()
    }

    /**
     * Час, від якого рахується нагадування, якщо в події є дата, але немає
     * часу початку. Час початку з `timeRaw` не вигадуємо.
     */
    val reminderFallbackTime: LocalTime = LocalTime.parse(BuildConfig.REMINDER_FALLBACK_TIME)

    val isDebug: Boolean = BuildConfig.DEBUG
}
