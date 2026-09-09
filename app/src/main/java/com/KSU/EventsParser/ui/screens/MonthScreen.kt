package com.KSU.EventsParser.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringArrayResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import com.KSU.EventsParser.R
import com.KSU.EventsParser.core.util.AppDateFormat
import com.KSU.EventsParser.domain.model.Event
import com.KSU.EventsParser.ui.components.AppHeader
import com.KSU.EventsParser.ui.components.AppIcons
import com.KSU.EventsParser.ui.components.EmptyState
import com.KSU.EventsParser.ui.components.EventCard
import com.KSU.EventsParser.ui.components.HeaderNavButton
import com.KSU.EventsParser.ui.components.OfflineBanner
import com.KSU.EventsParser.ui.components.RefreshableBox
import com.KSU.EventsParser.ui.components.SectionLabel
import com.KSU.EventsParser.ui.theme.AppColors
import com.KSU.EventsParser.ui.theme.AppType
import com.KSU.EventsParser.ui.theme.Dimens
import com.KSU.EventsParser.ui.theme.EventPalette
import com.KSU.EventsParser.ui.util.rememberAppLocale
import com.kizitonwose.calendar.compose.HorizontalCalendar
import com.kizitonwose.calendar.compose.rememberCalendarState
import com.kizitonwose.calendar.core.CalendarDay
import com.kizitonwose.calendar.core.DayPosition
import kotlinx.coroutines.launch
import java.time.DayOfWeek
import java.time.LocalDate
import java.time.YearMonth

/**
 * Вкладка «Місяць»: сітка з крапками подій і список подій обраного дня.
 *
 * Тиждень починається з понеділка (DayOfWeek.MONDAY), підписи днів —
 * з ресурсів, тож у кожній локалі свої. У макеті сітка починалася з неділі
 * (DAYS_RU = ["Вс","Пн",...] і getDay()), що для української хибно.
 *
 * Події без дати сюди не потрапляють за визначенням — вони доступні
 * на вкладці «Події» в секції «Без дати».
 */
@Composable
fun MonthScreen(
    events: List<Event>,
    selectedDate: LocalDate,
    onSelectDate: (LocalDate) -> Unit,
    onEventClick: (Event) -> Unit,
    modifier: Modifier = Modifier,
    isOffline: Boolean = false,
    isRefreshing: Boolean = false,
    onRefresh: () -> Unit = {},
) {
    val locale = rememberAppLocale()
    val today = remember { LocalDate.now() }
    val currentMonth = remember { YearMonth.now() }

    val calendarState = rememberCalendarState(
        startMonth = currentMonth.minusMonths(MONTHS_BACK),
        endMonth = currentMonth.plusMonths(MONTHS_FORWARD),
        firstVisibleMonth = currentMonth,
        firstDayOfWeek = DayOfWeek.MONDAY,
    )
    val scope = rememberCoroutineScope()
    val visibleMonth = calendarState.firstVisibleMonth.yearMonth

    // Індекс «дата → події» будуємо один раз на зміну списку, а не в кожній комірці.
    val eventsByDate = remember(events) {
        events.filter { it.date != null }.groupBy { it.date!! }
    }
    val selectedDayEvents = eventsByDate[selectedDate].orEmpty()

    Column(modifier = modifier.fillMaxSize().background(AppColors.Background)) {
        AppHeader(title = AppDateFormat.monthYear(visibleMonth.atDay(1), locale)) {
            HeaderNavButton(
                icon = AppIcons.ChevronLeft,
                contentDescription = stringResource(R.string.cd_previous),
                onClick = {
                    scope.launch {
                        calendarState.animateScrollToMonth(visibleMonth.minusMonths(1))
                    }
                },
            )
            HeaderNavButton(
                icon = AppIcons.ChevronRight,
                contentDescription = stringResource(R.string.cd_next),
                onClick = {
                    scope.launch {
                        calendarState.animateScrollToMonth(visibleMonth.plusMonths(1))
                    }
                },
            )
        }

        if (isOffline) OfflineBanner()

        Column(modifier = Modifier.background(AppColors.Surface)) {
            WeekdayHeader()
            HorizontalCalendar(
                state = calendarState,
                modifier = Modifier.padding(
                    horizontal = Dimens.MonthGridPaddingH,
                    vertical = Dimens.MonthGridPaddingV,
                ),
                dayContent = { day ->
                    DayCell(
                        day = day,
                        isSelected = day.date == selectedDate,
                        isToday = day.date == today,
                        dayEvents = eventsByDate[day.date].orEmpty(),
                        onClick = { onSelectDate(day.date) },
                    )
                },
            )
        }

        RefreshableBox(
            isRefreshing = isRefreshing,
            onRefresh = onRefresh,
            modifier = Modifier.fillMaxSize(),
        ) {
            SelectedDayEvents(
                date = selectedDate,
                events = selectedDayEvents,
                onEventClick = onEventClick,
                modifier = Modifier.fillMaxSize(),
            )
        }
    }
}

