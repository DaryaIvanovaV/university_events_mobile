package com.KSU.EventsParser.domain.reminder

import com.KSU.EventsParser.domain.model.Event
import java.time.Instant
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime
import java.time.ZoneId
import java.time.ZonedDateTime
import java.time.temporal.ChronoUnit

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
     * Зсув віднімаємо в ЛОКАЛЬНОМУ (настінному) часі поясу події. Так «за добу»
     * зберігає той самий настінний час через перехід на літній/зимовий час
     * (у Києві зимовий час настає в останню неділю жовтня), а обраний користувачем
     * момент перетворюється на зсув і назад без втрат (див. [offsetFor]).
     */
    fun triggerAt(start: ZonedDateTime, offsetMinutes: Int): ZonedDateTime =
        start.toLocalDateTime()
            .minusMinutes(offsetMinutes.toLong())
            .atZone(start.zone)

    /**
     * Зсув для обраного користувачем моменту [trigger] (настінний час у поясі [start])
     * або `null`, якщо момент не підходить: уже минув, не раніше початку події
     * або далі ніж за [ReminderOffsets.MAX].
     */
    fun offsetFor(start: ZonedDateTime, trigger: LocalDateTime, now: Instant): Int? {
        val minutes = ChronoUnit.MINUTES.between(trigger, start.toLocalDateTime())
        if (minutes !in 1..ReminderOffsets.MAX.toLong()) return null
        val offset = minutes.toInt()
        return offset.takeIf { isOffsetValid(start, it, now) }
    }

    /**
     * Найраніша дата, яку можна обрати для нагадування: сьогодні (у поясі події),
     * але не раніше ніж за [ReminderOffsets.MAX_DAYS] діб до дати початку.
     * Межу беремо на добу ближче, щоб будь-який час обраної дати вміщався в MAX.
     */
    fun earliestTriggerDate(start: ZonedDateTime, now: Instant): LocalDate {
        val today = now.atZone(start.zone).toLocalDate()
        val limit = start.toLocalDate().minusDays((ReminderOffsets.MAX_DAYS - 1).toLong())
        return maxOf(today, limit)
    }

    /**
     * Чи спрацює нагадування в ту саму календарну добу, що й початок події.
     *
     * Порівнюємо дати в поясі [start], а не в поясі пристрою: «23:30 попередньої
     * доби» для події о 00:30 має показатися з датою, а «13:00» для події о 16:00 —
     * без неї. Тому [trigger] спершу переводимо в пояс [start].
     */
    fun isSameDay(start: ZonedDateTime, trigger: ZonedDateTime): Boolean =
        start.toLocalDate() == trigger.withZoneSameInstant(start.zone).toLocalDate()

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
     * із запасних, що вміщається. `null` — до початку лічені хвилини, і UI одразу
     * пропонує обрати час.
     */
    fun defaultOffset(start: ZonedDateTime, now: Instant): Int? =
        (listOf(ReminderOffsets.DEFAULT) + ReminderOffsets.FALLBACKS)
            .firstOrNull { isOffsetValid(start, it, now) }
}
