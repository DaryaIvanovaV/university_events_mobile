package com.KSU.EventsParser.ui

import com.KSU.EventsParser.domain.model.AppError
import com.KSU.EventsParser.domain.model.Event

/**
 * Стан екранів зі списком подій.
 *
 * «Порожня стрічка» і «сервер недоступний» — окремі стани: для користувача
 * вони виглядали б однаково (порожній екран), але означають різне й вимагають
 * різних дій.
 */
sealed interface EventsUiState {

    data object Loading : EventsUiState

    data class Success(
        val events: List<Event>,
        val isRefreshing: Boolean = false,
        /**
         * Дані показуються з кешу, бо остання синхронізація не вдалася.
         * Над списком з'являється банер «офлайн».
         */
        val isOffline: Boolean = false,
    ) : EventsUiState

    /** Сервер відповів, але подій немає. */
    data object Empty : EventsUiState

    /** Синхронізація не вдалася й показати нічого — кеш порожній. */
    data class Error(
        val error: AppError,
        /** Результат проби /health/live — чи взагалі живий сервер. */
        val serverReachable: Boolean = false,
    ) : EventsUiState
}
