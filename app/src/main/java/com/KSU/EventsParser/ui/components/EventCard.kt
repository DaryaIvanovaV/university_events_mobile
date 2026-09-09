package com.KSU.EventsParser.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import com.KSU.EventsParser.R
import com.KSU.EventsParser.domain.model.Event
import com.KSU.EventsParser.ui.theme.AppColors
import com.KSU.EventsParser.ui.theme.AppType
import com.KSU.EventsParser.ui.theme.Dimens
import com.KSU.EventsParser.ui.theme.EventPalette
import com.KSU.EventsParser.ui.util.dateText
import com.KSU.EventsParser.ui.util.label
import com.KSU.EventsParser.ui.util.locationText
import com.KSU.EventsParser.ui.util.timeText

/**
 * Картка події зі списку.
 *
 * Відповідає EventCard із макета: біла картка, скруглення 12, кольорова смуга
 * завширшки 3 ліворуч у колір типу, шеврон праворуч.
 *
 * Час у макеті показувався діапазоном «09:00 — 18:00», але в API немає поля
 * часу завершення, тож вигадувати його не можна — показуємо лише початок.
 * Замість тегів із макета показуємо target_audience: це єдине поле API,
 * близьке за змістом.
 */
@OptIn(ExperimentalLayoutApi::class)
@Composable
fun EventCard(
    event: Event,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    showDate: Boolean = false,
    showLocation: Boolean = false,
) {
    val colors = EventPalette.of(event.type)
    val shape = RoundedCornerShape(Dimens.CardRadius)

    Row(
        modifier = modifier
            .fillMaxWidth()
            .height(IntrinsicSize.Min)
            .shadow(
                elevation = Dimens.CardElevation,
                shape = shape,
                ambientColor = AppColors.CardShadow,
                spotColor = AppColors.CardShadow,
            )
            .clip(shape)
            .background(AppColors.Surface)
            .clickable(onClick = onClick),
    ) {
        // Кольорова смуга ліворуч — borderLeft:"3px solid" у макеті.
        Box(
            modifier = Modifier
                .width(Dimens.CardStripe)
                .fillMaxHeight()
                .background(colors.dot),
        )

        Row(
            modifier = Modifier
                .weight(1f)
                .padding(
                    horizontal = Dimens.CardPaddingH,
                    vertical = Dimens.CardPaddingV,
                ),
            horizontalArrangement = Arrangement.spacedBy(Dimens.CardContentGap),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Column(modifier = Modifier.weight(1f)) {
                if (showDate) {
                    Text(
                        text = event.dateText(),
                        style = AppType.CardDate,
                        color = AppColors.TextMuted,
                    )
                    Box(Modifier.height(Dimens.CardDateGap))
                }

                Text(
                    text = event.title,
                    style = AppType.CardTitle,
                    color = AppColors.TextPrimary,
                    maxLines = 3,
                    overflow = TextOverflow.Ellipsis,
                )

                Box(Modifier.height(Dimens.CardTitleGap))

                FlowRow(
                    horizontalArrangement = Arrangement.spacedBy(Dimens.CardMetaGap),
                    verticalArrangement = Arrangement.spacedBy(Dimens.CardDateGap),
                ) {
                    Text(
                        text = event.timeText(),
                        style = AppType.CardMeta,
                        color = AppColors.TextLabel,
                    )
                    TypeChip(type = event.type)
                }

                if (showLocation) {
                    Box(Modifier.height(Dimens.CardPlaceGap))
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(Dimens.CardDateGap),
                    ) {
                        Icon(
                            imageVector = AppIcons.Pin,
                            contentDescription = null,
                            tint = AppColors.TextMuted,
                            modifier = Modifier.size(Dimens.IconTiny),
                        )
                        Text(
                            text = event.locationText(),
                            style = AppType.CardMeta,
                            color = AppColors.TextMuted,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                        )
                    }
                }
            }

            Icon(
                imageVector = AppIcons.ChevronRight,
                contentDescription = stringResource(R.string.cd_open_event),
                tint = AppColors.Chevron,
                modifier = Modifier.size(Dimens.IconChevronCard),
            )
        }
    }
}

/** Чип типу події: padding "1px 7px", радіус 20, кольори з палітри типу. */
@Composable
fun TypeChip(
    type: com.KSU.EventsParser.domain.model.EventType,
    modifier: Modifier = Modifier,
) {
    val colors = EventPalette.of(type)
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(Dimens.ChipRadius))
            .background(colors.bg)
            .padding(
                horizontal = Dimens.ChipPaddingH,
                vertical = Dimens.ChipPaddingV,
            ),
    ) {
        Text(
            text = type.label(),
            style = AppType.ChipSmall,
            color = colors.text,
        )
    }
}
