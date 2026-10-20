package com.KSU.EventsParser.reminder

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.util.Log
import com.KSU.EventsParser.EventsApp
import com.KSU.EventsParser.push.AppNotifications
import kotlinx.coroutines.launch

private const val TAG = "ReminderReceiver"

/**
 * Приймає спрацювання будильника нагадування й показує сповіщення.
 *
 * Робота з Room асинхронна, а `onReceive` має завершитися швидко, тому
 * використовуємо goAsync(): система не вбиває процес, доки не викликано finish().
 * Корутина живе в appScope контейнера (не GlobalScope).
 */
class ReminderReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {
        val eventId = intent.getIntExtra(ReminderScheduler.EXTRA_EVENT_ID, NO_EVENT)
        if (eventId == NO_EVENT) return

        val appContext = context.applicationContext
        val container = (appContext as EventsApp).container
        val pendingResult = goAsync()

        container.appScope.launch {
            try {
                // null — подію перенесено, видалено або нагадування вже знято.
                container.reminderRepository.consume(eventId)?.let { (event, offsetMinutes) ->
                    AppNotifications.showReminder(appContext, event, offsetMinutes)
                }
            } catch (e: Exception) {
                Log.e(TAG, "Не вдалося показати нагадування для події $eventId", e)
            } finally {
                pendingResult.finish()
            }
        }
    }

    private companion object {
        const val NO_EVENT = -1
    }
}
