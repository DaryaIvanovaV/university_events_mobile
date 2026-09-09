package com.KSU.EventsParser.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import com.KSU.EventsParser.R
import com.KSU.EventsParser.core.util.AppDateFormat
import com.KSU.EventsParser.domain.model.Event
import com.KSU.EventsParser.ui.components.AppHeader
import com.KSU.EventsParser.ui.components.AppIcons
import com.KSU.EventsParser.ui.components.EmptyState
import com.KSU.EventsParser.ui.components.EventCard
import com.KSU.EventsParser.ui.components.OfflineBanner
import com.KSU.EventsParser.ui.components.RefreshableBox
import com.KSU.EventsParser.ui.theme.AppColors
import com.KSU.EventsParser.ui.theme.AppType
import com.KSU.EventsParser.ui.theme.Dimens
import com.KSU.EventsParser.ui.util.rememberAppLocale
import java.time.LocalDate

/**
 * Вкладка «День»: події одного дня з перемиканням «‹ дата ›».
 */
@Composable
fun DayScreen(
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
    val dayEvents = remember(events, selectedDate) {
        events.filter { it.date == selectedDate }
    }

    Column(modifier = modifier.fillMaxSize().background(AppColors.Background)) {
        AppHeader(title = AppDateFormat.dayMonth(selectedDate, locale))
        if (isOffline) OfflineBanner()

        // Панель навігації по днях — біла смуга під шапкою.
        Column(modifier = Modifier.fillMaxWidth().background(AppColors.Surface)) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(
                        horizontal = Dimens.ScreenPadding,
                        vertical = Dimens.CardPaddingV,
                    ),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
            ) {
                DayNavButton(
                    icon = AppIcons.ChevronLeft,
                    contentDescription = stringResource(R.string.cd_previous),
                    onClick = { onSelectDate(selectedDate.minusDays(1)) },
                )
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = AppDateFormat.dayMonth(selectedDate, locale),
                        style = AppType.DayViewDate,
                        color = AppColors.TextTitle,
                    )
                    Text(
                        text = AppDateFormat.weekdayShort(selectedDate.dayOfWeek, locale),
                        style = AppType.DayViewWeekday,
                        color = AppColors.TextLabel,
                    )
                }
                DayNavButton(
                    icon = AppIcons.ChevronRight,
                    contentDescription = stringResource(R.string.cd_next),
                    onClick = { onSelectDate(selectedDate.plusDays(1)) },
                )
            }
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(Dimens.Hairline)
                    .background(AppColors.Divider),
            )
        }

        RefreshableBox(
            isRefreshing = isRefreshing,
            onRefresh = onRefresh,
            modifier = Modifier.fillMaxSize(),
        ) {
            if (dayEvents.isEmpty()) {
                EmptyState(
                    title = stringResource(R.string.empty_day_long),
                    modifier = Modifier.fillMaxWidth(),
                )
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(
                        top = Dimens.ScreenPaddingSmall,
                        bottom = Dimens.ListBottomPadding,
                    ),
                ) {
                    items(dayEvents, key = { it.id }) { event ->
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
        }
    }
}

@Composable
private fun DayNavButton(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    contentDescription: String,
    onClick: () -> Unit,
) {
    Box(
        modifier = Modifier
            .clickable(onClick = onClick)
            .padding(Dimens.CardPlaceGap),
    ) {
        Icon(
            imageVector = icon,
            contentDescription = contentDescription,
            tint = AppColors.TextIcon,
            modifier = Modifier.size(Dimens.IconDayNav),
        )
    }
}
