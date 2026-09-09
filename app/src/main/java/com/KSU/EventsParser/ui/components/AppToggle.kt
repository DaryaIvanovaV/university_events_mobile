package com.KSU.EventsParser.ui.components

import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import com.KSU.EventsParser.ui.theme.AppColors
import com.KSU.EventsParser.ui.theme.Dimens

/**
 * Перемикач із макета: 44×26, радіус 13, повзунок 20 з відступом 3.
 * Стандартний Switch з Material 3 має іншу геометрію й помітно вибивався б.
 */
@Composable
fun AppToggle(
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
) {
    val track by animateColorAsState(
        targetValue = if (checked) AppColors.Accent else AppColors.ToggleOff,
        label = "toggleTrack",
    )
    val thumbOffset by animateDpAsState(
        targetValue = if (checked) {
            Dimens.ToggleWidth - Dimens.ToggleThumb - Dimens.ToggleThumbInset
        } else {
            Dimens.ToggleThumbInset
        },
        label = "toggleThumb",
    )

    Box(
        modifier = modifier
            .size(width = Dimens.ToggleWidth, height = Dimens.ToggleHeight)
            .clip(RoundedCornerShape(Dimens.ToggleHeight / 2))
            .background(track)
            .clickable(
                enabled = enabled,
                interactionSource = remember { MutableInteractionSource() },
                indication = null,
            ) { onCheckedChange(!checked) },
        contentAlignment = Alignment.CenterStart,
    ) {
        Box(
            modifier = Modifier
                .offset(x = thumbOffset)
                .size(Dimens.ToggleThumb)
                .shadow(elevation = Dimens.CardElevation, shape = CircleShape)
                .clip(CircleShape)
                .background(AppColors.Surface),
        )
    }
}
