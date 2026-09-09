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

/**
 * Канал сповіщень і побудова самого сповіщення.
 *
 * Потрібно тільки для випадку, коли застосунок на передньому плані: там
 * `onMessageReceived` спрацьовує, але система сповіщення не показує —
 * малюємо його самі.
 */
object AppNotifications {

    const val CHANNEL_ID = "events"

    fun ensureChannel(context: Context) {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O) return

        val manager = context.getSystemService(NotificationManager::class.java) ?: return
        if (manager.getNotificationChannel(CHANNEL_ID) != null) return

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

    /**
     * Показує сповіщення про подію. Тап відкриває екран деталей — той самий
     * шлях, що й у системного сповіщення з трея.
     */
    fun show(context: Context, eventId: String?, title: String?, body: String?) {
        ensureChannel(context)

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

        val notification = NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_notification)
            .setContentTitle(title ?: context.getString(R.string.app_name))
            .setContentText(body)
            .setStyle(NotificationCompat.BigTextStyle().bigText(body))
            .setAutoCancel(true)
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setContentIntent(pendingIntent)
            .build()

        // Без дозволу POST_NOTIFICATIONS (Android 13+) notify() мовчки нічого
        // не зробить — саме тому дозвіл запитується в налаштуваннях.
        if (NotificationManagerCompat.from(context).areNotificationsEnabled()) {
            NotificationManagerCompat.from(context)
                .notify(eventId?.toIntOrNull() ?: 0, notification)
        }
    }
}
