package com.KSU.EventsParser.domain.reminder

import com.KSU.EventsParser.domain.model.Event
import com.KSU.EventsParser.domain.model.EventType
import com.KSU.EventsParser.domain.reminder.ReminderSchedule.Availability
import java.time.Duration
import java.time.Instant
import java.time.LocalDate
import java.time.LocalTime
import java.time.ZoneId
import java.time.ZonedDateTime
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class ReminderScheduleTest {

    private val zone = ZoneId.of("Europe/Kyiv")
    private val fallback = LocalTime.of(9, 0)

    /** Фіксований «зараз»: 2026-10-19 12:00 за Києвом. */
    private val now: Instant = ZonedDateTime.of(2026, 10, 19, 12, 0, 0, 0, zone).toInstant()

    private fun event(
        date: LocalDate? = LocalDate.of(2026, 10, 25),
        time: LocalTime? = LocalTime.of(10, 0),
        timeRaw: String? = time?.toString(),
    ) = Event(
        id = 1,
        title = "Тест",
        date = date,
        dateRaw = date?.toString(),
        time = time,
        timeRaw = timeRaw,
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

    private fun start(year: Int, month: Int, day: Int, hour: Int, minute: Int = 0) =
        ZonedDateTime.of(year, month, day, hour, minute, 0, 0, zone)

    // ─── eventStart ──────────────────────────────────────────────────────────

    @Test
    fun eventStart_withTime_usesEventTime() {
        val result = ReminderSchedule.eventStart(event(time = LocalTime.of(14, 30)), zone, fallback)
        assertEquals(start(2026, 10, 25, 14, 30), result)
    }

    @Test
    fun eventStart_withoutTime_usesFallback0900() {
        val result = ReminderSchedule.eventStart(event(time = null), zone, fallback)
        assertEquals(start(2026, 10, 25, 9, 0), result)
    }

    @Test
    fun eventStart_unparsedTimeRaw_usesFallbackAndDoesNotInventTime() {
        // "11:00-16:00" не розібралося в LocalTime: час із рядка не вигадуємо.
        val result = ReminderSchedule.eventStart(
            event(time = null, timeRaw = "11:00-16:00"),
            zone,
            fallback,
        )
        assertEquals(start(2026, 10, 25, 9, 0), result)
    }

    @Test
    fun eventStart_withoutDate_isNull() {
        assertNull(ReminderSchedule.eventStart(event(date = null), zone, fallback))
    }

    // ─── availability ────────────────────────────────────────────────────────

    @Test
    fun availability_noDate() {
        assertEquals(Availability.NoDate, ReminderSchedule.availability(event(date = null), now, zone, fallback))
    }

    @Test
    fun availability_startedEvent() {
        val past = event(date = LocalDate.of(2026, 10, 18))
        assertEquals(Availability.Started, ReminderSchedule.availability(past, now, zone, fallback))
    }

    @Test
    fun availability_eventStartingExactlyNow_isStarted() {
        val atNow = event(date = LocalDate.of(2026, 10, 19), time = LocalTime.of(12, 0))
        assertEquals(Availability.Started, ReminderSchedule.availability(atNow, now, zone, fallback))
    }

    @Test
    fun availability_futureEventWithTime_doesNotUseFallback() {
        val result = ReminderSchedule.availability(event(), now, zone, fallback)
        assertEquals(Availability.Available(start(2026, 10, 25, 10, 0), usesFallbackTime = false), result)
    }

    @Test
    fun availability_futureEventWithoutTime_usesFallback() {
        val result = ReminderSchedule.availability(event(time = null), now, zone, fallback)
        assertEquals(Availability.Available(start(2026, 10, 25, 9, 0), usesFallbackTime = true), result)
    }

    @Test
    fun availability_todayWithoutTime_afterFallbackTime_isStarted() {
        // Сьогодні без часу: 09:00 уже минуло (зараз 12:00), тож відліку немає.
        val today = event(date = LocalDate.of(2026, 10, 19), time = null)
        assertEquals(Availability.Started, ReminderSchedule.availability(today, now, zone, fallback))
    }

    // ─── triggerAt / isOffsetValid ───────────────────────────────────────────

    @Test
    fun triggerAt_minutesAndHours() {
        val s = start(2026, 10, 25, 10, 0)
        assertEquals(start(2026, 10, 25, 9, 45), ReminderSchedule.triggerAt(s, 15))
        assertEquals(start(2026, 10, 25, 9, 0), ReminderSchedule.triggerAt(s, 60))
        assertEquals(start(2026, 10, 25, 7, 0), ReminderSchedule.triggerAt(s, 180))
    }

    @Test
    fun triggerAt_oneDay_keepsLocalTimeAcrossDstSwitch() {
        // Зимовий час у Києві настає 2026-10-25 о 04:00 → 03:00.
        // Подія 25-го о 10:00 (UTC+2); «за добу» — це 24-го о 10:00 (UTC+3), а не о 09:00.
        val s = start(2026, 10, 25, 10, 0)
        val trigger = ReminderSchedule.triggerAt(s, 1440)

        assertEquals(LocalDate.of(2026, 10, 24), trigger.toLocalDate())
        assertEquals(LocalTime.of(10, 0), trigger.toLocalTime())
        // Реальний проміжок між спрацюванням і подією — 25 годин, бо доба на межі DST довша.
        assertEquals(Duration.ofHours(25), Duration.between(trigger, s))
    }

    @Test
    fun triggerAt_dayPlusHours_subtractsDaysInLocalTimeFirst() {
        // 1500 хв = 1 доба + 1 година.
        val s = start(2026, 10, 25, 10, 0)
        val trigger = ReminderSchedule.triggerAt(s, 1500)
        assertEquals(LocalDate.of(2026, 10, 24), trigger.toLocalDate())
        assertEquals(LocalTime.of(9, 0), trigger.toLocalTime())
    }

    @Test
    fun isOffsetValid_triggerInFuture_isTrue() {
        // Подія завтра о 12:00: навіть за 1439 хвилин спрацювання (сьогодні 12:01) ще попереду.
        val s = start(2026, 10, 20, 12, 0)
        assertTrue(ReminderSchedule.isOffsetValid(s, 15, now))
        assertTrue(ReminderSchedule.isOffsetValid(s, 1439, now))
    }

    @Test
    fun isOffsetValid_triggerExactlyNow_isFalse() {
        val s = start(2026, 10, 20, 12, 0)
        // Рівно за добу від 20-го 12:00 — це 19-те 12:00, тобто «зараз»: уже не майбутнє.
        assertFalse(ReminderSchedule.isOffsetValid(s, 1440, now))
    }

    @Test
    fun isOffsetValid_triggerInPast_isFalse() {
        val s = start(2026, 10, 19, 13, 0)
        assertFalse(ReminderSchedule.isOffsetValid(s, 180, now))
        assertFalse(ReminderSchedule.isOffsetValid(s, 60, now))
        assertTrue(ReminderSchedule.isOffsetValid(s, 30, now))
    }

    // ─── defaultOffset ───────────────────────────────────────────────────────

    @Test
    fun defaultOffset_farEvent_isOneHour() {
        assertEquals(60, ReminderSchedule.defaultOffset(start(2026, 10, 25, 10, 0), now))
    }

    @Test
    fun defaultOffset_eventInFortyMinutes_isLargestFittingPreset() {
        // До початку 40 хвилин: година не вміщається, 30 хв — так, 15 хв — так.
        val s = start(2026, 10, 19, 12, 40)
        assertEquals(30, ReminderSchedule.defaultOffset(s, now))
    }

    @Test
    fun defaultOffset_eventInTwentyMinutes_isFifteen() {
        val s = start(2026, 10, 19, 12, 20)
        assertEquals(15, ReminderSchedule.defaultOffset(s, now))
    }

    @Test
    fun defaultOffset_eventInTenMinutes_isNull() {
        // Жоден пресет (мінімум 15 хв) не вміщається — UI відкриє власне значення.
        val s = start(2026, 10, 19, 12, 10)
        assertNull(ReminderSchedule.defaultOffset(s, now))
    }

    // ─── ReminderOffsets ─────────────────────────────────────────────────────

    @Test
    fun presets_andLimits() {
        assertEquals(listOf(15, 30, 60, 180, 1440), ReminderOffsets.PRESETS)
        assertEquals(60, ReminderOffsets.DEFAULT)
        assertEquals(43_200, ReminderOffsets.MAX)
    }

    @Test
    fun decompose_picksLargestExactUnit() {
        assertEquals(OffsetAmount(15, OffsetUnit.MINUTES), ReminderOffsets.decompose(15))
        assertEquals(OffsetAmount(90, OffsetUnit.MINUTES), ReminderOffsets.decompose(90))
        assertEquals(OffsetAmount(1, OffsetUnit.HOURS), ReminderOffsets.decompose(60))
        assertEquals(OffsetAmount(3, OffsetUnit.HOURS), ReminderOffsets.decompose(180))
        assertEquals(OffsetAmount(1, OffsetUnit.DAYS), ReminderOffsets.decompose(1440))
        assertEquals(OffsetAmount(30, OffsetUnit.DAYS), ReminderOffsets.decompose(ReminderOffsets.MAX))
        // 25 годин не ділиться на доби націло — лишаємо години.
        assertEquals(OffsetAmount(25, OffsetUnit.HOURS), ReminderOffsets.decompose(1500))
    }

    @Test
    fun offsetAmount_toMinutes_isInverseOfDecompose() {
        for (minutes in listOf(1, 15, 60, 90, 180, 1440, 1500, 2880, ReminderOffsets.MAX)) {
            assertEquals(minutes, ReminderOffsets.decompose(minutes).toMinutes())
        }
    }
}
