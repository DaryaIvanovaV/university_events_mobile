package com.KSU.EventsParser.domain.model

import java.time.LocalDate

/**
 * Подія вважається минулою, коли від її дати минула ще й одна повна доба:
 * подія від 19 жовтня лишається в кеші весь день 20-го і зникає з 21-го.
 *
 * Подія без дати (або з датою, яка не розібралася й тому стала `null`)
 * НЕ видаляється ніколи: не знаючи дати, ми не можемо стверджувати, що вона минула.
 *
 * [today] передає викликач — це «сьогодні» за київським часом, а не за годинником пристрою.
 */
fun Event.isExpired(today: LocalDate): Boolean =
    date != null && date.plusDays(1) < today

/** Ідентифікатори подій, які [isExpired] на день [today]. */
fun expiredIds(events: List<Event>, today: LocalDate): List<Int> =
    events.filter { it.isExpired(today) }.map { it.id }
