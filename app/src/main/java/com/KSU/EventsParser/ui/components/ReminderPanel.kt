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
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import com.KSU.EventsParser.R
import com.KSU.EventsParser.core.config.AppConfig
import com.KSU.EventsParser.core.util.AppDateFormat
import com.KSU.EventsParser.domain.reminder.ReminderOffsets
import com.KSU.EventsParser.domain.reminder.ReminderSchedule
import com.KSU.EventsParser.domain.reminder.ReminderSchedule.Availability
import com.KSU.EventsParser.ui.theme.AppColors
import com.KSU.EventsParser.ui.theme.AppType
import com.KSU.EventsParser.ui.theme.Dimens
import com.KSU.EventsParser.ui.theme.EventsParserTheme
import com.KSU.EventsParser.ui.util.reminderOffsetLabel
import java.time.Instant
import java.time.ZonedDateTime

/**
 * Вміст блоку «Нагадування» на екрані деталей (картку-обгортку малює екран).
 *
 * Без власного стану: що показувати, вирішує екран.
 *
 * @param availability чи можна взагалі поставити нагадування (немає дати / вже почалася).
 * @param offsetMinutes увімкнений зараз зсув у хвилинах або `null`, якщо нагадування вимкнено.
 * @param now «зараз» для визначення пресетів, які вже не вміщаються до початку події.
 * @param permissionDenied користувач відмовив у дозволі на сповіщення.
 * @param onToggle перемикач; `true` — користувач хоче ввімкнути.
 * @param onSelectOffset вибрано пресет (у хвилинах).
 * @param onCustomClick натиснуто «Своє…».
 */
@OptIn(ExperimentalLayoutApi::class)
@Composable
fun ReminderPanel(
    availability: Availability,
    offsetMinutes: Int?,
    now: Instant,
    permissionDenied: Boolean,
    onToggle: (Boolean) -> Unit,
    onSelectOffset: (Int) -> Unit,
    onCustomClick: () -> Unit,
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
                    text = if (offsetMinutes != null) {
                        stringResource(R.string.reminder_before, reminderOffsetLabel(offsetMinutes))
                    } else {
                        stringResource(R.string.settings_state_off)
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
            FlowRow(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = Dimens.ReminderChipsTopGap),
                horizontalArrangement = Arrangement.spacedBy(Dimens.ChipGap),
                verticalArrangement = Arrangement.spacedBy(Dimens.ChipGap),
            ) {
                ReminderOffsets.PRESETS.forEach { preset ->
                    ReminderChip(
                        text = reminderOffsetLabel(preset),
                        selected = offsetMinutes == preset,
                        // Пресет, за який вже запізно, вимикаємо, а не ховаємо:
                        // так видно, що він існує, але не вміщається.
                        enabled = ReminderSchedule.isOffsetValid(available.start, preset, now),
                        onClick = { onSelectOffset(preset) },
                    )
                }
                ReminderChip(
                    text = stringResource(R.string.reminder_custom),
                    selected = offsetMinutes !in ReminderOffsets.PRESETS,
                    onClick = onCustomClick,
                )
            }
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
 * Чип зсуву. Повторює вигляд FilterChip зі списку подій, але має ще
 * й вимкнений стан. `internal`, бо тими самими чипами користується діалог
 * власного значення.
 */
@Composable
internal fun ReminderChip(
    text: String,
    selected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
) {
    val background = when {
        selected -> AppColors.Primary
        !enabled -> AppColors.Divider
        else -> AppColors.ChipIdleBg
    }
    val content = when {
        selected -> AppColors.OnPrimary
        !enabled -> AppColors.TextDisabled
        else -> AppColors.TextChipIdle
    }

    Box(
        modifier = modifier
            .clip(RoundedCornerShape(Dimens.ChipRadius))
            .background(background)
            .clickable(enabled = enabled, onClick = onClick)
            .padding(
                horizontal = Dimens.FilterChipPaddingH,
                vertical = Dimens.FilterChipPaddingV,
            ),
    ) {
        Text(
            text = text,
            style = AppType.ChipFilter,
            color = content,
        )
    }
}

// ─── Прев'ю ───────────────────────────────────────────────────────────────────

/** Фіксований «зараз» для прев'ю: 2026-10-19 12:00 за часом подій. */
private val PreviewNow: Instant =
    ZonedDateTime.of(2026, 10, 19, 12, 0, 0, 0, AppConfig.eventZone).toInstant()

private val PreviewStart: ZonedDateTime =
    ZonedDateTime.of(2026, 10, 25, 10, 0, 0, 0, AppConfig.eventZone)

@Composable
private fun ReminderPanelPreview(
    availability: Availability,
    offsetMinutes: Int?,
    permissionDenied: Boolean = false,
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
                now = PreviewNow,
                permissionDenied = permissionDenied,
                onToggle = {},
                onSelectOffset = {},
                onCustomClick = {},
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

@Preview(showBackground = true, name = "Резервний час 09:00")
@Composable
private fun ReminderPanelFallbackTimePreview() {
    ReminderPanelPreview(
        Availability.Available(PreviewStart.withHour(9), usesFallbackTime = true),
        180,
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
