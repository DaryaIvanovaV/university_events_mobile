package com.KSU.EventsParser.reminder

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.util.Log
import androidx.core.app.AlarmManagerCompat

private const val TAG = "ReminderScheduler"

/**
 * Постановка й зняття будильників нагадувань через [AlarmManager].
 *
 * Один будильник на подію: `PendingIntent` ідентифікується за requestCode = eventId,
 * тому повторний [schedule] з тим самим eventId ЗАМІНЮЄ попередній будильник
 * (так перенесення події модератором підхоплюється під час reconcile).
 *
 * Це тонка обгортка над системою: логіку «коли саме спрацьовувати» вирішує
 * ReminderRepository, а арифметику часу — domain/reminder.
 */
class ReminderScheduler(private val context: Context) {

    private val alarmManager: AlarmManager?
        get() = context.getSystemService(AlarmManager::class.java)

    /**
     * Ставить будильник на [triggerAtMillis] (epoch UTC).
     *
     * Якщо точні будильники дозволені — точний (спрацьовує навіть у Doze),
     * інакше неточний: на практиці він запізнюється на кілька хвилин, але
     * нагадування все одно приходить.
     */
    fun schedule(eventId: Int, triggerAtMillis: Long) {
        val manager = alarmManager ?: return
        val pendingIntent = pendingIntent(eventId)

        if (AlarmManagerCompat.canScheduleExactAlarms(manager)) {
            try {
                AlarmManagerCompat.setExactAndAllowWhileIdle(
                    manager,
                    AlarmManager.RTC_WAKEUP,
                    triggerAtMillis,
                    pendingIntent,
                )
                return
            } catch (e: SecurityException) {
                // Дозвіл на точні будильники могли відкликати між перевіркою і викликом.
                Log.w(TAG, "Точний будильник заборонено — ставимо неточний", e)
            }
        }
        AlarmManagerCompat.setAndAllowWhileIdle(
            manager,
            AlarmManager.RTC_WAKEUP,
            triggerAtMillis,
            pendingIntent,
        )
    }

    /** Знімає будильник події. Якщо його не було — нічого не відбувається. */
    fun cancel(eventId: Int) {
        val pendingIntent = pendingIntent(eventId)
        alarmManager?.cancel(pendingIntent)
        pendingIntent.cancel()
    }

    /**
     * Явний Intent на [ReminderReceiver] (не експортований). Прапорці
     * IMMUTABLE | UPDATE_CURRENT: вміст незмінний, а той самий requestCode
     * оновлює наявний PendingIntent, а не створює другий.
     */
    private fun pendingIntent(eventId: Int): PendingIntent {
        val intent = Intent(context, ReminderReceiver::class.java).apply {
            putExtra(EXTRA_EVENT_ID, eventId)
        }
        return PendingIntent.getBroadcast(
            context,
            eventId,
            intent,
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT,
        )
    }

    companion object {
        /** Ідентифікатор події в Intent'і будильника (число, не рядок, як у push). */
        const val EXTRA_EVENT_ID = "reminder_event_id"
    }
}
