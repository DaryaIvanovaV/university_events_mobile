package com.KSU.EventsParser.domain.model

import java.time.Instant
import java.time.LocalDate
import java.time.LocalTime

/**
 * Подія у вигляді, придатному для UI.
 *
 * Гарантовано непорожні за контрактом лише `id`, `event_title`, `event_type`,
 * `status`, `created_at`, `updated_at`. Решта може бути `null` — і це не помилка
 * даних: система принципово не вигадує те, чого не було в оголошенні.
 *
 * Дати й час зберігаємо парами «розібране значення + сирий рядок»:
 * якщо сервер надішле щось, що не парситься, ми покажемо сирий рядок,
 * а не втратимо інформацію і не впадемо.
 */
data class Event(
    val id: Int,
    val title: String,
    /** Розібрана дата. `null`, якщо дати немає або вона не розібралася. */
    val date: LocalDate?,
    /** Сирий рядок дати з API — показуємо, якщо [date] не розібралася. */
    val dateRaw: String?,
    /** Розібраний час. `null`, якщо часу немає або він не у форматі HH:MM. */
    val time: LocalTime?,
    /** Сирий рядок часу. На сервері немає валідатора цього поля: історично
     *  просочувалися значення на кшталт "11:00-16:00". Такий рядок показуємо як є. */
    val timeRaw: String?,
    val location: String?,
    val organizer: String?,
    val targetAudience: String?,
    val type: EventType,
    val description: String?,
    val language: String?,
    /**
     * Рівно одне http(s)-посилання або `null`.
     * Мапер відкидає значення з іншою схемою, щоб кнопка «Приєднатися»
     * ніколи не намагалася відкрити не-URL.
     */
    val link: String?,
    /**
     * Ідентифікатор конференції та код доступу, наприклад "845 2371 9004, код 316742".
     * Це НЕ URL — відкривати його як посилання не можна, лише копіювати.
     * Ситуація «meetingCode є, link == null» цілком нормальна.
     */
    val meetingCode: String?,
    val createdAt: Instant?,
    val updatedAt: Instant?,
) {
    /** Чи є що показати в рядку часу (розібране значення або сирий рядок). */
    val hasAnyTime: Boolean get() = time != null || !timeRaw.isNullOrBlank()

    /** Чи є що показати в рядку дати. */
    val hasAnyDate: Boolean get() = date != null || !dateRaw.isNullOrBlank()
}
