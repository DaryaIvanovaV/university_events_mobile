package com.KSU.EventsParser.data.remote.dto

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * Відповідь GET /events/sync.
 *
 * `cursor` НЕПРОЗОРИЙ: зберігаємо й повертаємо його дослівно. Формат — деталь
 * реалізації сервера (всередині пара «момент, id»), розбирати або конструювати
 * його самотужки не можна.
 *
 * `removed` не опціональний: модератор може відхилити вже опубліковану подію
 * (захід скасували). Зі стрічки GET /events вона просто зникає, і клієнт із кешем
 * показував би скасовану подію — студент прийшов би до зачиненої аудиторії.
 */
@Serializable
data class SyncResponseDto(
    val cursor: String? = null,
    @SerialName("has_more") val hasMore: Boolean = false,
    val changed: List<EventDto> = emptyList(),
    val removed: List<Int> = emptyList(),
)
