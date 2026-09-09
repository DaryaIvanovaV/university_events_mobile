package com.KSU.EventsParser.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import com.KSU.EventsParser.R
import com.KSU.EventsParser.domain.model.AppError
import com.KSU.EventsParser.ui.theme.AppColors
import com.KSU.EventsParser.ui.theme.AppType
import com.KSU.EventsParser.ui.theme.Dimens

/**
 * Порожній стан у стилі макета: емодзі та пояснення.
 *
 * Обов'язково прокручуваний, хоч вміст і не виходить за екран: інакше
 * PullToRefreshBox не отримує жесту (йому потрібне джерело вкладеної
 * прокрутки), і «потягнути, щоб оновити» переставало б працювати саме тоді,
 * коли воно найпотрібніше — на порожньому списку.
 */
@Composable
fun EmptyState(
    title: String,
    modifier: Modifier = Modifier,
    hint: String? = null,
    emoji: String? = "📭",
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .verticalScroll(rememberScrollState())
            .padding(
                horizontal = Dimens.ScreenPadding,
                vertical = Dimens.EmptyStatePaddingV,
            ),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(Dimens.EmptyStateIconGap),
    ) {
        if (emoji != null) {
            Text(text = emoji, style = AppType.EmptyStateIcon)
        }
        Text(
            text = title,
            style = AppType.EmptyState,
            color = AppColors.TextDisabled,
            textAlign = TextAlign.Center,
        )
        if (hint != null) {
            Text(
                text = hint,
                style = AppType.RowSubtitle,
                color = AppColors.TextDisabled,
                textAlign = TextAlign.Center,
            )
        }
    }
}

@Composable
fun LoadingState(modifier: Modifier = Modifier) {
    Box(
        modifier = modifier.fillMaxSize(),
        contentAlignment = Alignment.Center,
    ) {
        CircularProgressIndicator(color = AppColors.Primary)
    }
}

/**
 * Стан помилки. Текст залежить від класу помилки: «сервер недоступний»
 * і «сервіс недоступний» — різні ситуації з різними порадами.
 */
@Composable
fun ErrorState(
    error: AppError,
    onRetry: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val (titleRes, hintRes) = when (error) {
        AppError.Unreachable ->
            R.string.error_unreachable to R.string.error_unreachable_hint

        AppError.ServiceUnavailable ->
            R.string.error_service_unavailable to R.string.error_service_unavailable_hint

        else ->
            R.string.error_generic to R.string.error_unreachable_hint
    }

    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(
                horizontal = Dimens.ScreenPadding,
                vertical = Dimens.EmptyStatePaddingV,
            ),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(Dimens.EmptyStateIconGap),
    ) {
        Icon(
            imageVector = AppIcons.AlertCircle,
            contentDescription = null,
            tint = AppColors.TextDisabled,
            modifier = Modifier.size(Dimens.EmptyStateIconGap * 4),
        )
        Text(
            text = stringResource(titleRes),
            style = AppType.RowTitle,
            color = AppColors.TextTitle,
            textAlign = TextAlign.Center,
        )
        Text(
            text = stringResource(hintRes),
            style = AppType.EmptyState,
            color = AppColors.TextDisabled,
            textAlign = TextAlign.Center,
        )
        Box(
            modifier = Modifier
                .clip(RoundedCornerShape(Dimens.ChipRadius))
                .background(AppColors.Primary)
                .clickable(onClick = onRetry)
                .padding(
                    horizontal = Dimens.FilterChipPaddingH,
                    vertical = Dimens.FilterChipPaddingV,
                ),
        ) {
            Text(
                text = stringResource(R.string.action_retry),
                style = AppType.ChipFilter,
                color = AppColors.OnPrimary,
            )
        }
    }
}

/** Банер «офлайн» над списком, коли показуються збережені дані. */
@Composable
fun OfflineBanner(modifier: Modifier = Modifier) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .background(AppColors.AccentLight)
            .padding(
                horizontal = Dimens.ScreenPadding,
                vertical = Dimens.FilterChipPaddingV,
            ),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(Dimens.ChipGap),
    ) {
        Icon(
            imageVector = AppIcons.AlertCircle,
            contentDescription = null,
            tint = AppColors.CodeTint,
            modifier = Modifier.size(Dimens.IconSmall),
        )
        Text(
            text = stringResource(R.string.offline_banner),
            style = AppType.RowSubtitle,
            color = AppColors.CodeTint,
        )
    }
}

/** Підпис секції: 11sp, напівжирний, розріджений, великими літерами. */
@Composable
fun SectionLabel(
    text: String,
    modifier: Modifier = Modifier,
) {
    Text(
        text = text.uppercase(),
        style = AppType.SectionLabel,
        color = AppColors.TextLabel,
        modifier = modifier,
    )
}
