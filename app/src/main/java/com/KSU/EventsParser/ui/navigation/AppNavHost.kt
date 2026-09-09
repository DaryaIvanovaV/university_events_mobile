package com.KSU.EventsParser.ui.navigation

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.KSU.EventsParser.data.repository.DeviceRepository
import com.KSU.EventsParser.domain.model.Event
import com.KSU.EventsParser.ui.EventsUiState
import com.KSU.EventsParser.ui.EventsViewModel
import com.KSU.EventsParser.ui.components.BottomNav
import com.KSU.EventsParser.ui.components.ErrorState
import com.KSU.EventsParser.ui.components.LoadingState
import com.KSU.EventsParser.ui.screens.DayScreen
import com.KSU.EventsParser.ui.screens.DetailScreen
import com.KSU.EventsParser.ui.screens.ListScreen
import com.KSU.EventsParser.ui.screens.MonthScreen
import com.KSU.EventsParser.ui.screens.SettingsScreen
import com.KSU.EventsParser.ui.screens.WeekScreen
import com.KSU.EventsParser.ui.theme.AppColors

/**
 * Навігація застосунку: п'ять вкладок і екран деталей поверх них.
 *
 * Нижня панель ховається на екрані деталей — так само, як у макеті,
 * де деталі показувалися замість усього вмісту разом із навігацією.
 */
@Composable
fun AppNavHost(
    viewModel: EventsViewModel,
    deviceRepository: DeviceRepository,
    modifier: Modifier = Modifier,
    deepLinkEventId: Int? = null,
    onDeepLinkHandled: () -> Unit = {},
) {
    val navController = rememberNavController()
    val backStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = backStackEntry?.destination?.route

    val state by viewModel.state.collectAsState()
    val selectedDate by viewModel.selectedDate.collectAsState()
    val audienceFilter by viewModel.audienceFilter.collectAsState()

    val showBottomNav = currentRoute != Routes.DETAIL

    val openDetail: (Event) -> Unit = { event ->
        navController.navigate(Routes.detail(event.id))
    }

    // Перехід з push-сповіщення. Подія могла ще не потрапити в кеш —
    // екран деталей сам запустить синхронізацію й дочекається її.
    LaunchedEffect(deepLinkEventId) {
        val eventId = deepLinkEventId ?: return@LaunchedEffect
        navController.navigate(Routes.detail(eventId)) { launchSingleTop = true }
        onDeepLinkHandled()
    }

    Column(modifier = modifier.fillMaxSize().background(AppColors.Background)) {
        NavHost(
            navController = navController,
            startDestination = Routes.MONTH,
            modifier = Modifier.weight(1f),
        ) {
            composable(Routes.MONTH) {
                EventsContent(state, viewModel::refresh) { events, isOffline, isRefreshing ->
                    MonthScreen(
                        events = events,
                        selectedDate = selectedDate,
                        onSelectDate = viewModel::selectDate,
                        onEventClick = openDetail,
                        isOffline = isOffline,
                        isRefreshing = isRefreshing,
                        onRefresh = viewModel::refresh,
                    )
                }
            }

            composable(Routes.WEEK) {
                EventsContent(state, viewModel::refresh) { events, isOffline, isRefreshing ->
                    WeekScreen(
                        events = events,
                        onEventClick = openDetail,
                        isOffline = isOffline,
                        isRefreshing = isRefreshing,
                        onRefresh = viewModel::refresh,
                    )
                }
            }

            composable(Routes.DAY) {
                EventsContent(state, viewModel::refresh) { events, isOffline, isRefreshing ->
                    DayScreen(
                        events = events,
                        selectedDate = selectedDate,
                        onSelectDate = viewModel::selectDate,
                        onEventClick = openDetail,
                        isOffline = isOffline,
                        isRefreshing = isRefreshing,
                        onRefresh = viewModel::refresh,
                    )
                }
            }

            composable(Routes.LIST) {
                EventsContent(state, viewModel::refresh) { events, isOffline, isRefreshing ->
                    ListScreen(
                        events = events,
                        onEventClick = openDetail,
                        isOffline = isOffline,
                        isRefreshing = isRefreshing,
                        onRefresh = viewModel::refresh,
                        audienceFilter = audienceFilter,
                        onAudienceFilterChange = viewModel::setAudienceFilter,
                    )
                }
            }

            composable(Routes.SETTINGS) {
                SettingsScreen(deviceRepository = deviceRepository)
            }

            composable(
                route = Routes.DETAIL,
                arguments = listOf(
                    navArgument(Routes.DETAIL_ARG_ID) { type = NavType.IntType },
                ),
            ) { entry ->
                val eventId = entry.arguments?.getInt(Routes.DETAIL_ARG_ID) ?: return@composable
                // Подія береться з уже завантажених даних — мережевого запиту за нею немає.
                val cached = viewModel.findById(eventId)

                if (cached != null) {
                    DetailScreen(event = cached, onBack = { navController.popBackStack() })
                } else {
                    // Такий випадок штатний лише для переходу з push: подію щойно
                    // підтвердили, і в кеші її ще немає. Синхронізуємось один раз.
                    var resolved by remember(eventId) { mutableStateOf<Event?>(null) }
                    var missing by remember(eventId) { mutableStateOf(false) }

                    LaunchedEffect(eventId) {
                        val found = viewModel.awaitEvent(eventId)
                        if (found != null) resolved = found else missing = true
                    }

                    val event = resolved
                    when {
                        event != null -> DetailScreen(
                            event = event,
                            onBack = { navController.popBackStack() },
                        )
                        // Події немає й після синхронізації — її могли відхилити
                        // (потрапила в `removed`). Тихо повертаємось назад.
                        missing -> LaunchedEffect(Unit) { navController.popBackStack() }
                        else -> LoadingState()
                    }
                }
            }
        }

        if (showBottomNav) {
            BottomNav(
                currentRoute = currentRoute,
                onTabSelected = { tab ->
                    if (currentRoute != tab.route) {
                        navController.navigate(tab.route) {
                            popUpTo(Routes.MONTH) { saveState = true }
                            launchSingleTop = true
                            restoreState = true
                        }
                    }
                },
            )
        }
    }
}

/**
 * Обгортка стану для вкладок зі списком подій: усі чотири поводяться однаково,
 * тому Loading / Error / Empty обробляються в одному місці.
 */
/**
 * Обгортка стану для вкладок зі списком подій: усі чотири поводяться однаково,
 * тому Loading / Error / Empty обробляються в одному місці.
 *
 * Прапорець «офлайн» передається всередину, а не малюється тут: кожен екран
 * сам малює свою шапку, і банер має стояти під нею, а не над нею.
 */
@Composable
private fun EventsContent(
    state: EventsUiState,
    onRetry: () -> Unit,
    content: @Composable (
        events: List<Event>,
        isOffline: Boolean,
        isRefreshing: Boolean,
    ) -> Unit,
) {
    when (state) {
        is EventsUiState.Loading -> LoadingState()
        is EventsUiState.Error -> ErrorState(error = state.error, onRetry = onRetry)
        // Порожня стрічка — теж коректний стан: екрани самі покажуть
        // свої порожні повідомлення («немає подій цього дня» тощо).
        is EventsUiState.Empty -> content(emptyList(), false, false)
        is EventsUiState.Success -> content(state.events, state.isOffline, state.isRefreshing)
    }
}
