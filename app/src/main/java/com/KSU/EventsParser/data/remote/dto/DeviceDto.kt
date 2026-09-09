package com.KSU.EventsParser.data.remote.dto

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * Тіло запиту для POST /devices/register і POST /devices/unregister — формат однаковий.
 *
 * Назва теми валідується на сервері за [a-zA-Z0-9-_.~%]+, інакше 422.
 * Тема за замовчуванням — `conferences` (не «events»), береться з BuildConfig.
 */
@Serializable
data class DeviceRegistrationDto(
    @SerialName("fcm_token") val fcmToken: String,
    val topics: List<String>,
)

/** Відповідь обох ендпоінтів: {"status": "subscribed", "topics": "conferences"}. */
@Serializable
data class DeviceRegistrationResponseDto(
    val status: String? = null,
    val topics: String? = null,
)
