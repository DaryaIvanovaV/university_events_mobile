package com.KSU.EventsParser.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalResources
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import com.KSU.EventsParser.R
import com.KSU.EventsParser.core.config.AppConfig
import com.KSU.EventsParser.core.util.AppDateFormat
import com.KSU.EventsParser.domain.reminder.ReminderSchedule
import com.KSU.EventsParser.domain.reminder.ReminderSchedule.Availability
import com.KSU.EventsParser.ui.theme.AppColors
import com.KSU.EventsParser.ui.theme.AppType
import com.KSU.EventsParser.ui.theme.Dimens
import com.KSU.EventsParser.ui.theme.EventsParserTheme
import com.KSU.EventsParser.ui.util.rememberAppLocale
import com.KSU.EventsParser.ui.util.reminderTriggerText
import java.time.ZonedDateTime

/**
 * Вміст блоку «Нагадування» на екрані деталей (картку-обгортку малює екран).
 *
 * Без власного стану: що показувати, вирішує екран. Користувач обирає не зсув
 * («за 15 хв»), а сам момент спрацювання — дату й час; два чипи відкривають
 * відповідні діалоги (див. ReminderPickers).
 *
 * @param availability чи можна взагалі поставити нагадування (немає дати / вже почалася).
 * @param offsetMinutes увімкнений зараз зсув у хвилинах або `null`, якщо нагадування вимкнено.
 * @param permissionDenied користувач відмовив у дозволі на сповіщення.
 * @param invalidPick останній обраний момент не підійшов (уже минув чи не раніше початку).
 * @param onToggle перемикач; `true` — користувач хоче ввімкнути.
 * @param onPickDate натиснуто чип дати.
 * @param onPickTime натиснуто чип часу.
 */
@OptIn(ExperimentalLayoutApi::class)
@Composable
fun ReminderPanel(
    availability: Availability,
    offsetMinutes: Int?,
    permissionDenied: Boolean,
    invalidPick: Boolean,
    onToggle: (Boolean) -> Unit,
    onPickDate: () -> Unit,
    onPickTime: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val available = availability as? Availability.Available
    val isOn = offsetMinutes != null
    // Вимкнути можна завжди; ввімкнути — лише коли є від чого відраховувати.
    val toggleEnabled = isOn || available != null

    Column(modifier = modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(Dimens.LinkRowGap),
        ) {
            Icon(
                imageVector = AppIcons.Bell,
                contentDescription = null,
                tint = if (isOn) AppColors.Accent else AppColors.TextMuted,
                modifier = Modifier.size(Dimens.IconBellDetail),
            )
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = stringResource(R.string.reminder_title),
                    style = AppType.RowTitle,
                    color = AppColors.TextTitle,
                )
                Text(
                    text = when {
                        offsetMinutes == null -> stringResource(R.string.settings_state_off)
                        // Без початку (подію перенесли чи вона вже почалася) момент
                        // спрацювання не порахувати; пояснення дає підказка нижче.
                        available == null -> stringResource(R.string.settings_state_on)
                        else -> reminderTriggerText(
                            res = LocalResources.current,
                            locale = rememberAppLocale(),
                            start = available.start,
                            offsetMinutes = offsetMinutes,
                        )
                    },
                    style = AppType.RowSubtitle,
                    color = AppColors.TextMuted,
                )
            }
            AppToggle(
                checked = isOn,
                onCheckedChange = onToggle,
                enabled = toggleEnabled,
                modifier = Modifier.alpha(if (toggleEnabled) 1f else Dimens.ReminderDisabledAlpha),
            )
        }

        // Пояснення, чому блок недоступний, або від якого часу йде відлік.
        when (availability) {
            Availability.NoDate -> ReminderHint(text = stringResource(R.string.reminder_no_date))
            Availability.Started -> ReminderHint(text = stringResource(R.string.reminder_event_started))
            is Availability.Available -> if (availability.usesFallbackTime) {
                ReminderHint(
                    text = stringResource(
                        R.string.reminder_fallback_time_hint,
                        AppDateFormat.time(availability.start.toLocalTime()),
                    ),
                )
            }
        }

        if (permissionDenied) {
            ReminderHint(
                text = stringResource(R.string.reminder_permission_hint),
                color = AppColors.CodeTint,
            )
        }

        if (isOn && available != null) {
            val trigger = ReminderSchedule.triggerAt(available.start, offsetMinutes)
            FlowRow(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = Dimens.ReminderChipsTopGap),
                horizontalArrangement = Arrangement.spacedBy(Dimens.ChipGap),
                verticalArrangement = Arrangement.spacedBy(Dimens.ChipGap),
            ) {
                ReminderChip(
                    text = AppDateFormat.dayMonth(trigger.toLocalDate(), rememberAppLocale()),
                    leadingIcon = AppIcons.Calendar,
                    contentDescription = stringResource(R.string.cd_reminder_pick_date),
                    onClick = onPickDate,
                )
                ReminderChip(
                    text = AppDateFormat.time(trigger.toLocalTime()),
                    leadingIcon = AppIcons.Clock,
                    contentDescription = stringResource(R.string.cd_reminder_pick_time),
                    onClick = onPickTime,
                )
            }
        }

        if (invalidPick) {
            ReminderHint(
                text = stringResource(R.string.reminder_pick_invalid),
                color = AppColors.CodeTint,
            )
        }
    }
}

