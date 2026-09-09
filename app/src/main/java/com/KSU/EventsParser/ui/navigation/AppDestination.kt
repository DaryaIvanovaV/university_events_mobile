package com.KSU.EventsParser.ui.navigation

import androidx.annotation.StringRes
import androidx.compose.ui.graphics.vector.ImageVector
import com.KSU.EventsParser.R
import com.KSU.EventsParser.ui.components.AppIcons

/**
 * Маршрути застосунку.
 *
 * П'ята вкладка в макеті називалася «Alerts» і вела на список нагадувань
 * по кожній події. Такої функції немає й не буде: сервер шле рівно один push
 * у момент підтвердження події, планувальника нагадувань не існує. Тому вкладку
 * замінено на «Налаштування» — там живе глобальний перемикач push,
 * вибір мови та фільтр за аудиторією.
 */
object Routes {
    const val MONTH = "month"
    const val WEEK = "week"
    const val DAY = "day"
    const val LIST = "list"
    const val SETTINGS = "settings"

    const val DETAIL_ARG_ID = "eventId"
    const val DETAIL = "detail/{$DETAIL_ARG_ID}"

    fun detail(eventId: Int): String = "detail/$eventId"
}

enum class BottomTab(
    val route: String,
    @param:StringRes val labelRes: Int,
    val icon: ImageVector,
) {
    MONTH(Routes.MONTH, R.string.nav_month, AppIcons.Calendar),
    WEEK(Routes.WEEK, R.string.nav_week, AppIcons.Week),
    DAY(Routes.DAY, R.string.nav_day, AppIcons.Grid),
    LIST(Routes.LIST, R.string.nav_list, AppIcons.List),
    SETTINGS(Routes.SETTINGS, R.string.nav_settings, AppIcons.Settings),
}
