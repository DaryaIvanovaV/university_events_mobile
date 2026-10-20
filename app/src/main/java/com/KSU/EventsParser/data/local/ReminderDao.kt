package com.KSU.EventsParser.data.local

import androidx.room.Dao
import androidx.room.Query
import androidx.room.Upsert
import kotlinx.coroutines.flow.Flow

@Dao
interface ReminderDao {

    /** Усі нагадування — для підписки UI (eventId → зсув). */
    @Query("SELECT * FROM reminders")
    fun observeAll(): Flow<List<ReminderEntity>>

    /** Одноразове читання всіх рядків: reconcile після синхронізації й перезавантаження. */
    @Query("SELECT * FROM reminders")
    suspend fun getAll(): List<ReminderEntity>

    @Query("SELECT * FROM reminders WHERE eventId = :eventId LIMIT 1")
    suspend fun find(eventId: Int): ReminderEntity?

    /** Нагадування одне на подію: повторне ввімкнення перезаписує зсув. */
    @Upsert
    suspend fun upsert(reminder: ReminderEntity)

    /** Ідемпотентне видалення: спрацювання й вимкнення можуть збігтися в часі. */
    @Query("DELETE FROM reminders WHERE eventId = :eventId")
    suspend fun delete(eventId: Int)
}
