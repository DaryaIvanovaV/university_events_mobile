package com.KSU.EventsParser

import android.app.Application
import android.content.Context
import androidx.room.Room
import com.KSU.EventsParser.data.local.EventsDatabase
import com.KSU.EventsParser.data.local.NotificationPreferences
import com.KSU.EventsParser.data.local.SyncPreferences
import com.KSU.EventsParser.data.remote.EventsApi
import com.KSU.EventsParser.data.remote.NetworkModule
import com.KSU.EventsParser.data.repository.DeviceRepository
import com.KSU.EventsParser.data.repository.EventsRepository
import com.KSU.EventsParser.push.AppNotifications

/**
 * Ручне складання залежностей.
 *
 * DI-фреймворк (Hilt/Koin) у стек проєкту не заявлений, а для застосунку такого
 * розміру контейнер із кількох полів простіший і прозоріший за кодогенерацію.
 * Створення відкладене (by lazy), щоб не платити за мережевий стек і відкриття
 * бази на старті.
 */
class AppContainer(private val context: Context) {

    val eventsApi: EventsApi by lazy { NetworkModule.eventsApi() }

    private val database: EventsDatabase by lazy {
        Room.databaseBuilder(context, EventsDatabase::class.java, EventsDatabase.NAME).build()
    }

    private val syncPreferences: SyncPreferences by lazy { SyncPreferences(context) }

    private val notificationPreferences: NotificationPreferences by lazy {
        NotificationPreferences(context)
    }

    val eventsRepository: EventsRepository by lazy {
        EventsRepository(
            api = eventsApi,
            dao = database.eventDao(),
            syncPreferences = syncPreferences,
        )
    }

    val deviceRepository: DeviceRepository by lazy {
        DeviceRepository(api = eventsApi, preferences = notificationPreferences)
    }
}

class EventsApp : Application() {
    lateinit var container: AppContainer
        private set

    override fun onCreate() {
        super.onCreate()
        container = AppContainer(this)
        // Канал має існувати ДО першого сповіщення, зокрема до того, яке
        // система показує сама, коли застосунок згорнуто.
        AppNotifications.ensureChannel(this)
    }
}
