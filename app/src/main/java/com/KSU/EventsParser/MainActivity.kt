package com.KSU.EventsParser

import android.content.Context
import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.lifecycle.viewmodel.compose.viewModel
import com.KSU.EventsParser.core.locale.AppLocale
import com.KSU.EventsParser.push.PushDeepLink
import com.KSU.EventsParser.ui.EventsViewModel
import com.KSU.EventsParser.ui.navigation.AppNavHost
import com.KSU.EventsParser.ui.theme.EventsParserTheme
import kotlinx.coroutines.flow.MutableStateFlow

class MainActivity : ComponentActivity() {

    /**
     * Подія, на яку треба перейти з push-сповіщення.
     *
     * Шляхів надходження два, і обидва обов'язкові:
     *  • застосунок згорнуто — сповіщення показала система, дані прийшли
     *    в extras Intent'а запуску (onCreate);
     *  • застосунок уже відкрито — тап по сповіщенню приходить в onNewIntent
     *    (працює завдяки launchMode="singleTop" у маніфесті).
     */
    private val pendingEventId = MutableStateFlow<Int?>(null)

    /**
     * Підміна локалі до створення інтерфейсу.
     * Потрібна лише на API < 33; на новіших системах локаль застосовує
     * сама система через LocaleManager — див. AppLocale.
     */
    override fun attachBaseContext(newBase: Context) {
        super.attachBaseContext(AppLocale.wrap(newBase))
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        pendingEventId.value = PushDeepLink.eventIdFrom(intent)

        val container = (application as EventsApp).container

        setContent {
            EventsParserTheme {
                val eventsViewModel: EventsViewModel = viewModel(
                    factory = EventsViewModel.factory(container.eventsRepository),
                )
                val deepLinkEventId by pendingEventId.collectAsState()

                AppNavHost(
                    viewModel = eventsViewModel,
                    deviceRepository = container.deviceRepository,
                    deepLinkEventId = deepLinkEventId,
                    onDeepLinkHandled = { pendingEventId.value = null },
                )
            }
        }
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        // Щоб наступні звернення до getIntent() бачили свіжі дані.
        setIntent(intent)
        PushDeepLink.eventIdFrom(intent)?.let { pendingEventId.value = it }
    }
}
