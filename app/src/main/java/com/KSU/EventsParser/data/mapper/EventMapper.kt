package com.KSU.EventsParser.data.mapper

import com.KSU.EventsParser.data.local.EventEntity
import com.KSU.EventsParser.data.remote.dto.EventDto
import com.KSU.EventsParser.domain.model.Event
import com.KSU.EventsParser.domain.model.EventType
import java.time.Instant
import java.time.LocalDate
import java.time.LocalTime

/**
 * Перетворення DTO та рядків кешу на доменну модель.
 *
 * Жоден розбір тут не має права кинути виняток: одна крива дата з сервера
 * не повинна зносити цілу сторінку стрічки. Тому всі парсинги загорнуто
 * в runCatching, а сирі рядки зберігаються поруч із розібраними значеннями.
 */
object EventMapper {

    /** Назва-заглушка, якщо сервер несподівано не надіслав event_title. */
    private const val UNTITLED = "—"

    fun toDomain(dto: EventDto): Event = Event(
        id = dto.id,
        title = dto.eventTitle?.takeIf { it.isNotBlank() } ?: UNTITLED,
        date = parseDate(dto.date),
        dateRaw = dto.date,
        time = parseTime(dto.time),
        timeRaw = dto.time,
        location = dto.location,
        organizer = dto.organizer,
        targetAudience = dto.targetAudience,
        type = EventType.fromApi(dto.eventType),
        description = dto.description,
        language = dto.language,
        link = sanitizeLink(dto.link),
        meetingCode = dto.meetingCode?.takeIf { it.isNotBlank() },
        createdAt = parseInstant(dto.createdAt),
        updatedAt = parseInstant(dto.updatedAt),
    )

    fun toEntity(dto: EventDto): EventEntity = EventEntity(
        id = dto.id,
        eventTitle = dto.eventTitle,
        date = dto.date,
        time = dto.time,
        location = dto.location,
        organizer = dto.organizer,
        targetAudience = dto.targetAudience,
        eventType = dto.eventType,
        description = dto.description,
        language = dto.language,
        link = dto.link,
        meetingCode = dto.meetingCode,
        status = dto.status,
        createdAt = dto.createdAt,
        updatedAt = dto.updatedAt,
    )

    fun toDomain(entity: EventEntity): Event = Event(
        id = entity.id,
        title = entity.eventTitle?.takeIf { it.isNotBlank() } ?: UNTITLED,
        date = parseDate(entity.date),
        dateRaw = entity.date,
        time = parseTime(entity.time),
        timeRaw = entity.time,
        location = entity.location,
        organizer = entity.organizer,
        targetAudience = entity.targetAudience,
        type = EventType.fromApi(entity.eventType),
        description = entity.description,
        language = entity.language,
        link = sanitizeLink(entity.link),
        meetingCode = entity.meetingCode?.takeIf { it.isNotBlank() },
        createdAt = parseInstant(entity.createdAt),
        updatedAt = parseInstant(entity.updatedAt),
    )

    /** `date` — YYYY-MM-DD, локальна дата події, без часу й не UTC. */
    private fun parseDate(raw: String?): LocalDate? =
        raw?.takeIf { it.isNotBlank() }?.let { runCatching { LocalDate.parse(it) }.getOrNull() }

    /**
     * `time` — за домовленістю HH:MM, але валідатора на сервері НЕМАЄ.
     * Якщо розбір не вдався, повертаємо null, а сирий рядок показуємо як є —
     * див. Event.timeRaw.
     */
    private fun parseTime(raw: String?): LocalTime? =
        raw?.takeIf { it.isNotBlank() }?.let { runCatching { LocalTime.parse(it) }.getOrNull() }

    /** `created_at` / `updated_at` — ISO 8601 UTC із суфіксом Z і мікросекундами. */
    private fun parseInstant(raw: String?): Instant? =
        raw?.takeIf { it.isNotBlank() }?.let { runCatching { Instant.parse(it) }.getOrNull() }

    /**
     * За контрактом `link` — рівно одне http(s)-посилання. Перевіряємо схему,
     * щоб кнопка «Приєднатися» ніколи не намагалася відкрити не-URL.
     *
     * Свідомо НЕ шукаємо посилання регуляркою в description чи location:
     * backend уже виконав цю роботу, і дублювання логіки тут неминуче
     * розійшлося б із серверною.
     */
    private fun sanitizeLink(raw: String?): String? {
        val trimmed = raw?.trim()?.takeIf { it.isNotBlank() } ?: return null
        val lower = trimmed.lowercase()
        return if (lower.startsWith("http://") || lower.startsWith("https://")) trimmed else null
    }
}
