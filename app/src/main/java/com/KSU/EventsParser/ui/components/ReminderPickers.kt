package com.KSU.EventsParser.ui.components

import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDefaults
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.SelectableDates
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TimePicker
import androidx.compose.material3.TimePickerDefaults
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.material3.rememberTimePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import com.KSU.EventsParser.R
import com.KSU.EventsParser.ui.theme.AppColors
import com.KSU.EventsParser.ui.theme.AppType
import com.KSU.EventsParser.ui.theme.EventsParserTheme
import java.time.Instant
import java.time.LocalDate
import java.time.LocalTime
import java.time.ZoneOffset

/**
 * Вибір дати спрацювання нагадування.
 *
 * Обрати можна лише дати з [dates] (від сьогодні до дня події, див.
 * ReminderSchedule.earliestTriggerDate) — решта в календарі неактивні.
 * DatePicker працює з мілісекундами опівночі за UTC, тому дату переводимо
 * саме через UTC, а не через пояс пристрою: інакше день «з'їжджав» би.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ReminderDatePickerDialog(
    initial: LocalDate,
    dates: ClosedRange<LocalDate>,
    onConfirm: (LocalDate) -> Unit,
    onDismiss: () -> Unit,
) {
    val selectable = remember(dates) {
        object : SelectableDates {
            override fun isSelectableDate(utcTimeMillis: Long): Boolean =
                utcMillisToDate(utcTimeMillis) in dates

            override fun isSelectableYear(year: Int): Boolean =
                year in dates.start.year..dates.endInclusive.year
        }
    }
    val state = rememberDatePickerState(
        initialSelectedDateMillis = initial.toUtcMillis(),
        yearRange = dates.start.year..dates.endInclusive.year,
        selectableDates = selectable,
    )
    val colors = DatePickerDefaults.colors(containerColor = AppColors.Surface)

    DatePickerDialog(
        onDismissRequest = onDismiss,
        colors = colors,
        confirmButton = {
            val millis = state.selectedDateMillis
            DialogButton(
                text = stringResource(R.string.action_ok),
                enabled = millis != null,
                onClick = { millis?.let { onConfirm(utcMillisToDate(it)) } },
            )
        },
        dismissButton = {
            DialogButton(text = stringResource(R.string.action_cancel), onClick = onDismiss)
        },
    ) {
        DatePicker(state = state, colors = colors, showModeToggle = false)
    }
}

/**
 * Вибір часу спрацювання нагадування. Формат 24-годинний — як і всюди
 * в застосунку (див. AppDateFormat.time).
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ReminderTimePickerDialog(
    initial: LocalTime,
    onConfirm: (LocalTime) -> Unit,
    onDismiss: () -> Unit,
) {
    val state = rememberTimePickerState(
        initialHour = initial.hour,
        initialMinute = initial.minute,
        is24Hour = true,
    )

    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = AppColors.Surface,
        title = {
            Text(
                text = stringResource(R.string.reminder_pick_time_title),
                style = AppType.DayViewDate,
                color = AppColors.TextTitle,
            )
        },
        text = {
            TimePicker(
                state = state,
                colors = TimePickerDefaults.colors(
                    clockDialColor = AppColors.ChipIdleBg,
                    timeSelectorUnselectedContainerColor = AppColors.ChipIdleBg,
                ),
            )
        },
        confirmButton = {
            DialogButton(
                text = stringResource(R.string.action_ok),
                onClick = { onConfirm(LocalTime.of(state.hour, state.minute)) },
            )
        },
        dismissButton = {
            DialogButton(text = stringResource(R.string.action_cancel), onClick = onDismiss)
        },
    )
}

@Composable
private fun DialogButton(
    text: String,
    onClick: () -> Unit,
    enabled: Boolean = true,
) {
    TextButton(
        onClick = onClick,
        enabled = enabled,
        colors = ButtonDefaults.textButtonColors(
            contentColor = AppColors.Primary,
            disabledContentColor = AppColors.TextDisabled,
        ),
    ) {
        Text(text = text, style = AppType.RowTitle)
    }
}

private fun LocalDate.toUtcMillis(): Long =
    atStartOfDay(ZoneOffset.UTC).toInstant().toEpochMilli()

private fun utcMillisToDate(millis: Long): LocalDate =
    Instant.ofEpochMilli(millis).atZone(ZoneOffset.UTC).toLocalDate()

// ─── Прев'ю ───────────────────────────────────────────────────────────────────

@Preview(showBackground = true, name = "Вибір дати")
@Composable
private fun ReminderDatePickerPreview() {
    EventsParserTheme {
        ReminderDatePickerDialog(
            initial = LocalDate.of(2026, 10, 24),
            dates = LocalDate.of(2026, 10, 19)..LocalDate.of(2026, 10, 25),
            onConfirm = {},
            onDismiss = {},
        )
    }
}

@Preview(showBackground = true, name = "Вибір часу")
@Composable
private fun ReminderTimePickerPreview() {
    EventsParserTheme {
        ReminderTimePickerDialog(
            initial = LocalTime.of(9, 0),
            onConfirm = {},
            onDismiss = {},
        )
    }
}
