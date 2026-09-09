package com.KSU.EventsParser.data.repository

import android.util.Log
import com.KSU.EventsParser.core.config.AppConfig
import com.KSU.EventsParser.data.local.EventDao
import com.KSU.EventsParser.data.local.SyncPreferences
import com.KSU.EventsParser.data.mapper.EventMapper
import com.KSU.EventsParser.data.remote.EventsApi
import com.KSU.EventsParser.data.remote.NetworkModule
import com.KSU.EventsParser.domain.model.AppError
import com.KSU.EventsParser.domain.model.DataResult
import com.KSU.EventsParser.domain.model.Event
import com.KSU.EventsParser.domain.model.EventPage
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext
import retrofit2.HttpException
import java.io.IOException
import java.time.LocalDate

private const val TAG = "EventsRepository"

/**
 * Доступ до подій.
 *
 * Джерело правди для інтерфейсу — локальний кеш Room, а наповнює його
 * `GET /events/sync`. Саме цей ендпоінт спроєктовано під кеш: перший виклик
 * без курсора віддає всю опубліковану стрічку, наступні — лише зміни, і лише
 * він повідомляє про ВИДАЛЕНІ події.
 */
class EventsRepository(
    private val api: EventsApi,
    private val dao: EventDao,
    private val syncPreferences: SyncPreferences,
) {

    /** Потік подій із кешу. Порядок задає SQL — такий самий, як у GET /events. */
    fun observeEvents(): Flow<List<Event>> =
        dao.observeAll().map { entities -> entities.map(EventMapper::toDomain) }

    suspend fun hasCachedEvents(): Boolean = withContext(Dispatchers.IO) { dao.count() > 0 }

    /** Пряме читання однієї події з кешу — без звернення до мережі. */
    suspend fun findEvent(id: Int): Event? = withContext(Dispatchers.IO) {
        dao.findById(id)?.let(EventMapper::toDomain)
    }

    /**
     * Інкрементальна синхронізація кешу.
     *
     * Алгоритм точно за контрактом:
     *   cursor = завантажити_зі_сховища()            // при першому запуску null
     *   do {
     *       відповідь = GET /events/sync?cursor=...
     *       changed → upsert за id                   // повтори — це норма
     *       removed → ідемпотентне видалення
     *       cursor = відповідь.cursor; зберегти
     *   } while (відповідь.has_more)
     *
     * `removed` обробляти обов'язково: модератор може відхилити вже
     * опубліковану подію (захід скасували). Зі стрічки вона просто зникає,
     * і клієнт із кешем показував би скасований захід — студент прийшов би
     * до зачиненої аудиторії.
     */
    suspend fun sync(): DataResult<Unit> = withContext(Dispatchers.IO) {
        var cursor: String? = syncPreferences.cursor()
        var alreadyResetCursor = false
        var pages = 0

        while (true) {
            if (++pages > MAX_SYNC_PAGES) {
                Log.w(TAG, "Синхронізація перевищила $MAX_SYNC_PAGES сторінок — зупиняємось")
                return@withContext DataResult.Success(Unit)
            }

            when (
                val result = runCatchingApi {
                    api.sync(cursor = cursor, limit = AppConfig.syncPageSize)
                }
            ) {
                is DataResult.Failure -> {
                    // 422 означає, що сервер визнав курсор битим. Мовчки почати
                    // з нуля не можна — клієнт вирішив би, що його кеш свіжий.
                    // Тому чистимо кеш і синхронізуємося без курсора. Рівно один раз,
                    // інакше при постійному 422 був би нескінченний цикл.
                    if (result.error == AppError.BadRequest && !alreadyResetCursor) {
                        Log.w(TAG, "422: битий курсор — очищаємо кеш і синхронізуємось з нуля")
                        dao.clear()
                        syncPreferences.clear()
                        cursor = null
                        alreadyResetCursor = true
                        continue
                    }
                    return@withContext result
                }

                is DataResult.Success -> {
                    val body = result.data

                    if (body.changed.isNotEmpty()) {
                        dao.upsertAll(body.changed.map(EventMapper::toEntity))
                    }
                    if (body.removed.isNotEmpty()) {
                        dao.deleteByIds(body.removed)
                    }

                    cursor = body.cursor
                    syncPreferences.saveCursor(body.cursor)

                    Log.d(
                        TAG,
                        "sync: changed=${body.changed.size} removed=${body.removed.size} " +
                            "has_more=${body.hasMore}",
                    )

                    // has_more істинне означає, що видача впeрлася в limit —
                    // повторюємо одразу, без затримки.
                    if (!body.hasMore) return@withContext DataResult.Success(Unit)
                }
            }
        }
        // Недосяжно: з циклу виходимо лише через return@withContext вище.
        @Suppress("UNREACHABLE_CODE")
        return@withContext DataResult.Success(Unit)
    }

    /**
     * Одна сторінка стрічки через GET /events.
     *
     * Основний потік даних іде через [sync] — цей метод лишається як
     * документований посторінковий доступ до стрічки (наприклад, для
     * діагностики або серверної фільтрації за типом чи діапазоном дат).
     *
     * Свідомо НЕ фільтруємо минулі події повторно на клієнті: «сьогодні» для
     * параметра upcoming сервер рахує за київським часом (APP_TIMEZONE), і
     * друга фільтрація за локальним часом дала б розбіжність на межі доби.
     */
    suspend fun loadFeed(
        offset: Int = 0,
        limit: Int = AppConfig.feedPageSize,
        upcoming: Boolean? = null,
        dateFrom: LocalDate? = null,
        dateTo: LocalDate? = null,
        eventType: String? = null,
    ): DataResult<EventPage> = withContext(Dispatchers.IO) {
        runCatchingApi {
            val response = api.getEvents(
                upcoming = upcoming,
                dateFrom = dateFrom?.toString(),
                dateTo = dateTo?.toString(),
                eventType = eventType,
                limit = limit,
                offset = offset,
            )
            if (!response.isSuccessful) throw HttpException(response)

            val body = response.body().orEmpty()
            // Наявність наступної сторінки визначає лише X-Total-Count:
            // GET /events повертає «голий» масив, тож за його довжиною це не порахувати.
            val total = response.headers()[NetworkModule.HEADER_TOTAL_COUNT]
                ?.toIntOrNull()
                ?: (offset + body.size)

            EventPage(events = body.map(EventMapper::toDomain), total = total, offset = offset)
        }
    }

    /**
     * Проба доступності сервера. Використовується, щоб відрізнити
     * «сервер недоступний» від «стрічка порожня» — для користувача обидва
     * стани виглядають як порожній екран.
     */
    suspend fun isServerReachable(): Boolean = withContext(Dispatchers.IO) {
        runCatching { api.healthLive().isSuccessful }.getOrDefault(false)
    }

    private companion object {
        const val MAX_SYNC_PAGES = 100
    }
}

/** Загальне відображення винятків на [AppError] за таблицею помилок контракту. */
internal inline fun <T> runCatchingApi(block: () -> T): DataResult<T> = try {
    DataResult.Success(block())
} catch (e: HttpException) {
    val error = when (e.code()) {
        422 -> AppError.BadRequest
        503 -> AppError.ServiceUnavailable
        500 -> AppError.ServerError
        else -> AppError.Unknown
    }
    Log.w(TAG, "HTTP ${e.code()} -> $error", e)
    DataResult.Failure(error)
} catch (e: IOException) {
    // ConnectException, UnknownHostException, SocketTimeoutException тощо.
    Log.w(TAG, "Мережа недоступна", e)
    DataResult.Failure(AppError.Unreachable)
} catch (e: Exception) {
    Log.e(TAG, "Непередбачена помилка", e)
    DataResult.Failure(AppError.Unknown)
}
