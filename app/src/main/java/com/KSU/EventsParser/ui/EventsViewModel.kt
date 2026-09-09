package com.KSU.EventsParser.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.CreationExtras
import com.KSU.EventsParser.data.repository.EventsRepository
import com.KSU.EventsParser.domain.model.AppError
import com.KSU.EventsParser.domain.model.DataResult
import com.KSU.EventsParser.domain.model.Event
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.time.LocalDate

/**
 * Спільне сховище подій для всіх вкладок.
 *
 * Один ViewModel на весь NavHost навмисно: екран деталей за контрактом НЕ
 * робить мережевого запиту (GET /events/{id} закритий ключем і повертає
 * raw_text), тому він мусить брати подію з уже завантажених даних.
 *
 * Джерело правди — кеш Room; наповнює його EventsRepository.sync().
 * Завдяки цьому застосунок відкривається миттєво й працює без мережі.
 *
 * РІШЕННЯ ПРО ФІЛЬТРАЦІЮ, окремо для кожного екрана:
 * з сервера тягнемо ВСЮ стрічку без параметрів upcoming / date_from / date_to.
 * Причина: на сервері це SQL-порівняння по полю `date`, і вони мовчки
 * відкидають усі події з date == null. Такі події існують за задумом системи
 * й мають лишатися доступними. Тому фільтруємо в пам'яті:
 *   • вкладки «Місяць»/«Тиждень»/«День» самі відбирають події з датою;
 *   • вкладка «Події» показує всі, а події без дати збирає в секцію «Без дати».
 */
class EventsViewModel(
    private val repository: EventsRepository,
) : ViewModel() {

    /** Стан останньої синхронізації — окремо від даних, які йдуть із кешу. */
    private data class SyncStatus(
        val inProgress: Boolean = false,
        val error: AppError? = null,
        val serverReachable: Boolean = false,
        /** Чи завершилася хоч одна спроба: до цього показуємо Loading, а не Empty. */
        val completed: Boolean = false,
    )

    private val syncStatus = MutableStateFlow(SyncStatus())

    /** Останній знімок кешу — потрібен для findById на екрані деталей. */
    @Volatile
    private var latestEvents: List<Event> = emptyList()

    val state: StateFlow<EventsUiState> =
        combine(repository.observeEvents(), syncStatus) { events, sync ->
            latestEvents = events
            when {
                // Дані є — показуємо їх навіть під час помилки синхронізації,
                // додавши банер «офлайн». Кеш кращий за порожній екран.
                events.isNotEmpty() -> EventsUiState.Success(
                    events = events,
                    isRefreshing = sync.inProgress,
                    isOffline = sync.error != null,
                )

                !sync.completed -> EventsUiState.Loading

                sync.error != null -> EventsUiState.Error(
                    error = sync.error,
                    serverReachable = sync.serverReachable,
                )

                else -> EventsUiState.Empty
            }
        }.stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(STOP_TIMEOUT_MILLIS),
            initialValue = EventsUiState.Loading,
        )

    /** Обраний день на вкладках «Місяць» і «День». */
    private val _selectedDate = MutableStateFlow(LocalDate.now())
    val selectedDate: StateFlow<LocalDate> = _selectedDate.asStateFlow()

    /**
     * Фільтр за цільовою аудиторією. Поле target_audience — вільний текст,
     * а не перелік, тому це підрядковий пошук, а не вибір зі списку.
     */
    private val _audienceFilter = MutableStateFlow("")
    val audienceFilter: StateFlow<String> = _audienceFilter.asStateFlow()

    init {
        refresh()
    }

    fun selectDate(date: LocalDate) {
        _selectedDate.value = date
    }

    fun setAudienceFilter(value: String) {
        _audienceFilter.value = value
    }

    /** Синхронізує кеш із сервером. Дані на екрані оновляться самі — через Room. */
    fun refresh() {
        viewModelScope.launch { runSync() }
    }

    private suspend fun runSync() {
        if (syncStatus.value.inProgress) return
        syncStatus.update { it.copy(inProgress = true) }

        when (val result = repository.sync()) {
            is DataResult.Success -> syncStatus.value = SyncStatus(
                inProgress = false,
                error = null,
                serverReachable = true,
                completed = true,
            )

            is DataResult.Failure -> {
                // Відрізняємо «сервер лежить» від «стрічка порожня»:
                // без цього обидва випадки виглядають як порожній екран.
                val reachable = repository.isServerReachable()
                syncStatus.value = SyncStatus(
                    inProgress = false,
                    error = result.error,
                    serverReachable = reachable,
                    completed = true,
                )
            }
        }
    }

    /** Пошук події для екрана деталей — БЕЗ мережевого запиту. */
    fun findById(eventId: Int): Event? = latestEvents.firstOrNull { it.id == eventId }

    /**
     * Знаходить подію для переходу з push-сповіщення.
     *
     * Push приходить у момент підтвердження події, тож її цілком може ще не
     * бути в локальному кеші. Тоді один раз синхронізуємось і читаємо кеш
     * ЗНОВУ напряму — покладатися тут на потік Room не можна, бо між записом
     * у базу та новим знімком у StateFlow є проміжок.
     *
     * Це не порушує заборону на мережевий запит за подією: `GET /events/{id}`
     * не викликається, ми лише оновлюємо кеш звичайною синхронізацією.
     */
    suspend fun awaitEvent(eventId: Int): Event? {
        repository.findEvent(eventId)?.let { return it }
        runSync()
        return repository.findEvent(eventId)
    }

    companion object {
        private const val STOP_TIMEOUT_MILLIS = 5_000L

        fun factory(repository: EventsRepository): ViewModelProvider.Factory =
            object : ViewModelProvider.Factory {
                @Suppress("UNCHECKED_CAST")
                override fun <T : ViewModel> create(
                    modelClass: Class<T>,
                    extras: CreationExtras,
                ): T = EventsViewModel(repository) as T
            }
    }
}
