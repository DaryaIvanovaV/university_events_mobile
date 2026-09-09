package com.KSU.EventsParser.data.local

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.first

private val Context.syncDataStore: DataStore<Preferences> by preferencesDataStore(name = "sync")

/**
 * Зберігання курсора синхронізації.
 *
 * Курсор НЕПРОЗОРИЙ: зберігаємо й повертаємо його дослівно, як прислав сервер.
 * Не розбирати, не конструювати самотужки й не підставляти замість нього час
 * пристрою — годинник телефона може відставати або поспішати, і події, змінені
 * в цьому проміжку, загубилися б назавжди. Усередині курсора пара (момент, id)
 * саме тому, що за одним лише часом синхронізація зациклилася б на подіях
 * з однаковим updated_at.
 */
class SyncPreferences(private val context: Context) {

    private val cursorKey = stringPreferencesKey("events_cursor")

    /** `null` — синхронізації ще не було; тоді запит іде без параметра cursor. */
    suspend fun cursor(): String? = context.syncDataStore.data.first()[cursorKey]

    suspend fun saveCursor(value: String?) {
        context.syncDataStore.edit { preferences ->
            if (value == null) preferences.remove(cursorKey) else preferences[cursorKey] = value
        }
    }

    /** Скидання курсора — потрібне на 422 (сервер визнав курсор битим). */
    suspend fun clear() {
        context.syncDataStore.edit { it.remove(cursorKey) }
    }
}
