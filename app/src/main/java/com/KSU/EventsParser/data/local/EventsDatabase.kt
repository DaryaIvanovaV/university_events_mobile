package com.KSU.EventsParser.data.local

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase

@Database(
    entities = [EventEntity::class, ReminderEntity::class],
    version = 2,
    exportSchema = false,
)
abstract class EventsDatabase : RoomDatabase() {
    abstract fun eventDao(): EventDao
    abstract fun reminderDao(): ReminderDao

    companion object {
        const val NAME = "events.db"

        /**
         * 1 → 2: додано таблицю нагадувань.
         *
         * SQL має збігатися з тим, що Room генерує для ReminderEntity, інакше
         * застосунок упаде після оновлення. Деструктивного відкату (fallback) немає
         * свідомо: нагадування — дані користувача, їх не відновити із сервера.
         */
        val MIGRATION_1_2: Migration = object : Migration(1, 2) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL(
                    "CREATE TABLE IF NOT EXISTS `reminders` " +
                        "(`eventId` INTEGER NOT NULL, `offsetMinutes` INTEGER NOT NULL, " +
                        "PRIMARY KEY(`eventId`))",
                )
            }
        }
    }
}
