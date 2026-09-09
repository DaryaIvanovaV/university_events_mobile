package com.KSU.EventsParser.ui.components

import androidx.compose.foundation.layout.BoxScope
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.material3.pulltorefresh.PullToRefreshDefaults
import androidx.compose.material3.pulltorefresh.rememberPullToRefreshState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.KSU.EventsParser.ui.theme.AppColors

/**
 * Обгортка «потягнути, щоб оновити».
 *
 * Винесена окремо, щоб OptIn на експериментальний API Material 3 був в одному
 * місці, а не в кожному екрані, і щоб індикатор скрізь мав кольори теми.
 *
 * Оновлення тут означає синхронізацію кешу (`/events/sync`), а не перезавантаження
 * стрічки: дані на екрані оновляться самі, коли Room віддасть новий знімок.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RefreshableBox(
    isRefreshing: Boolean,
    onRefresh: () -> Unit,
    modifier: Modifier = Modifier,
    content: @Composable BoxScope.() -> Unit,
) {
    val state = rememberPullToRefreshState()
    PullToRefreshBox(
        isRefreshing = isRefreshing,
        onRefresh = onRefresh,
        modifier = modifier,
        state = state,
        indicator = {
            PullToRefreshDefaults.Indicator(
                state = state,
                isRefreshing = isRefreshing,
                modifier = Modifier.align(androidx.compose.ui.Alignment.TopCenter),
                containerColor = AppColors.Surface,
                color = AppColors.Primary,
            )
        },
        content = content,
    )
}
