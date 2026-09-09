package com.KSU.EventsParser.data.local

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map

private val Context.notificationDataStore: DataStore<Preferences> by
    preferencesDataStore(name = "notifications")

/**
 * Вибір користувача щодо push-сповіщень.
 *
 * Зберігаємо саме НАМІР користувача, окремо від системного дозволу
 * POST_NOTIFICATIONS: дозвіл можна відкликати в налаштуваннях системи, і тоді
 * намір лишається увімкненим, але сповіщення не показуються. Плутати ці дві
 * речі не можна — інакше після повернення дозволу підписка не відновиться.
 */
class NotificationPreferences(private val context: Context) {

    private val enabledKey = booleanPreferencesKey("notifications_enabled")

    /** За замовчуванням вимкнено: не підписуємо користувача без його згоди. */
    val enabled: Flow<Boolean> =
        context.notificationDataStore.data.map { it[enabledKey] ?: false }

    suspend fun isEnabled(): Boolean = enabled.first()

    suspend fun setEnabled(value: Boolean) {
        context.notificationDataStore.edit { it[enabledKey] = value }
    }
}
