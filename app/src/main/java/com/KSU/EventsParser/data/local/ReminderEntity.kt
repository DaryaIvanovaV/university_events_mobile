package com.KSU.EventsParser.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * Нагадування про подію: одне на подію, зсув — у хвилинах до початку.
 *
 * Зовнішнього ключа на `events` НЕМАЄ свідомо. На 422 ми викликаємо
 * `EventDao.clear()` і перезавантажуємо кеш подій; каскадне видалення
 * стерло б усі нагадування — це дані користувача, їх не відновити із сервера.
 * Осиротілі рядки прибирає ReminderRepository.reconcile() після успішної синхронізації.
 */
@Entity(tableName = "reminders")
data class ReminderEntity(
    @PrimaryKey val eventId: Int,
    val offsetMinutes: Int,
)
