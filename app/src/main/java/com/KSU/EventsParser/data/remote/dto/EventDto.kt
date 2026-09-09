package com.KSU.EventsParser.data.remote.dto

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * Подія рівно в тому вигляді, як її віддає API.
 *
 * Свідомо ВСІ поля, крім `id`, оголошені nullable зі значенням за замовчуванням —
 * навіть ті, що за контрактом непорожні (`event_title`, `event_type`). Причина:
 * якщо сервер колись віддасть запис без обов'язкового поля, kotlinx.serialization
 * кине MissingFieldException і ми втратимо ВСЮ сторінку, а не один запис.
 * Значення за замовчуванням підставляє мапер — див. EventMapper.
 *
 * Поля `source_channel` і `source_message_id` службові й тут навмисно відсутні:
 * Json налаштований з ignoreUnknownKeys = true, тому вони просто ігноруються.
 */
@Serializable
data class EventDto(
    val id: Int,
    @SerialName("event_title") val eventTitle: String? = null,
    val date: String? = null,
    val time: String? = null,
    val location: String? = null,
    val organizer: String? = null,
    @SerialName("target_audience") val targetAudience: String? = null,
    @SerialName("event_type") val eventType: String? = null,
    val description: String? = null,
    val language: String? = null,
    val link: String? = null,
    @SerialName("meeting_code") val meetingCode: String? = null,
    val status: String? = null,
    @SerialName("created_at") val createdAt: String? = null,
    @SerialName("updated_at") val updatedAt: String? = null,
)
