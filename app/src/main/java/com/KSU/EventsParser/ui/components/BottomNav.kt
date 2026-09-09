package com.KSU.EventsParser.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import com.KSU.EventsParser.ui.navigation.BottomTab
import com.KSU.EventsParser.ui.theme.AppColors
import com.KSU.EventsParser.ui.theme.AppType
import com.KSU.EventsParser.ui.theme.Dimens

/**
 * Нижня навігація з макета: біле тло, тонка верхня межа, п'ять рівних вкладок.
 * Крапку-індикатор із макета прибрано разом із екраном нагадувань — вона
 * позначала непрочитані нагадування, яких у застосунку немає.
 */
@Composable
fun BottomNav(
    currentRoute: String?,
    onTabSelected: (BottomTab) -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(modifier = modifier.fillMaxWidth()) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(Dimens.Hairline)
                .background(AppColors.GridBorder),
        )
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(AppColors.Surface)
                .navigationBarsPadding(),
        ) {
            BottomTab.entries.forEach { tab ->
                BottomNavItem(
                    tab = tab,
                    selected = currentRoute == tab.route,
                    onClick = { onTabSelected(tab) },
                    modifier = Modifier.weight(1f),
                )
            }
        }
    }
}

@Composable
private fun BottomNavItem(
    tab: BottomTab,
    selected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val tint = if (selected) AppColors.Primary else AppColors.TextNavIdle
    Column(
        modifier = modifier
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null,
                onClick = onClick,
            )
            .padding(
                top = Dimens.NavPaddingTop,
                bottom = Dimens.NavPaddingBottom,
                start = Dimens.NavPaddingH,
                end = Dimens.NavPaddingH,
            ),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(Dimens.NavItemGap),
    ) {
        Icon(
            imageVector = tab.icon,
            contentDescription = null,
            tint = tint,
            modifier = Modifier.size(Dimens.NavIcon),
        )
        Text(
            text = stringResource(tab.labelRes),
            style = if (selected) AppType.NavLabelActive else AppType.NavLabel,
            color = tint,
        )
    }
}
