package com.KSU.EventsParser.domain.reminder

/** Зсув, розкладений на доби, години й хвилини: 1470 хв → 1 доба 0 год 30 хв. */
data class OffsetParts(val days: Int, val hours: Int, val minutes: Int)

/**
 * Межі зсуву нагадування та його розклад для підпису.
 *
 * Користувач обирає не зсув, а момент спрацювання (дату й час); зсув у хвилинах —
 * лише форма зберігання в Room, щоб нагадування рухалося разом із подією, якщо
 * сервер змінить її час.
 */
object ReminderOffsets {

    /** Зсув за замовчуванням при першому ввімкненні — одна година. */
    const val DEFAULT: Int = 60

    /**
     * Запасні зсуви, якщо до початку менше години: беремо найбільший, що вміщається.
     * На екрані їх не видно — користувач бачить лише момент «Нагадаю о 12:30».
     */
    val FALLBACKS: List<Int> = listOf(30, 15, 5)

    /** Скільки діб наперед можна поставити нагадування. */
    const val MAX_DAYS: Int = 30

    /** Найбільший дозволений зсув — 30 діб. */
    const val MAX: Int = MAX_DAYS * MINUTES_IN_DAY

    /** Розкладає хвилини на доби, години й хвилини для підпису «1 день 2 год 30 хв». */
    fun split(minutes: Int): OffsetParts = OffsetParts(
        days = minutes / MINUTES_IN_DAY,
        hours = minutes % MINUTES_IN_DAY / MINUTES_IN_HOUR,
        minutes = minutes % MINUTES_IN_HOUR,
    )

    private const val MINUTES_IN_HOUR = 60
}

private const val MINUTES_IN_DAY = 1440
