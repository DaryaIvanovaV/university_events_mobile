package com.KSU.EventsParser.domain.reminder

import com.KSU.EventsParser.domain.model.Event
import java.time.Instant
import java.time.LocalTime
import java.time.ZoneId
import java.time.ZonedDateTime

/**
 * Чиста арифметика часу для нагадувань. Жодних Android-класів: усе, що тут є,
 * перевіряється звичайними JVM-тестами.
 *
 * `date` і `time` події — це локальні дата й час у поясі [ZoneId] подій
 * (AppConfig.eventZone), а не в поясі пристрою.
 */
object ReminderSchedule {

    /** Чи можна поставити нагадування на подію. */
    sealed interface Availability {
        /** Дати немає (або вона не розібралася) — відліку не існує. */
        data object NoDate : Availability

        /** Подія вже почалася — нагадувати пізно. */
        data object Started : Availability

        /**
         * Нагадування можливе. [usesFallbackTime] — часу в події немає, і початок
         * умовно взято з резервного часу (зазвичай 09:00); UI має про це сказати.
         */
        data class Available(
            val start: ZonedDateTime,
            val usesFallbackTime: Boolean,
        ) : Availability
    }

    /**
     * Момент початку події або `null`, якщо дати немає.
     * Без розібраного часу беремо [fallbackTime]: час із `timeRaw` на кшталт
     * "11:00-16:00" не вигадуємо.
     */
    fun eventStart(event: Event, zone: ZoneId, fallbackTime: LocalTime): ZonedDateTime? {
        val date = event.date ?: return null
        return date.atTime(event.time ?: fallbackTime).atZone(zone)
    }

    /**
     * Момент спрацювання нагадування за [offsetMinutes] до [start].
     *
     * Цілі доби віднімаємо в ЛОКАЛЬНОМУ часі, а хвилини, що лишилися, — на шкалі
     * миттєвостей. Так «за добу» зберігає той самий настінний час через перехід
     * на літній/зимовий час (у Києві зимовий час настає в останню неділю жовтня).
     */
    fun triggerAt(start: ZonedDateTime, offsetMinutes: Int): ZonedDateTime =
        start
            .minusDays((offsetMinutes / MINUTES_IN_DAY).toLong())
            .minusMinutes((offsetMinutes % MINUTES_IN_DAY).toLong())

    /** Визначає, чи можна ставити нагадування на [event] у момент [now]. */
    fun availability(
        event: Event,
        now: Instant,
        zone: ZoneId,
        fallbackTime: LocalTime,
    ): Availability {
        val start = eventStart(event, zone, fallbackTime) ?: return Availability.NoDate
        if (!start.toInstant().isAfter(now)) return Availability.Started
        return Availability.Available(
            start = start,
            usesFallbackTime = event.time == null,
        )
    }

    /** Зсув придатний, якщо момент спрацювання ще попереду. */
    fun isOffsetValid(start: ZonedDateTime, offsetMinutes: Int, now: Instant): Boolean =
        triggerAt(start, offsetMinutes).toInstant().isAfter(now)

    /**
     * Зсув для першого ввімкнення: година, якщо вона ще можлива, інакше найбільший
     * пресет, що вміщається. `null` — не вміщається жоден пресет, і UI відкриває
     * діалог власного значення.
     */
    fun defaultOffset(start: ZonedDateTime, now: Instant): Int? {
        if (isOffsetValid(start, ReminderOffsets.DEFAULT, now)) return ReminderOffsets.DEFAULT
        return ReminderOffsets.PRESETS
            .filter { isOffsetValid(start, it, now) }
            .maxOrNull()
    }

    private const val MINUTES_IN_DAY = 1440
}