/** Рядок підписів днів тижня, з понеділка. */
@Composable
private fun WeekdayHeader(modifier: Modifier = Modifier) {
    val labels = stringArrayResource(R.array.weekday_initials)
    Column(modifier = modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(
                    start = Dimens.WeekdayRowPaddingH,
                    end = Dimens.WeekdayRowPaddingH,
                    top = Dimens.WeekdayRowPaddingTop,
                    bottom = Dimens.WeekdayRowPaddingBottom,
                ),
        ) {
            labels.forEach { label ->
                Text(
                    text = label,
                    style = AppType.Weekday,
                    color = AppColors.TextLabel,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.weight(1f),
                )
            }
        }
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(Dimens.Hairline)
                .background(AppColors.GridBorder),
        )
    }
}

/** Комірка дня: кружечок із числом і до трьох крапок під ним. */
@Composable
private fun DayCell(
    day: CalendarDay,
    isSelected: Boolean,
    isToday: Boolean,
    dayEvents: List<Event>,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    // Дні сусідніх місяців у макеті лишалися порожніми.
    if (day.position != DayPosition.MonthDate) {
        Box(modifier = modifier.size(Dimens.DayCircle))
        return
    }

    val background = when {
        isSelected -> AppColors.Primary
        isToday -> AppColors.Accent
        else -> Color.Transparent
    }
    val textColor = if (isSelected || isToday) AppColors.OnPrimary else AppColors.TextDay

    Column(
        modifier = modifier
            .fillMaxWidth()
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null,
                onClick = onClick,
            )
            .padding(vertical = Dimens.DayCellPadding),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Box(
            modifier = Modifier
                .size(Dimens.DayCircle)
                .clip(CircleShape)
                .background(background),
            contentAlignment = Alignment.Center,
        ) {
            Text(
                text = day.date.dayOfMonth.toString(),
                style = if (isToday) AppType.DayNumberToday else AppType.DayNumber,
                color = textColor,
            )
        }
        Row(
            modifier = Modifier
                .padding(top = Dimens.EventDotRowTopGap)
                .height(Dimens.EventDotRowHeight),
            horizontalArrangement = Arrangement.spacedBy(Dimens.EventDotGap),
        ) {
            dayEvents.take(Dimens.MaxDotsPerDay).forEach { event ->
                Box(
                    modifier = Modifier
                        .size(Dimens.EventDot)
                        .clip(CircleShape)
                        .background(EventPalette.of(event.type).dot),
                )
            }
        }
    }
}

/** Список подій обраного дня під сіткою. */
@Composable
private fun SelectedDayEvents(
    date: LocalDate,
    events: List<Event>,
    onEventClick: (Event) -> Unit,
    modifier: Modifier = Modifier,
) {
    val locale = rememberAppLocale()
    if (events.isEmpty()) {
        // verticalScroll обов'язковий: без нього PullToRefreshBox не отримає
        // жесту й «потягнути, щоб оновити» не спрацює на порожньому дні.
        Column(
            modifier = modifier
                .fillMaxWidth()
                .verticalScroll(rememberScrollState()),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Text(
                text = stringResource(R.string.empty_day),
                style = AppType.EmptyState,
                color = AppColors.TextDisabled,
                modifier = Modifier.padding(vertical = Dimens.EmptyDayPaddingV),
            )
        }
        return
    }

    LazyColumn(
        modifier = modifier,
        contentPadding = PaddingValues(
            top = Dimens.SelectedDayListPaddingV,
            bottom = Dimens.ListBottomPadding,
        ),
    ) {
        item(key = "selected-day-label") {
            SectionLabel(
                text = AppDateFormat.dayMonth(date, locale),
                modifier = Modifier.padding(
                    horizontal = Dimens.ScreenPadding,
                    vertical = Dimens.CardDateGap,
                ),
            )
        }
        items(events, key = { it.id }) { event ->
            EventCard(
                event = event,
                onClick = { onEventClick(event) },
                showLocation = true,
                modifier = Modifier.padding(
                    horizontal = Dimens.ScreenPadding,
                    vertical = Dimens.CardGap / 2,
                ),
            )
        }
    }
}

private const val MONTHS_BACK = 12L
private const val MONTHS_FORWARD = 24L
