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
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material3.Icon
import androidx.compose.material3.LocalTextStyle
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.res.stringResource
import com.KSU.EventsParser.R
import com.KSU.EventsParser.core.util.AppDateFormat
import com.KSU.EventsParser.domain.model.Event
import com.KSU.EventsParser.domain.model.EventType
import com.KSU.EventsParser.ui.components.AppHeader
import com.KSU.EventsParser.ui.components.AppIcons
import com.KSU.EventsParser.ui.components.EmptyState
import com.KSU.EventsParser.ui.components.EventCard
import com.KSU.EventsParser.ui.components.OfflineBanner
import com.KSU.EventsParser.ui.components.RefreshableBox
import com.KSU.EventsParser.ui.components.SectionLabel
import com.KSU.EventsParser.ui.theme.AppColors
import com.KSU.EventsParser.ui.theme.AppType
import com.KSU.EventsParser.ui.theme.Dimens
import com.KSU.EventsParser.ui.util.label
import com.KSU.EventsParser.ui.util.matchesAudience
import com.KSU.EventsParser.ui.util.rememberAppLocale
import java.time.YearMonth

/**
 * Вкладка «Події»: усі події, згруповані за місяцями.
 *
 * ЄДИНИЙ екран, який показує події без дати. У сітку календаря вони потрапити
 * не можуть за визначенням, а серверні фільтри upcoming/date_from/date_to
 * мовчки відкидають записи з date == null, тому їх зібрано в окрему секцію
 * «Без дати» в кінці списку з поясненням, чому дати немає.
 *
 * Фільтр за аудиторією діє саме тут, а не глобально: інакше на вкладці
 * «Місяць» тихо зникали б крапки, і користувач не бачив би причини.
 */
