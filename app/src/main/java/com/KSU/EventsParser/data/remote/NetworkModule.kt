package com.KSU.EventsParser.data.remote

import android.util.Log
import com.KSU.EventsParser.core.config.AppConfig
import kotlinx.serialization.json.Json
import okhttp3.Interceptor
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.kotlinx.serialization.asConverterFactory
import java.util.concurrent.TimeUnit

/**
 * Складання мережевого стека.
 *
 * DI-фреймворка в проєкті немає — залежності збираються вручну в AppContainer,
 * тому це звичайний об'єкт з фабричними функціями.
 */
object NetworkModule {

    /** Заголовок, за яким запит знаходиться в логах сервера. */
    const val HEADER_REQUEST_ID = "X-Request-ID"

    /** Загальна кількість записів під тими самими фільтрами. */
    const val HEADER_TOTAL_COUNT = "X-Total-Count"

    private const val TAG = "EventsApi"

    /**
     * ignoreUnknownKeys обов'язковий: перелік полів події на сервері може
     * поповнюватися, і поява нового поля не має ламати розбір відповіді.
     * explicitNulls = false — щоб у тілі POST не з'являлися зайві null.
     */
    val json: Json = Json {
        ignoreUnknownKeys = true
        explicitNulls = false
        coerceInputValues = true
    }

    /**
     * Логує X-Request-ID кожної відповіді. Саме за ним конкретний запит
     * знаходиться в логах сервера, тому він потрібен і в успішних відповідях,
     * і в помилкових.
     */
    private val requestIdInterceptor = Interceptor { chain ->
        val response = chain.proceed(chain.request())
        val requestId = response.header(HEADER_REQUEST_ID)
        if (requestId != null) {
            Log.d(
                TAG,
                "${response.code} ${chain.request().method} ${chain.request().url} " +
                    "$HEADER_REQUEST_ID=$requestId",
            )
        }
        response
    }

    fun okHttpClient(): OkHttpClient = OkHttpClient.Builder()
        .connectTimeout(10, TimeUnit.SECONDS)
        .readTimeout(20, TimeUnit.SECONDS)
        .addInterceptor(requestIdInterceptor)
        .apply {
            if (AppConfig.isDebug) {
                addInterceptor(
                    HttpLoggingInterceptor().apply {
                        level = HttpLoggingInterceptor.Level.BASIC
                    },
                )
            }
        }
        .build()

    fun retrofit(client: OkHttpClient = okHttpClient()): Retrofit = Retrofit.Builder()
        // Retrofit вимагає, щоб базова адреса закінчувалася на «/».
        .baseUrl(AppConfig.baseUrl.trimEnd('/') + "/")
        .client(client)
        .addConverterFactory(json.asConverterFactory("application/json".toMediaType()))
        .build()

    fun eventsApi(retrofit: Retrofit = retrofit()): EventsApi =
        retrofit.create(EventsApi::class.java)
}
