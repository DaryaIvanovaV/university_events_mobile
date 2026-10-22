package com.KSU.EventsParser.domain.model

import java.time.LocalDate
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class EventExpiryTest {

    /** «Сьогодні» у тестах: 21 жовтня 2026. */
    private val today = LocalDate.of(2026, 10, 21)

    private fun event(
        id: Int = 1,
        date: LocalDate? = LocalDate.of(2026, 10, 25),
        dateRaw: String? = date?.toString(),
    ) = Event(
        id = id,
        title = "Тест",
        date = date,
        dateRaw = dateRaw,
        time = null,
        timeRaw = null,
        location = null,
        organizer = null,
        targetAudience = null,
        type = EventType.OTHER,
        description = null,
        language = null,
        link = null,
        meetingCode = null,
        createdAt = null,
        updatedAt = null,
    )

    // ─── Event.isExpired (DW-2.1) ────────────────────────────────────────────

    @Test
    fun DW_2_1_isExpired_dayAfterEvent_isKept() {
        // Подія 20 жовтня, сьогодні 21-го: date + 1 == today — ще лишається.
        assertFalse(event(date = LocalDate.of(2026, 10, 20)).isExpired(today))
    }

    @Test
    fun DW_2_1_isExpired_twoDaysAfterEvent_isExpired() {
        // Подія 19 жовтня, сьогодні 21-го: date + 2 == today — видаляється.
        assertTrue(event(date = LocalDate.of(2026, 10, 19)).isExpired(today))
    }

    @Test
    fun DW_2_1_isExpired_nullDate_isKept() {
        assertFalse(event(date = null).isExpired(today))
    }

    @Test
    fun DW_2_1_isExpired_futureDate_isKept() {
        assertFalse(event(date = LocalDate.of(2026, 11, 1)).isExpired(today))
    }

    @Test
    fun isExpired_eventToday_isKept() {
        assertFalse(event(date = today).isExpired(today))
    }

    @Test
    fun isExpired_longAgo_isExpired() {
        assertTrue(event(date = LocalDate.of(2026, 9, 30)).isExpired(today))
    }

    @Test
    fun isExpired_acrossMonthBoundary_usesCalendarDays() {
        // 31 жовтня + 1 день = 1 листопада: у цей день ще лишається, з 2-го — ні.
        val e = event(date = LocalDate.of(2026, 10, 31))
        assertFalse(e.isExpired(LocalDate.of(2026, 11, 1)))
        assertTrue(e.isExpired(LocalDate.of(2026, 11, 2)))
    }

    // ─── expiredIds (DW-2.2) ─────────────────────────────────────────────────

    @Test
    fun DW_2_2_expiredIds_mixedList_returnsOnlyExpiredIds() {
        val events = listOf(
            event(id = 1, date = LocalDate.of(2026, 9, 30)),   // давно минула
            event(id = 2, date = LocalDate.of(2026, 10, 20)),  // межа: лишається
            event(id = 3, date = LocalDate.of(2026, 10, 19)),  // минула
            event(id = 4, date = LocalDate.of(2026, 12, 1)),   // майбутня
            event(id = 5, date = null),                        // без дати
        )
        assertEquals(listOf(1, 3), expiredIds(events, today))
    }

    @Test
    fun DW_2_2_expiredIds_unparseableDateRaw_notReturned() {
        // Так мапер представляє зіпсований рядок дати: date == null, dateRaw збережено.
        val broken = event(id = 7, date = null, dateRaw = "завтра-ввечері")
        assertEquals(emptyList<Int>(), expiredIds(listOf(broken), today))
    }

    @Test
    fun DW_2_2_expiredIds_allFuture_returnsEmpty() {
        val events = listOf(
            event(id = 1, date = LocalDate.of(2026, 10, 22)),
            event(id = 2, date = LocalDate.of(2027, 1, 1)),
        )
        assertEquals(emptyList<Int>(), expiredIds(events, today))
    }

    @Test
    fun expiredIds_emptyList_returnsEmpty() {
        assertEquals(emptyList<Int>(), expiredIds(emptyList(), today))
    }

    @Test
    fun expiredIds_allExpired_returnsAllIdsInOrder() {
        val events = listOf(
            event(id = 9, date = LocalDate.of(2026, 1, 1)),
            event(id = 3, date = LocalDate.of(2026, 2, 1)),
        )
        assertEquals(listOf(9, 3), expiredIds(events, today))
    }
}