@Composable
fun ListScreen(
    events: List<Event>,
    onEventClick: (Event) -> Unit,
    modifier: Modifier = Modifier,
    isOffline: Boolean = false,
    isRefreshing: Boolean = false,
    onRefresh: () -> Unit = {},
    audienceFilter: String = "",
    onAudienceFilterChange: (String) -> Unit = {},
) {
    var selectedType by rememberSaveable { mutableStateOf<EventType?>(null) }
    val locale = rememberAppLocale()

    // Показуємо лише ті типи, які реально є в даних, — інакше смуга фільтрів
    // з десяти чипів була б переважно порожньою.
    val presentTypes = remember(events) {
        events.map { it.type }.distinct().sortedBy { it.ordinal }
    }

    val filtered = remember(events, selectedType, audienceFilter) {
        events
            .filter { selectedType == null || it.type == selectedType }
            .filter { it.matchesAudience(audienceFilter) }
    }

    val dated = remember(filtered) {
        filtered.filter { it.date != null }
            .groupBy { YearMonth.from(it.date) }
            .toSortedMap()
    }
    val undated = remember(filtered) { filtered.filter { it.date == null } }

    Column(modifier = modifier.fillMaxSize().background(AppColors.Background)) {
        AppHeader(title = stringResource(R.string.title_list))
        if (isOffline) OfflineBanner()

        FilterBar(
            types = presentTypes,
            selected = selectedType,
            onSelect = { selectedType = it },
            audienceFilter = audienceFilter,
            onAudienceFilterChange = onAudienceFilterChange,
        )

        RefreshableBox(
            isRefreshing = isRefreshing,
            onRefresh = onRefresh,
            modifier = Modifier.fillMaxSize(),
        ) {
            if (filtered.isEmpty()) {
                EmptyState(
                    title = stringResource(
                        if (events.isEmpty()) R.string.empty_feed else R.string.empty_filtered
                    ),
                    hint = stringResource(
                        if (events.isEmpty()) {
                            R.string.empty_feed_hint
                        } else {
                            R.string.empty_filtered_hint
                        },
                    ),
                    modifier = Modifier.fillMaxWidth(),
                )
                return@RefreshableBox
            }

            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(bottom = Dimens.ListBottomPadding),
            ) {
                dated.forEach { (month, monthEvents) ->
                    item(key = "month-$month") {
                        SectionLabel(
                            text = AppDateFormat.monthYear(month.atDay(1), locale),
                            modifier = Modifier.padding(
                                start = Dimens.ScreenPadding,
                                end = Dimens.ScreenPadding,
                                top = Dimens.ListSectionPaddingTop,
                                bottom = Dimens.ListSectionPaddingBottom,
                            ),
                        )
                    }
                    items(monthEvents, key = { it.id }) { event ->
                        EventCard(
                            event = event,
                            onClick = { onEventClick(event) },
                            showDate = true,
                            modifier = Modifier.padding(
                                horizontal = Dimens.ScreenPadding,
                                vertical = Dimens.CardGap / 2,
                            ),
                        )
                    }
                }

                if (undated.isNotEmpty()) {
                    item(key = "no-date-header") {
                        Column(
                            modifier = Modifier.padding(
                                start = Dimens.ScreenPadding,
                                end = Dimens.ScreenPadding,
                                top = Dimens.ListSectionPaddingTop,
                                bottom = Dimens.ListSectionPaddingBottom,
                            ),
                        ) {
                            SectionLabel(text = stringResource(R.string.section_no_date))
                            Box(Modifier.height(Dimens.CardDateGap))
                            Text(
                                text = stringResource(R.string.section_no_date_hint),
                                style = AppType.RowSubtitle,
                                color = AppColors.TextDisabled,
                            )
                        }
                    }
                    items(undated, key = { it.id }) { event ->
                        EventCard(
                            event = event,
                            onClick = { onEventClick(event) },
                            showDate = false,
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

/** Поле фільтра за аудиторією та смуга чипів за типом події. */
@Composable
private fun FilterBar(
    types: List<EventType>,
    selected: EventType?,
    onSelect: (EventType?) -> Unit,
    audienceFilter: String,
    onAudienceFilterChange: (String) -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(modifier = modifier.fillMaxWidth().background(AppColors.Surface)) {
        AudienceFilterField(
            value = audienceFilter,
            onValueChange = onAudienceFilterChange,
            modifier = Modifier.padding(
                start = Dimens.ScreenPadding,
                end = Dimens.ScreenPadding,
                top = Dimens.FilterBarPaddingV,
            ),
        )

        LazyRow(
            contentPadding = PaddingValues(
                horizontal = Dimens.ScreenPadding,
                vertical = Dimens.FilterBarPaddingV,
            ),
            horizontalArrangement = Arrangement.spacedBy(Dimens.ChipGap),
        ) {
            item(key = "all") {
                FilterChip(
                    text = stringResource(R.string.filter_all),
                    selected = selected == null,
                    onClick = { onSelect(null) },
                )
            }
            items(types, key = { it.name }) { type ->
                FilterChip(
                    text = type.label(),
                    selected = selected == type,
                    onClick = { onSelect(type) },
                )
            }
        }

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(Dimens.Hairline)
                .background(AppColors.Divider),
        )
    }
}

/**
 * Поле фільтра за `target_audience`.
 *
 * Поле в API — ВІЛЬНИЙ ТЕКСТ («2 та 3 курси, усі охочі», «група КН-41»),
 * а не перелік, тому це підрядковий пошук без урахування регістру,
 * а не вибір зі списку. Персональних підписок в API немає взагалі —
 * фільтрувати можна лише на клієнті.
 */
@Composable
private fun AudienceFilterField(
    value: String,
    onValueChange: (String) -> Unit,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(Dimens.ChipRadius))
            .background(AppColors.ChipIdleBg)
            .padding(
                horizontal = Dimens.FilterChipPaddingH,
                vertical = Dimens.FilterChipPaddingV,
            ),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(Dimens.ChipGap),
    ) {
        Icon(
            imageVector = AppIcons.Users,
            contentDescription = null,
            tint = AppColors.TextLabel,
            modifier = Modifier.size(Dimens.IconSmall),
        )
        Box(modifier = Modifier.weight(1f)) {
            if (value.isEmpty()) {
                Text(
                    text = stringResource(R.string.settings_audience_hint),
                    style = AppType.ChipFilter,
                    color = AppColors.TextDisabled,
                )
            }
            BasicTextField(
                value = value,
                onValueChange = onValueChange,
                singleLine = true,
                textStyle = LocalTextStyle.current.merge(
                    AppType.ChipFilter.copy(color = AppColors.TextChipIdle),
                ),
                cursorBrush = SolidColor(AppColors.Primary),
                modifier = Modifier.fillMaxWidth(),
            )
        }
        if (value.isNotEmpty()) {
            Icon(
                imageVector = AppIcons.Close,
                contentDescription = stringResource(R.string.settings_audience_clear),
                tint = AppColors.TextLabel,
                modifier = Modifier
                    .size(Dimens.IconSmall)
                    .clickable { onValueChange("") },
            )
        }
    }
}

@Composable
private fun FilterChip(
    text: String,
    selected: Boolean,
    onClick: () -> Unit,
) {
    Row(
        modifier = Modifier
            .clip(RoundedCornerShape(Dimens.ChipRadius))
            .background(if (selected) AppColors.Primary else AppColors.ChipIdleBg)
            .clickable(onClick = onClick)
            .padding(
                horizontal = Dimens.FilterChipPaddingH,
                vertical = Dimens.FilterChipPaddingV,
            ),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(
            text = text,
            style = AppType.ChipFilter,
            color = if (selected) AppColors.OnPrimary else AppColors.TextChipIdle,
        )
    }
}
