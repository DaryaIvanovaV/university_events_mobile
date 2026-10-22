package com.KSU.EventsParser.ui.util

import android.content.res.Resources
import com.KSU.EventsParser.R
import com.KSU.EventsParser.core.util.AppDateFormat
import com.KSU.EventsParser.domain.reminder.ReminderOffsets
import com.KSU.EventsParser.domain.reminder.ReminderSchedule
import java.time.ZonedDateTime
import java.util.Locale

/**
 * Підпис зсуву нагадування для сповіщення: «30 хв», «1 год 30 хв», «1 день 2 год».
 *
 * Зсув зберігається в хвилинах і може бути будь-яким (користувач обирає момент,
 * а не зсув), тому виводимо всі ненульові частини (див. ReminderOffsets.split).
 * Варіант без Compose потрібен для сповіщення, яке будується поза екраном.
 */
fun reminderOffsetLabel(res: Resources, minutes: Int): String {
    val (days, hours, mins) = ReminderOffsets.split(minutes)
    val parts = buildList {
        if (days > 0) add(res.getQuantityString(R.plurals.reminder_days, days, days))
        if (hours > 0) add(res.getString(R.string.reminder_unit_hours, hours))
        if (mins > 0 || isEmpty()) add(res.getString(R.string.reminder_unit_minutes, mins))
    }
    return parts.joinToString(" ")
}

/**
 * Підзаголовок увімкненого нагадування: коли воно спрацює, а не за скільки до початку.
 *
 * Якщо спрацювання випадає на ту саму добу, що й початок, показуємо лише час
 * («Нагадаю о 13:00»), інакше ще й дату («Нагадаю 20 жовтня о 16:00») — інакше
 * нагадування «за добу» чи через північ вводило б в оману. Час — у поясі події
 * ([start] уже в ньому), локаль передається явно (див. AppDateFormat).
 */
fun reminderTriggerText(
    res: Resources,
    locale: Locale,
    start: ZonedDateTime,
    offsetMinutes: Int,
): String {
    val trigger = ReminderSchedule.triggerAt(start, offsetMinutes)
    val time = AppDateFormat.time(trigger.toLocalTime())
    return if (ReminderSchedule.isSameDay(start, trigger)) {
        res.getString(R.string.reminder_fires_at, time)
    } else {
        res.getString(
            R.string.reminder_fires_on,
            AppDateFormat.dayMonth(trigger.toLocalDate(), locale),
            time,
        )
    }
}
