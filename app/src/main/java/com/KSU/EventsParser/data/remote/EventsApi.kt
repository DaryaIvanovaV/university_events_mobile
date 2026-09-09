package com.KSU.EventsParser.data.remote

import com.KSU.EventsParser.data.remote.dto.DeviceRegistrationDto
import com.KSU.EventsParser.data.remote.dto.DeviceRegistrationResponseDto
import com.KSU.EventsParser.data.remote.dto.EventDto
import com.KSU.EventsParser.data.remote.dto.SyncResponseDto
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Query

/**
 * Єдиний мережевий інтерфейс застосунку.
 *
 * Тут оголошено РІВНО п'ять ендпоінтів, доступних без API-ключа. Решта системи
 * (модерація, черга, статистика, GET /events/{id}) закрита ключем, і ключа в
 * застосунку немає й бути не повинно: ключ, зашитий в APK, вважається публічним.
 *
 * GET /events/{id} свідомо ВІДСУТНІЙ у цьому інтерфейсі, щоб його не можна було
 * викликати помилково: він адміністративний і повертає raw_text — неопрацьований
 * текст оголошення, де можуть бути телефони й прізвища. Екран деталей будується
 * з уже завантаженого об'єкта і не робить жодного мережевого запиту.
 */
interface EventsApi {

    /**
     * Стрічка подій.
     *
     * Відповідь — «голий» JSON-масив, а не об'єкт-обгортка. Загальна кількість
     * під тими самими фільтрами приходить у заголовку X-Total-Count, тому
     * повертаємо Response<...>, а не одразу список: за довжиною масиву
     * наявність наступної сторінки визначити неможливо.
     *
     * Параметр `status` не передаємо — на сервері за замовчуванням `approved`,
     * а публічна стрічка іншого й не віддає.
     *
     * УВАГА: upcoming, dateFrom і dateTo — це SQL-порівняння по полю `date`,
     * тому вони МОВЧКИ відкидають усі події з date == null.
     */
    @GET("events")
    suspend fun getEvents(
        @Query("upcoming") upcoming: Boolean? = null,
        @Query("date_from") dateFrom: String? = null,
        @Query("date_to") dateTo: String? = null,
        @Query("event_type") eventType: String? = null,
        @Query("limit") limit: Int,
        @Query("offset") offset: Int,
    ): Response<List<EventDto>>

    /**
     * Інкрементальна синхронізація локального кешу.
     * `cursor` при першому запуску — null: прийде вся опублікована стрічка.
     */
    @GET("events/sync")
    suspend fun sync(
        @Query("cursor") cursor: String?,
        @Query("limit") limit: Int,
    ): SyncResponseDto

    /** Підписка пристрою на push. Викликати і при кожній ротації FCM-токена. */
    @POST("devices/register")
    suspend fun registerDevice(
        @Body body: DeviceRegistrationDto,
    ): DeviceRegistrationResponseDto

    /** Відписка — той самий формат тіла. */
    @POST("devices/unregister")
    suspend fun unregisterDevice(
        @Body body: DeviceRegistrationDto,
    ): DeviceRegistrationResponseDto

    /**
     * Проба доступності сервера. Не чіпає базу й відповідає миттєво.
     * Свідомо НЕ /health/ready: той опитує БД і LLM і може відповісти
     * `degraded`, хоча стрічка при цьому працює.
     */
    @GET("health/live")
    suspend fun healthLive(): Response<Unit>
}
