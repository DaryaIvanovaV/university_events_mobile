package com.KSU.EventsParser.data.repository

import com.KSU.EventsParser.core.config.AppConfig
import com.KSU.EventsParser.data.local.ReminderDao
import com.KSU.EventsParser.data.local.ReminderEntity
import com.KSU.EventsParser.domain.model.Event
import com.KSU.EventsParser.domain.reminder.ReminderOffsets
import com.KSU.EventsParser.domain.reminder.ReminderSchedule
import com.KSU.EventsParser.reminder.ReminderScheduler
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.withContext
import java.time.Clock
import java.time.Duration
import java.time.ZonedDateTime

/**
 * Нагадування про події: одне на подію, зсув у хвилинах до початку.
 *
 * Джерело правди — таблиця `reminders` у Room, будильники в системі лише
 * відображають її. Тому [reconcile] може в будь-який момент привести
 * будильники у відповідність до таблиці й кешу подій.
 *
 * Усі зміни виконуються під одним [Mutex]: спрацювання будильника,
 * ввімкнення з екрана й reconcile після синхронізації можуть збігтися в часі.
 */
class ReminderRepository(
    private val dao: ReminderDao,
    private val eventsRepository: EventsRepository,
    private val scheduler: ReminderScheduler,
    private val clock: Clock = Clock.systemUTC(),
) {

    private val mutex = Mutex()

    /** eventId → зсув у хвилинах. Для екрана деталей. */
    fun observeOffsets(): Flow<Map<Int, Int>> =
        dao.observeAll().map { rows -> rows.associate { it.eventId to it.offsetMinutes } }

    /**
     * Вмикає нагадування за [offsetMinutes] до початку події.
     *
     * @return false, якщо поставити його не можна: події немає в кеші, у неї немає
     *         дати, зсув виходить за межі або момент спрацювання вже минув.
     *         У цьому разі нічого не зберігається.
     */
    suspend fun set(eventId: Int, offsetMinutes: Int): Boolean = withContext(Dispatchers.IO) {
        mutex.withLock { setLocked(eventId, offsetMinutes) }
    }

    /** Вимикає нагадування: знімає будильник і видаляє рядок. */
    suspend fun clear(eventId: Int) {
        withContext(Dispatchers.IO) {
            mutex.withLock {
                scheduler.cancel(eventId)
                dao.delete(eventId)
            }
        }
    }

    /**
     * Приводить будильники у відповідність до таблиці й кешу подій. Ідемпотентний.
     *
     * Для кожного рядка:
     *  • події немає в кеші, у неї немає дати або вона вже почалася — будильник
     *    знімаємо, рядок видаляємо;
     *  • інакше ставимо будильник на `max(момент спрацювання, зараз)`: нагадування,
     *    пропущене, поки телефон був вимкнений, усе одно спрацює, якщо подія ще
     *    не почалася. Повторна постановка з тим самим PendingIntent замінює
     *    старий будильник, і саме так підхоплюється перенесена модератором подія.
     *
     * Порожній кеш подій не чіпаємо: після 422 кеш очищується, і якщо повторна
     * синхронізація не вдалася, видалення «осиротілих» рядків знищило б усі
     * нагадування користувача. Зайвий рядок безпечний — [consume] його прибере.
     */
    suspend fun reconcile() {
        withContext(Dispatchers.IO) {
            mutex.withLock {
                if (!eventsRepository.hasCachedEvents()) return@withLock

                val now = clock.instant()
                for (row in dao.getAll()) {
                    val start = startOf(eventsRepository.findEvent(row.eventId))
                    if (start == null || !start.toInstant().isAfter(now)) {
                        scheduler.cancel(row.eventId)
                        dao.delete(row.eventId)
                        continue
                    }
                    val trigger = ReminderSchedule.triggerAt(start, row.offsetMinutes).toInstant()
                    scheduler.schedule(row.eventId, maxOf(trigger, now).toEpochMilli())
                }
            }
        }
    }

    /**
     * Викликається, коли спрацював будильник події [eventId].
     *
     * @return подія й зсув, про які треба сповістити, або `null`, якщо
     *         сповіщати не треба: рядка вже немає, подію видалено чи позбавлено
     *         дати, або її перенесли на пізніше (тоді будильник ставиться наново).
     *         Після успішного повернення рядок видаляється: нагадування спрацьовує один раз.
     */
    suspend fun consume(eventId: Int): Pair<Event, Int>? = withContext(Dispatchers.IO) {
        mutex.withLock { consumeLocked(eventId) }
    }

    private suspend fun setLocked(eventId: Int, offsetMinutes: Int): Boolean {
        if (offsetMinutes !in 1..ReminderOffsets.MAX) return false

        val start = startOf(eventsRepository.findEvent(eventId)) ?: return false
        if (!ReminderSchedule.isOffsetValid(start, offsetMinutes, clock.instant())) return false

        dao.upsert(ReminderEntity(eventId = eventId, offsetMinutes = offsetMinutes))
        scheduler.schedule(
            eventId,
            ReminderSchedule.triggerAt(start, offsetMinutes).toInstant().toEpochMilli(),
        )
        return true
    }

    private suspend fun consumeLocked(eventId: Int): Pair<Event, Int>? {
        val row = dao.find(eventId) ?: return null

        val event = eventsRepository.findEvent(eventId)
        val start = startOf(event)
        if (event == null || start == null) {
            // Подію видалили або в неї зник час відліку — нагадувати нема про що.
            dao.delete(eventId)
            return null
        }

        val trigger = ReminderSchedule.triggerAt(start, row.offsetMinutes).toInstant()
        if (clock.instant().isBefore(trigger.minus(MOVED_TOLERANCE))) {
            // Спрацювало раніше за актуальний момент — подію перенесли на пізніше.
            scheduler.schedule(eventId, trigger.toEpochMilli())
            return null
        }

        dao.delete(eventId)
        return event to row.offsetMinutes
    }

    /** Момент початку події в поясі подій або `null`, якщо події чи дати немає. */
    private fun startOf(event: Event?): ZonedDateTime? =
        event?.let {
            ReminderSchedule.eventStart(it, AppConfig.eventZone, AppConfig.reminderFallbackTime)
        }

    private companion object {
        /** Допуск на неточність будильника: раніше за цей час — подію перенесли. */
        val MOVED_TOLERANCE: Duration = Duration.ofMinutes(1)
    }
}
