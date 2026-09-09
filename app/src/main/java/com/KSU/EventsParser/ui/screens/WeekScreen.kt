package com.KSU.EventsParser.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringArrayResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import com.KSU.EventsParser.R
import com.KSU.EventsParser.domain.model.Event
import com.KSU.EventsParser.ui.components.AppHeader
import com.KSU.EventsParser.ui.components.AppIcons
import com.KSU.EventsParser.ui.components.HeaderNavButton
import com.KSU.EventsParser.ui.components.OfflineBanner
import com.KSU.EventsParser.ui.components.RefreshableBox
import com.KSU.EventsParser.ui.theme.AppColors
import com.KSU.EventsParser.ui.theme.AppType
import com.KSU.EventsParser.ui.theme.Dimens
import com.KSU.EventsParser.ui.theme.EventPalette
import com.KSU.EventsParser.ui.util.timeText
import java.time.DayOfWeek
import java.time.LocalDate
import java.time.temporal.TemporalAdjusters

/**
 * Вкладка «Тиждень»: сім колонок із плитками подій.
 *
 * Тиждень рахується від понеділка. У макеті для цього був вираз
 * `(getDay()+6)%7`, який виправляв неділю-перший; тут це робить
 * TemporalAdjusters.previousOrSame(MONDAY) — явно й без арифметики.
 */
@Composable
fun WeekScreen(
    events: List<Event>,
    onEventClick: (Event) -> Unit,
    modifier: Modifier = Modifier,
    isOffline: Boolean = false,
    isRefreshing: Boolean = false,
    onRefresh: () -> Unit = {},
) {
    val today = remember { LocalDate.now() }
    var weekStart by remember {
        mutableStateOf(today.with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY)))
    }
    val days = remember(weekStart) { (0L until 7L).map { weekStart.plusDays(it) } }
    val eventsByDate = remember(events) {
        events.filter { it.date != null }.groupBy { it.date!! }
    }
    val weekdayLabels = stringArrayResource(R.array.weekday_initials)

    Column(modifier = modifier.fillMaxSize().background(AppColors.Surface)) {
        AppHeader(title = stringResource(R.string.title_week)) {
            HeaderNavButton(
                icon = AppIcons.ChevronLeft,
                contentDescription = stringResource(R.string.cd_previous),
                onClick = { weekStart = weekStart.minusWeeks(1) },
            )
            HeaderNavButton(
                icon = AppIcons.ChevronRight,
                contentDescription = stringResource(R.string.cd_next),
                onClick = { weekStart = weekStart.plusWeeks(1) },
            )
        }

        if (isOffline) OfflineBanner()

        // Смуга з днями тижня та числами.
        Row(modifier = Modifier.fillMaxWidth().height(IntrinsicSize.Min)) {
            days.forEachIndexed { index, date ->
                Column(
                    modifier = Modifier
                        .weight(1f)
                        .padding(
                            top = Dimens.WeekHeaderPaddingTop,
                            bottom = Dimens.WeekHeaderPaddingBottom,
                            start = Dimens.WeekHeaderPaddingH,
                            end = Dimens.WeekHeaderPaddingH,
                        ),
                    horizontalAlignment = Alignment.CenterHorizontally,
                ) {
                    Text(
                        text = weekdayLabels[index].uppercase(),
                        style = AppType.WeekStripWeekday,
                        color = AppColors.TextMuted,
                        textAlign = TextAlign.Center,
                    )
                    Box(
                        modifier = Modifier
                            .padding(top = Dimens.EventDotRowTopGap)
                            .size(Dimens.WeekDayCircle)
                            .clip(CircleShape)
                            .background(
                                if (date == today) AppColors.Accent else Color.Transparent,
                            ),
                        contentAlignment = Alignment.Center,
                    ) {
                        Text(
                            text = date.dayOfMonth.toString(),
                            style = if (date == today) {
                                AppType.DayNumberToday
                            } else {
                                AppType.DayNumber
                            },
                            color = if (date == today) {
                                AppColors.OnPrimary
                            } else {
                                AppColors.TextDay
                            },
                        )
                    }
                }
                if (index < 6) VerticalHairline()
            }
        }
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(Dimens.Hairline)
                .background(AppColors.Divider),
        )

        // Колонки подій.
        RefreshableBox(
            isRefreshing = isRefreshing,
            onRefresh = onRefresh,
            modifier = Modifier.fillMaxSize(),
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState())
                    .heightIn(min = Dimens.WeekMinHeight)
                    .height(IntrinsicSize.Max),
            ) {
                days.forEachIndexed { index, date ->
                    Column(
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxHeight()
                            .padding(
                                vertical = Dimens.WeekColumnPaddingV,
                                horizontal = Dimens.WeekColumnPaddingH,
                            ),
                        verticalArrangement = Arrangement.spacedBy(Dimens.WeekColumnGap),
                    ) {
                        eventsByDate[date].orEmpty().forEach { event ->
                            WeekEventTile(event = event, onClick = { onEventClick(event) })
                        }
                    }
                    if (index < 6) VerticalHairline()
                }
            }
        }
    }
}

@Composable
private fun VerticalHairline() {
    Box(
        modifier = Modifier
            .width(Dimens.Hairline)
            .fillMaxHeight()
            .background(AppColors.Divider),
    )
}

/** Плитка події в колонці дня: маленька, з назвою у два рядки й часом. */
@Composable
private fun WeekEventTile(
    event: Event,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val colors = EventPalette.of(event.type)
    Column(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(Dimens.WeekEventRadius))
            .background(colors.bg)
            .clickable(onClick = onClick)
            .padding(
                vertical = Dimens.WeekEventPaddingV,
                horizontal = Dimens.WeekEventPaddingH,
            ),
    ) {
        Text(
            text = event.title,
            style = AppType.WeekEventTitle,
            color = colors.text,
            maxLines = 2,
            overflow = TextOverflow.Ellipsis,
        )
        Text(
            text = event.timeText(),
            style = AppType.WeekEventTime,
            color = colors.text,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
        )
    }
}
