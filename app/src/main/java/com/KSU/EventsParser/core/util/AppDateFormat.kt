package com.KSU.EventsParser.core.util

import java.time.DayOfWeek
import java.time.LocalDate
import java.time.LocalTime
import java.time.format.DateTimeFormatter
import java.time.format.TextStyle
import java.util.Locale
import java.util.concurrent.ConcurrentHashMap

/**
 * Форматування дат під поточну локаль інтерфейсу.
 *
 * Локаль передається явно, а не береться з Locale.getDefault(): користувач може
 * обрати мову застосунку окремо від системної (див. AppLocale), і дати мають
 * збігатися з рештою інтерфейсу.
 *
 * Важлива відмінність шаблонів для української:
 *   MMMM — родовий відмінок, для дати з числом: «3 квітня»;
 *   LLLL — називний відмінок окремо, для заголовка місяця: «жовтень 2026».
 * Переплутати їх означає отримати «3 квітень» або «квітня 2026».
 */
object AppDateFormat {

    private val cache = ConcurrentHashMap<String, DateTimeFormatter>()

    private fun formatter(pattern: String, locale: Locale): DateTimeFormatter =
        cache.getOrPut("$pattern|${locale.toLanguageTag()}") {
            DateTimeFormatter.ofPattern(pattern, locale)
        }

    /** «Жовтень 2026» — заголовок екрана місяця. */
    fun monthYear(date: LocalDate, locale: Locale): String =
        formatter("LLLL yyyy", locale).format(date).replaceFirstChar { it.titlecase(locale) }

    /** «3 квітня» — заголовок дня, дата в картці. */
    fun dayMonth(date: LocalDate, locale: Locale): String =
        formatter("d MMMM", locale).format(date)

    /** «3 квітня 2026» — рядок дати на екрані деталей. */
    fun dayMonthYear(date: LocalDate, locale: Locale): String =
        formatter("d MMMM yyyy", locale).format(date)

    /** «Пн» — повний підпис дня тижня в денному вигляді. */
    fun weekdayShort(dayOfWeek: DayOfWeek, locale: Locale): String =
        dayOfWeek.getDisplayName(TextStyle.SHORT, locale)
            .replaceFirstChar { it.titlecase(locale) }

    /**
     * «12:00». Формат 24-годинний незалежно від локалі — так само, як його
     * задає сервер, і так само, як його бачив автор оголошення.
     */
    fun time(time: LocalTime): String = formatter("HH:mm", Locale.ROOT).format(time)
}
