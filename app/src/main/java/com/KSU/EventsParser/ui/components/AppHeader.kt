package com.KSU.EventsParser.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import com.KSU.EventsParser.R
import com.KSU.EventsParser.ui.theme.AppColors
import com.KSU.EventsParser.ui.theme.AppType
import com.KSU.EventsParser.ui.theme.Dimens

/**
 * Шапка екрана: темно-синє тло, надрядковий напис «ХДУ» та заголовок
 * серифним шрифтом. Відповідає Header із макета.
 *
 * Надрядковий напис у макеті був «ФКН · МГУ» — назва російського університету.
 * Замінено на «ХДУ» (в англійській локалі — «KSU»).
 */
@Composable
fun AppHeader(
    title: String,
    modifier: Modifier = Modifier,
    actions: @Composable RowScope.() -> Unit = {},
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .background(AppColors.HeaderBg)
            .statusBarsPadding()
            .padding(
                start = Dimens.ScreenPadding,
                end = Dimens.ScreenPadding,
                top = Dimens.HeaderPaddingTop,
                bottom = Dimens.HeaderPaddingBottom,
            ),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween,
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = stringResource(R.string.header_overline).uppercase(),
                style = AppType.HeaderOverline,
                color = AppColors.HeaderOverline,
            )
            Box(Modifier.height(Dimens.HeaderOverlineGap))
            Text(
                text = title,
                style = AppType.HeaderTitle,
                color = AppColors.OnPrimary,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis,
            )
        }
        Row(horizontalArrangement = Arrangement.spacedBy(Dimens.HeaderButtonGap)) {
            actions()
        }
    }
}

/** Квадратна кнопка «‹» / «›» у шапці: 32×32, радіус 8, напівпрозоре тло. */
@Composable
fun HeaderNavButton(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    contentDescription: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Box(
        modifier = modifier
            .size(Dimens.HeaderButtonSize)
            .clip(RoundedCornerShape(Dimens.HeaderButtonRadius))
            .background(AppColors.HeaderButtonBg)
            .clickable(onClick = onClick),
        contentAlignment = Alignment.Center,
    ) {
        Icon(
            imageVector = icon,
            contentDescription = contentDescription,
            tint = AppColors.OnPrimary,
            modifier = Modifier.size(Dimens.IconHeader),
        )
    }
}
