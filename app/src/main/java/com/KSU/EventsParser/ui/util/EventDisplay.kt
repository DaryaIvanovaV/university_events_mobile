package com.KSU.EventsParser.ui.util

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import com.KSU.EventsParser.R
import com.KSU.EventsParser.core.util.AppDateFormat
import com.KSU.EventsParser.domain.model.Event
import com.KSU.EventsParser.domain.model.EventType
import java.util.Locale

/**
 * Перетворення полів події на текст для екрана.
 *
 * Головне правило: жодне поле не показується як «null» і жодна дата не
 * вигадується. Якщо значення немає — показуємо явну українську заглушку
 * («дата уточнюється»), щоб користувач бачив саме відсутність даних,
 * а не помилку застосунку.
 */

/** Поточна локаль інтерфейсу — може відрізнятися від системної. */
@Composable
fun rememberAppLocale(): Locale {
    val configuration = LocalContext.current.resources.configuration
    return remember(configuration) { configuration.locales[0] }
}

@Composable
fun EventType.label(): String = stringResource(
    when (this) {
        EventType.CONFERENCE -> R.string.type_conference
        EventType.LECTURE -> R.string.type_lecture
        EventType.WEBINAR -> R.string.type_webinar
        EventType.SEMINAR -> R.string.type_seminar
        EventType.WORKSHOP -> R.string.type_workshop
        EventType.HACKATHON -> R.string.type_hackathon
        EventType.DEADLINE -> R.string.type_deadline
        EventType.EXAM -> R.string.type_exam
        EventType.CONSULTATION -> R.string.type_consultation
        EventType.OTHER -> R.string.type_other
    }
)

/** «3 квітня», або сирий рядок, якщо дата не розібралася, або заглушка. */
@Composable
fun Event.dateText(): String {
    val locale = rememberAppLocale()
    return date?.let { AppDateFormat.dayMonth(it, locale) }
        ?: dateRaw?.takeIf { it.isNotBlank() }
        ?: stringResource(R.string.date_unspecified)
}

/** «3 квітня 2026» — розгорнутий варіант для екрана деталей. */
@Composable
fun Event.fullDateText(): String {
    val locale = rememberAppLocale()
    return date?.let { AppDateFormat.dayMonthYear(it, locale) }
        ?: dateRaw?.takeIf { it.isNotBlank() }
        ?: stringResource(R.string.date_unspecified)
}

/**
 * «12:00». Якщо час не розібрався у LocalTime — показуємо сирий рядок як є:
 * на сервері немає валідатора цього поля, і значення на кшталт "11:00-16:00"
 * історично просочувалися. Викинути їх було б гірше, ніж показати.
 */
@Composable
fun Event.timeText(): String {
    return time?.let { AppDateFormat.time(it) }
        ?: timeRaw?.takeIf { it.isNotBlank() }
        ?: stringResource(R.string.time_unspecified)
}

@Composable
fun Event.locationText(): String =
    location?.takeIf { it.isNotBlank() } ?: stringResource(R.string.location_unspecified)

@Composable
fun Event.organizerText(): String =
    organizer?.takeIf { it.isNotBlank() } ?: stringResource(R.string.organizer_unspecified)

@Composable
fun Event.descriptionText(): String =
    description?.takeIf { it.isNotBlank() } ?: stringResource(R.string.description_unspecified)

/**
 * Чи підходить подія під фільтр за цільовою аудиторією.
 *
 * `target_audience` — вільний текст («2 та 3 курси, усі охочі», «група КН-41»),
 * а не перелік, тому порівнюємо ПІДРЯДКОМ без урахування регістру.
 * Порожній фільтр пропускає всі події, зокрема й ті, де аудиторія не вказана.
 */
fun Event.matchesAudience(filter: String): Boolean {
    val query = filter.trim()
    if (query.isEmpty()) return true
    return targetAudience?.contains(query, ignoreCase = true) == true
}

/** Рядок «дата, час» для екрана деталей. */
@Composable
fun Event.dateTimeText(): String {
    val datePart = fullDateText()
    val timePart = timeText()
    return "$datePart, $timePart"
}
