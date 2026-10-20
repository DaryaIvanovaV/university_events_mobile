package com.KSU.EventsParser.reminder

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.util.Log
import com.KSU.EventsParser.EventsApp
import kotlinx.coroutines.launch

private const val TAG = "BootReceiver"

/**
 * Відновлює будильники нагадувань.
 *
 * Система скидає всі будильники після перезавантаження пристрою, а також після
 * оновлення застосунку (MY_PACKAGE_REPLACED). Обидва випадки обробляє один
 * ідемпотентний [com.KSU.EventsParser.data.repository.ReminderRepository.reconcile].
 */
class BootReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action != Intent.ACTION_BOOT_COMPLETED &&
            intent.action != Intent.ACTION_MY_PACKAGE_REPLACED
        ) {
            return
        }

        val container = (context.applicationContext as EventsApp).container
        val pendingResult = goAsync()

        container.appScope.launch {
            try {
                container.reminderRepository.reconcile()
            } catch (e: Exception) {
                Log.e(TAG, "Не вдалося відновити нагадування", e)
            } finally {
                pendingResult.finish()
            }
        }
    }
}
