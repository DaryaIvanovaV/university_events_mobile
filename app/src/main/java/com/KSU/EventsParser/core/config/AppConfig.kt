package com.KSU.EventsParser.core.config

import com.KSU.EventsParser.BuildConfig

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

    val isDebug: Boolean = BuildConfig.DEBUG
}
