package com.KSU.EventsParser.domain.reminder

/**
 * Одиниця виміру зсуву нагадування. [minutes] — скільки хвилин в одній одиниці.
 */
enum class OffsetUnit(val minutes: Int) {
    MINUTES(1),
    HOURS(60),
    DAYS(1440),
}

/** Зсув у зручному для людини вигляді: «3 години», «2 дні», «45 хвилин». */
data class OffsetAmount(val amount: Int, val unit: OffsetUnit) {
    /** Повертає зсув у хвилинах. */
    fun toMinutes(): Int = amount * unit.minutes
}

/**
 * Допустимі зсуви нагадування: пресети, значення за замовчуванням і межа.
 *
 * Зсув завжди зберігаємо в хвилинах (як у Room), а в одиниці для підпису
 * розкладаємо лише на межі показу — див. [decompose].
 */
object ReminderOffsets {

    /** Пресети з макета, у хвилинах: 15 хв, 30 хв, 1 год, 3 год, 1 доба. */
    val PRESETS: List<Int> = listOf(15, 30, 60, 180, 1440)

    /** Зсув за замовчуванням — одна година. */
    const val DEFAULT: Int = 60

    /** Найбільший дозволений зсув — 30 діб. */
    const val MAX: Int = 30 * 1440

    /**
     * Розкладає хвилини на найбільшу одиницю, яка ділить значення націло:
     * 1440 → 1 доба, 180 → 3 години, 90 → 90 хвилин.
     */
    fun decompose(minutes: Int): OffsetAmount = when {
        minutes > 0 && minutes % OffsetUnit.DAYS.minutes == 0 ->
            OffsetAmount(minutes / OffsetUnit.DAYS.minutes, OffsetUnit.DAYS)
        minutes > 0 && minutes % OffsetUnit.HOURS.minutes == 0 ->
            OffsetAmount(minutes / OffsetUnit.HOURS.minutes, OffsetUnit.HOURS)
        else -> OffsetAmount(minutes, OffsetUnit.MINUTES)
    }
}
