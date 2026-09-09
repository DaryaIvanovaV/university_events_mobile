package com.KSU.EventsParser.domain.model

/**
 * Класи помилок за таблицею з контракту API.
 *
 * Окремо виділено [Unreachable]: «сервер недоступний» і «стрічка порожня»
 * для користувача виглядають однаково, якщо їх не розрізняти, — а це різні
 * ситуації з різними діями.
 */
enum class AppError {
    /** ConnectException / UnknownHost / timeout. Перевіряється пробою /health/live. */
    Unreachable,

    /** 503 — база даних недоступна. Показати «сервіс недоступний», повторити з backoff. */
    ServiceUnavailable,

    /** 500 — внутрішня помилка сервера. Повторити, залогувати X-Request-ID. */
    ServerError,

    /**
     * 422 — некоректні параметри: це баг клієнта.
     * Для курсора синхронізації означає «курсор битий» → очистити кеш і
     * синхронізуватися без курсора.
     */
    BadRequest,

    Unknown,
}

/** Результат операції рівня даних. */
sealed interface DataResult<out T> {
    data class Success<T>(val data: T) : DataResult<T>
    data class Failure(val error: AppError) : DataResult<Nothing>
}

/**
 * Сторінка стрічки.
 *
 * [total] береться із заголовка X-Total-Count, бо за довжиною масиву
 * наявність наступної сторінки визначити неможливо.
 */
data class EventPage(
    val events: List<Event>,
    val total: Int,
    val offset: Int,
) {
    val hasMore: Boolean get() = offset + events.size < total
}