@Composable
private fun ReminderHint(
    text: String,
    color: Color = AppColors.TextMuted,
) {
    Text(
        text = text,
        style = AppType.RowSubtitle,
        color = color,
        modifier = Modifier.padding(top = Dimens.ReminderHintTopGap),
    )
}

/**
 * Чип дати чи часу нагадування. Повторює вигляд FilterChip зі списку подій,
 * плюс іконка ліворуч, щоб було видно, що саме він змінює.
 */
@Composable
private fun ReminderChip(
    text: String,
    leadingIcon: ImageVector,
    contentDescription: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier
            .clip(RoundedCornerShape(Dimens.ChipRadius))
            .background(AppColors.ChipIdleBg)
            .clickable(onClickLabel = contentDescription, onClick = onClick)
            .padding(
                horizontal = Dimens.FilterChipPaddingH,
                vertical = Dimens.FilterChipPaddingV,
            ),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(Dimens.ChipGap),
    ) {
        Icon(
            imageVector = leadingIcon,
            contentDescription = null,
            tint = AppColors.TextChipIdle,
            modifier = Modifier.size(Dimens.IconInfoRow),
        )
        Text(
            text = text,
            style = AppType.ChipFilter,
            color = AppColors.TextChipIdle,
        )
    }
}

// ─── Прев'ю ───────────────────────────────────────────────────────────────────

private val PreviewStart: ZonedDateTime =
    ZonedDateTime.of(2026, 10, 25, 10, 0, 0, 0, AppConfig.eventZone)

@Composable
private fun ReminderPanelPreview(
    availability: Availability,
    offsetMinutes: Int?,
    permissionDenied: Boolean = false,
    invalidPick: Boolean = false,
) {
    EventsParserTheme {
        Box(
            modifier = Modifier
                .background(AppColors.Surface)
                .padding(Dimens.ScreenPadding),
        ) {
            ReminderPanel(
                availability = availability,
                offsetMinutes = offsetMinutes,
                permissionDenied = permissionDenied,
                invalidPick = invalidPick,
                onToggle = {},
                onPickDate = {},
                onPickTime = {},
            )
        }
    }
}

@Preview(showBackground = true, name = "Вимкнено")
@Composable
private fun ReminderPanelOffPreview() {
    ReminderPanelPreview(Availability.Available(PreviewStart, usesFallbackTime = false), null)
}

@Preview(showBackground = true, name = "Увімкнено")
@Composable
private fun ReminderPanelOnPreview() {
    ReminderPanelPreview(Availability.Available(PreviewStart, usesFallbackTime = false), 60)
}

@Preview(showBackground = true, name = "Та сама доба: подія о 16:00, нагадати о 13:00")
@Composable
private fun ReminderPanelSameDayPreview() {
    ReminderPanelPreview(
        Availability.Available(PreviewStart.withHour(16), usesFallbackTime = false),
        180,
    )
}

@Preview(showBackground = true, name = "Через північ: подія о 00:30, нагадати о 23:30")
@Composable
private fun ReminderPanelCrossMidnightPreview() {
    ReminderPanelPreview(
        Availability.Available(
            PreviewStart.withHour(0).withMinute(30),
            usesFallbackTime = false,
        ),
        60,
    )
}

@Preview(showBackground = true, name = "Напередодні в той самий час")
@Composable
private fun ReminderPanelDayBeforePreview() {
    ReminderPanelPreview(Availability.Available(PreviewStart, usesFallbackTime = false), 1440)
}

@Preview(showBackground = true, name = "Резервний час 09:00")
@Composable
private fun ReminderPanelFallbackTimePreview() {
    ReminderPanelPreview(
        Availability.Available(PreviewStart.withHour(9), usesFallbackTime = true),
        180,
    )
}

@Preview(showBackground = true, name = "Обраний момент не підходить")
@Composable
private fun ReminderPanelInvalidPickPreview() {
    ReminderPanelPreview(
        Availability.Available(PreviewStart, usesFallbackTime = false),
        60,
        invalidPick = true,
    )
}

@Preview(showBackground = true, name = "Без дати")
@Composable
private fun ReminderPanelNoDatePreview() {
    ReminderPanelPreview(Availability.NoDate, null)
}

@Preview(showBackground = true, name = "Подія вже почалася")
@Composable
private fun ReminderPanelStartedPreview() {
    ReminderPanelPreview(Availability.Started, null)
}

@Preview(showBackground = true, name = "Увімкнено, але подія вже почалася")
@Composable
private fun ReminderPanelStartedOnPreview() {
    ReminderPanelPreview(Availability.Started, 60)
}

@Preview(showBackground = true, name = "Увімкнено, але дату прибрано")
@Composable
private fun ReminderPanelNoDateOnPreview() {
    ReminderPanelPreview(Availability.NoDate, 60)
}
