package com.KSU.EventsParser.data.local

import androidx.room.Dao
import androidx.room.Query
import androidx.room.Upsert
import kotlinx.coroutines.flow.Flow

@Dao
interface EventDao {

    /**
     * Усі події в тому ж порядку, що й у GET /events: за датою за зростанням,
     * події без дати — в кінці, за однакової дати — за id.
     * У SQLite `date IS NULL` дає 0/1, тож NULL-и опиняються в кінці.
     */
    @Query("SELECT * FROM events ORDER BY date IS NULL, date ASC, id ASC")
    fun observeAll(): Flow<List<EventEntity>>

    @Query("SELECT * FROM events WHERE id = :id LIMIT 1")
    suspend fun findById(id: Int): EventEntity?

    /** Вставити АБО перезаписати за id: у /events/sync повтори — це норма. */
    @Upsert
    suspend fun upsertAll(events: List<EventEntity>)

    /** Ідемпотентне видалення: застосовуємо список `removed` із /events/sync. */
    @Query("DELETE FROM events WHERE id IN (:ids)")
    suspend fun deleteByIds(ids: List<Int>)

    /** Повне очищення — потрібне на 422 (битий курсор) перед повторною синхронізацією. */
    @Query("DELETE FROM events")
    suspend fun clear()

    @Query("SELECT COUNT(*) FROM events")
    suspend fun count(): Int
}
