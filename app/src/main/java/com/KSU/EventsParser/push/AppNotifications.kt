package com.KSU.EventsParser.push

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import com.KSU.EventsParser.MainActivity
import com.KSU.EventsParser.R
import com.KSU.EventsParser.core.locale.AppLocale
import com.KSU.EventsParser.domain.model.Event
import com.KSU.EventsParser.ui.util.dateText
import com.KSU.EventsParser.ui.util.reminderOffsetLabel
import com.KSU.EventsParser.ui.util.timeText

/**
 * Канали сповіщень і побудова самих сповіщень.
 *
 * Два канали:
 *  • `events` — push про нові події. Власне сповіщення малюємо самі лише коли
 *    застосунок на передньому плані: там `onMessageReceived` спрацьовує, але
 *    система сповіщення не показує;
 *  • `reminders` — локальні нагадування про початок події (див. ReminderReceiver).
 */
object AppNotifications {

    const val CHANNEL_ID = "events"
    const val REMINDERS_CHANNEL_ID = "reminders"

    /**
     * Тег сповіщень-нагадувань. Разом з id події він відрізняє нагадування
     * від push про ту саму подію, і одне не затирає інше.
     */
    private const val TAG_REMINDER = "reminder"

    fun ensureChannels(context: Context) {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O) return

        val manager = context.getSystemService(NotificationManager::class.java) ?: return

        if (manager.getNotificationChannel(CHANNEL_ID) == null) {
            manager.createNotificationChannel(
                NotificationChannel(
                    CHANNEL_ID,
                    context.getString(R.string.notification_channel_name),
                    NotificationManager.IMPORTANCE_DEFAULT,
                ).apply {
                    description = context.getString(R.string.notification_channel_description)
                },
            )
        }

        if (manager.getNotificationChannel(REMINDERS_CHANNEL_ID) == null) {
            manager.createNotificationChannel(
                NotificationChannel(
                    REMINDERS_CHANNEL_ID,
                    context.getString(R.string.reminder_channel_name),
                    NotificationManager.IMPORTANCE_HIGH,
                ).apply {
                    description = context.getString(R.string.reminder_channel_description)
                },
            )
        }
    }

    /**
     * Показує сповіщення про подію. Тап відкриває екран деталей — той самий
     * шлях, що й у системного сповіщення з трея.
     */
    fun show(context: Context, eventId: String?, title: String?, body: String?) {
        post(
            context = context,
            channelId = CHANNEL_ID,
            tag = null,
            eventId = eventId,
            title = title,
            body = body,
            priority = NotificationCompat.PRIORITY_DEFAULT,
        )
    }

    /**
     * Показує нагадування про початок події: «Через 1 год · 25 жовтня, 10:00».
     *
     * Тап веде на екран деталей тим самим шляхом, що й push (PushDeepLink).
     * Текст локалізуємо вручну: ми поза Activity, і на API < 33 мову, обрану
     * в застосунку, потрібно накласти самим (AppLocale.wrap). На API 33+ це
     * no-op — мову вже застосувала система.
     */
    fun showReminder(context: Context, event: Event, offsetMinutes: Int) {
        val localized = AppLocale.wrap(context.applicationContext)
        val res = localized.resources
        val locale = res.configuration.locales[0]

        val whenText = "${event.dateText(res, locale)}, ${event.timeText(res)}"
        val body = res.getString(
            R.string.reminder_notification_body,
            reminderOffsetLabel(res, offsetMinutes),
            whenText,
        )

        post(
            context = context,
            channelId = REMINDERS_CHANNEL_ID,
            tag = TAG_REMINDER,
            eventId = event.id.toString(),
            title = event.title,
            body = body,
            priority = NotificationCompat.PRIORITY_HIGH,
        )
    }

    private fun post(
        context: Context,
        channelId: String,
        tag: String?,
        eventId: String?,
        title: String?,
        body: String?,
        priority: Int,
    ) {
        ensureChannels(context)

        val intent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
            if (!eventId.isNullOrEmpty()) {
                putExtra(PushDeepLink.EXTRA_EVENT_ID, eventId)
            }
        }

        val pendingIntent = PendingIntent.getActivity(
            context,
            eventId?.toIntOrNull() ?: 0,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE,
        )

        val notification = NotificationCompat.Builder(context, channelId)
            .setSmallIcon(R.drawable.ic_notification)
            .setContentTitle(title ?: context.getString(R.string.app_name))
            .setContentText(body)
            .setStyle(NotificationCompat.BigTextStyle().bigText(body))
            .setAutoCancel(true)
            .setPriority(priority)
            .setContentIntent(pendingIntent)
            .build()

        // Без дозволу POST_NOTIFICATIONS (Android 13+) notify() мовчки нічого
        // не зробить — саме тому дозвіл запитується в налаштуваннях.
        if (NotificationManagerCompat.from(context).areNotificationsEnabled()) {
            NotificationManagerCompat.from(context)
                .notify(tag, eventId?.toIntOrNull() ?: 0, notification)
        }
    }
}
