package com.KSU.EventsParser.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.LocalTextStyle
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.tooling.preview.Preview
import com.KSU.EventsParser.R
import com.KSU.EventsParser.core.config.AppConfig
import com.KSU.EventsParser.domain.reminder.OffsetUnit
import com.KSU.EventsParser.domain.reminder.ReminderOffsets
import com.KSU.EventsParser.domain.reminder.ReminderSchedule
import com.KSU.EventsParser.ui.theme.AppColors
import com.KSU.EventsParser.ui.theme.AppType
import com.KSU.EventsParser.ui.theme.Dimens
import com.KSU.EventsParser.ui.theme.EventsParserTheme
import com.KSU.EventsParser.ui.util.reminderOffsetLabel
import java.time.Instant
import java.time.ZonedDateTime

/**
 * Діалог власного зсуву нагадування: число та одиниця (хвилини / години / доби).
 *
 * Перевірка: від 1 до [ReminderOffsets.MAX] хвилин, і момент спрацювання має бути
 * в майбутньому. Помилку показуємо, лише коли користувач уже щось ввів, а кнопку
 * підтвердження до того часу просто вимкнено.
 *
 * Введений текст — локальний стан діалогу, а не екрана: він потрібен тільки
 * поки діалог відкритий.
 *
 * @param start початок події, від якого відраховується зсув.
 * @param initialMinutes поточний зсув (щоб відкрити діалог із ним) або `null`.
 * @param onConfirm підтверджено допустимий зсув у хвилинах.
 */
@Composable
fun ReminderCustomDialog(
    start: ZonedDateTime,
    initialMinutes: Int?,
    onConfirm: (Int) -> Unit,
    onDismiss: () -> Unit,
) {
    // Пресети вже мають свої чипи, тож їх у полі не підставляємо: порожнє поле
    // зрозуміліше, ніж число, яке збігається з одним із чипів.
    val initial = remember {
        initialMinutes
            ?.takeIf { it !in ReminderOffsets.PRESETS }
            ?.let(ReminderOffsets::decompose)
    }
    var text by remember { mutableStateOf(initial?.amount?.toString().orEmpty()) }
    var unit by remember { mutableStateOf(initial?.unit ?: OffsetUnit.MINUTES) }

    val amount = text.toIntOrNull()
    val minutes = amount?.let { it * unit.minutes }
    val isValid = minutes != null &&
        minutes in 1..ReminderOffsets.MAX &&
        ReminderSchedule.isOffsetValid(start, minutes, Instant.now())

    val focusRequester = remember { FocusRequester() }
    LaunchedEffect(Unit) { focusRequester.requestFocus() }

    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = AppColors.Surface,
        title = {
            Text(
                text = stringResource(R.string.reminder_custom_title),
                style = AppType.DayViewDate,
                color = AppColors.TextTitle,
            )
        },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(Dimens.ReminderDialogGap)) {
                Text(
                    text = stringResource(R.string.reminder_custom_hint),
                    style = AppType.RowSubtitle,
                    color = AppColors.TextMuted,
                )

                AmountField(
                    value = text,
                    onValueChange = { input ->
                        // Лише цифри й обмежена довжина: так amount * unit не переповнить Int.
                        text = input.filter(Char::isDigit).take(MAX_DIGITS)
                    },
                    modifier = Modifier.focusRequester(focusRequester),
                )

                Row(horizontalArrangement = Arrangement.spacedBy(Dimens.ChipGap)) {
                    OffsetUnit.entries.forEach { option ->
                        ReminderChip(
                            text = stringResource(unitLabelRes(option)),
                            selected = unit == option,
                            onClick = { unit = option },
                        )
                    }
                }

                if (text.isNotEmpty() && !isValid) {
                    Text(
                        text = stringResource(
                            R.string.reminder_custom_error,
                            reminderOffsetLabel(ReminderOffsets.MAX),
                        ),
                        style = AppType.RowSubtitle,
                        color = AppColors.CodeTint,
                    )
                }
            }
        },
        confirmButton = {
            TextButton(
                onClick = { if (isValid && minutes != null) onConfirm(minutes) },
                enabled = isValid,
                colors = ButtonDefaults.textButtonColors(
                    contentColor = AppColors.Primary,
                    disabledContentColor = AppColors.TextDisabled,
                ),
            ) {
                Text(text = stringResource(R.string.action_ok), style = AppType.RowTitle)
            }
        },
        dismissButton = {
            TextButton(
                onClick = onDismiss,
                colors = ButtonDefaults.textButtonColors(contentColor = AppColors.TextChipIdle),
            ) {
                Text(text = stringResource(R.string.action_cancel), style = AppType.RowTitle)
            }
        },
    )
}

/** Поле вводу числа у вигляді, схожому на поле фільтра зі списку подій. */
@Composable
private fun AmountField(
    value: String,
    onValueChange: (String) -> Unit,
    modifier: Modifier = Modifier,
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .heightIn(min = Dimens.ReminderInputMinHeight)
            .clip(RoundedCornerShape(Dimens.ChipRadius))
            .background(AppColors.ChipIdleBg)
            .padding(
                horizontal = Dimens.FilterChipPaddingH,
                vertical = Dimens.FilterChipPaddingV,
            ),
        contentAlignment = Alignment.CenterStart,
    ) {
        if (value.isEmpty()) {
            Text(
                text = stringResource(R.string.reminder_custom_amount_hint),
                style = AppType.InfoValue,
                color = AppColors.TextDisabled,
            )
        }
        BasicTextField(
            value = value,
            onValueChange = onValueChange,
            singleLine = true,
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
            textStyle = LocalTextStyle.current.merge(
                AppType.InfoValue.copy(color = AppColors.TextChipIdle),
            ),
            cursorBrush = SolidColor(AppColors.Primary),
            modifier = modifier.fillMaxWidth(),
        )
    }
}

private fun unitLabelRes(unit: OffsetUnit): Int = when (unit) {
    OffsetUnit.MINUTES -> R.string.reminder_custom_unit_minutes
    OffsetUnit.HOURS -> R.string.reminder_custom_unit_hours
    OffsetUnit.DAYS -> R.string.reminder_custom_unit_days
}

/** Максимум цифр у числі: 99999 діб * 1440 хвилин ще вміщається в Int. */
private const val MAX_DIGITS = 5

@Preview(showBackground = true, name = "Діалог власного значення")
@Composable
private fun ReminderCustomDialogPreview() {
    EventsParserTheme {
        ReminderCustomDialog(
            start = ZonedDateTime.of(2026, 10, 25, 10, 0, 0, 0, AppConfig.eventZone),
            initialMinutes = null,
            onConfirm = {},
            onDismiss = {},
        )
    }
}
