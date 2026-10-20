package com.KSU.EventsParser.ui.util

import android.content.res.Resources
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalResources
import com.KSU.EventsParser.R
import com.KSU.EventsParser.domain.reminder.OffsetUnit
import com.KSU.EventsParser.domain.reminder.ReminderOffsets

/**
 * Підпис зсуву нагадування: «15 хв», «3 год», «2 дні».
 *
 * Зсув зберігається в хвилинах; на екран його виводимо в найбільшій одиниці,
 * що ділить значення націло (див. ReminderOffsets.decompose). Варіант без Compose
 * потрібен для сповіщення, яке будується поза екраном.
 */
fun reminderOffsetLabel(res: Resources, minutes: Int): String {
    val (amount, unit) = ReminderOffsets.decompose(minutes)
    return when (unit) {
        OffsetUnit.MINUTES -> res.getString(R.string.reminder_unit_minutes, amount)
        OffsetUnit.HOURS -> res.getString(R.string.reminder_unit_hours, amount)
        OffsetUnit.DAYS -> res.getQuantityString(R.plurals.reminder_days, amount, amount)
    }
}

@Composable
fun reminderOffsetLabel(minutes: Int): String = reminderOffsetLabel(LocalResources.current, minutes)
