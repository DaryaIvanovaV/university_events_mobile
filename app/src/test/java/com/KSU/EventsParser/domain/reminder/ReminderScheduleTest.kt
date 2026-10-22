package com.KSU.EventsParser.domain.reminder

import com.KSU.EventsParser.domain.model.Event
import com.KSU.EventsParser.domain.model.EventType
import com.KSU.EventsParser.domain.reminder.ReminderSchedule.Availability
import java.time.Duration
import java.time.Instant
import java.time.LocalDate
import java.time.LocalDateTime
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

    // ─── isSameDay ───────────────────────────────────────────────────────────

    @Test
    fun isSameDay_sameDayTrigger_isTrue() {
        // DW-1.1/1.3: подія о 16:00, за 3 год → 13:00 тієї самої доби.
        val s = start(2026, 10, 20, 16, 0)
        val trigger = ReminderSchedule.triggerAt(s, 180)
        assertEquals(LocalTime.of(13, 0), trigger.toLocalTime())
        assertTrue(ReminderSchedule.isSameDay(s, trigger))
    }

    @Test
    fun isSameDay_triggerBeforeMidnight_isFalse() {
        // DW-1.2/1.3: подія о 00:30, за годину → 23:30 ПОПЕРЕДНЬОЇ доби.
        val s = start(2026, 10, 20, 0, 30)
        val trigger = ReminderSchedule.triggerAt(s, 60)
        assertEquals(start(2026, 10, 19, 23, 30), trigger)
        assertFalse(ReminderSchedule.isSameDay(s, trigger))
    }

    @Test
    fun isSameDay_triggerExactlyAtMidnight_isTrue() {
        // Межа доби: 00:30 мінус 30 хв = 00:00 тієї самої дати.
        val s = start(2026, 10, 20, 0, 30)
        assertTrue(ReminderSchedule.isSameDay(s, ReminderSchedule.triggerAt(s, 30)))
    }

    @Test
    fun isSameDay_wholeDayOffset_isFalse() {
        // DW-1.3: зсув у цілу добу завжди переносить спрацювання на іншу дату.
        val s = start(2026, 10, 20, 16, 0)
        val trigger = ReminderSchedule.triggerAt(s, 1440)
        assertEquals(LocalDate.of(2026, 10, 19), trigger.toLocalDate())
        assertFalse(ReminderSchedule.isSameDay(s, trigger))
    }

    @Test
    fun isSameDay_zeroOffset_isTrue() {
        val s = start(2026, 10, 20, 16, 0)
        assertTrue(ReminderSchedule.isSameDay(s, ReminderSchedule.triggerAt(s, 0)))
    }

    @Test
    fun isSameDay_triggerInOtherZone_comparesInZoneOfStart() {
        // Порівняння йде за поясом початку, а не поясом самого trigger.
        val s = start(2026, 10, 20, 0, 30)
        // 23:30 Києва 19-го = 20:30 UTC 19-го: інша доба в обох поясах.
        val triggerUtc = start(2026, 10, 19, 23, 30).withZoneSameInstant(ZoneId.of("UTC"))
        assertFalse(ReminderSchedule.isSameDay(s, triggerUtc))
        // 22:00 UTC 19-го = 01:00 Києва 20-го: за UTC це «19-те», за Києвом — «20-те».
        val lateUtc = ZonedDateTime.of(2026, 10, 19, 22, 0, 0, 0, ZoneId.of("UTC"))
        assertTrue(ReminderSchedule.isSameDay(s, lateUtc))
    }

    @Test
    fun isSameDay_oneDayOffsetAcrossDstSwitch_isFalse() {
        // 25 жовтня — перехід на зимовий час; «за добу» усе одно інша дата.
        val s = start(2026, 10, 25, 10, 0)
        assertFalse(ReminderSchedule.isSameDay(s, ReminderSchedule.triggerAt(s, 1440)))
    }

    // ─── offsetFor / earliestTriggerDate ─────────────────────────────────────

    @Test
    fun offsetFor_pickedMomentRoundTripsThroughTriggerAt() {
        val s = start(2026, 10, 25, 10, 0)
        val picked = LocalDateTime.of(2026, 10, 24, 18, 45)
        val offset = ReminderSchedule.offsetFor(s, picked, now)
        assertEquals(15 * 60 + 15, offset)
        assertEquals(picked, ReminderSchedule.triggerAt(s, offset!!).toLocalDateTime())
    }

    @Test
    fun offsetFor_acrossDstSwitch_keepsPickedWallClock() {
        // Подія 25-го о 10:00 (вже зимовий час), нагадати 24-го о 10:30 (ще літній).
        // Реально між ними 24,5 год, але обраний настінний час має зберегтися.
        val s = start(2026, 10, 25, 10, 0)
        val picked = LocalDateTime.of(2026, 10, 24, 10, 30)
        val offset = ReminderSchedule.offsetFor(s, picked, now)!!
        assertEquals(picked, ReminderSchedule.triggerAt(s, offset).toLocalDateTime())
    }

    @Test
    fun offsetFor_atOrAfterStart_isNull() {
        val s = start(2026, 10, 25, 10, 0)
        assertNull(ReminderSchedule.offsetFor(s, LocalDateTime.of(2026, 10, 25, 10, 0), now))
        assertNull(ReminderSchedule.offsetFor(s, LocalDateTime.of(2026, 10, 25, 11, 0), now))
    }

    @Test
    fun offsetFor_inPast_isNull() {
        // Зараз 19-го 12:00 — 11:59 того ж дня вже минуло.
        val s = start(2026, 10, 25, 10, 0)
        assertNull(ReminderSchedule.offsetFor(s, LocalDateTime.of(2026, 10, 19, 11, 59), now))
        assertEquals(
            LocalDateTime.of(2026, 10, 19, 12, 1),
            ReminderSchedule.triggerAt(
                s,
                ReminderSchedule.offsetFor(s, LocalDateTime.of(2026, 10, 19, 12, 1), now)!!,
            ).toLocalDateTime(),
        )
    }

    @Test
    fun offsetFor_beyondMax_isNull() {
        val s = start(2026, 12, 31, 10, 0)
        assertNull(ReminderSchedule.offsetFor(s, LocalDateTime.of(2026, 12, 1, 9, 59), now))
        assertEquals(ReminderOffsets.MAX, ReminderSchedule.offsetFor(s, LocalDateTime.of(2026, 12, 1, 10, 0), now))
    }

    @Test
    fun earliestTriggerDate_nearEvent_isToday() {
        assertEquals(
            LocalDate.of(2026, 10, 19),
            ReminderSchedule.earliestTriggerDate(start(2026, 10, 25, 10, 0), now),
        )
    }

    @Test
    fun earliestTriggerDate_farEvent_isLimitedSoAnyTimeFitsMax() {
        // Подія 31 грудня о 00:30: найраніша дата — 2 грудня, бо 2 грудня 00:00
        // ще вміщається в 30 діб, а 1 грудня о 23:00 — уже ні.
        val s = start(2026, 12, 31, 0, 30)
        val earliest = ReminderSchedule.earliestTriggerDate(s, now)
        assertEquals(LocalDate.of(2026, 12, 2), earliest)
        assertTrue(ReminderSchedule.offsetFor(s, earliest.atStartOfDay(), now) != null)
    }

    // ─── defaultOffset ───────────────────────────────────────────────────────

    @Test
    fun defaultOffset_farEvent_isOneHour() {
        assertEquals(60, ReminderSchedule.defaultOffset(start(2026, 10, 25, 10, 0), now))
    }

    @Test
    fun defaultOffset_eventInFortyMinutes_isLargestFittingFallback() {
        // До початку 40 хвилин: година не вміщається, 30 хв — так.
        val s = start(2026, 10, 19, 12, 40)
        assertEquals(30, ReminderSchedule.defaultOffset(s, now))
    }

    @Test
    fun defaultOffset_eventInTenMinutes_isFive() {
        val s = start(2026, 10, 19, 12, 10)
        assertEquals(5, ReminderSchedule.defaultOffset(s, now))
    }

    @Test
    fun defaultOffset_eventInThreeMinutes_isNull() {
        // Жоден запасний зсув (мінімум 5 хв) не вміщається — UI відкриє вибір часу.
        val s = start(2026, 10, 19, 12, 3)
        assertNull(ReminderSchedule.defaultOffset(s, now))
    }

    // ─── ReminderOffsets ─────────────────────────────────────────────────────

    @Test
    fun limits() {
        assertEquals(60, ReminderOffsets.DEFAULT)
        assertEquals(43_200, ReminderOffsets.MAX)
    }

    @Test
    fun split_daysHoursMinutes() {
        assertEquals(OffsetParts(0, 0, 15), ReminderOffsets.split(15))
        assertEquals(OffsetParts(0, 1, 30), ReminderOffsets.split(90))
        assertEquals(OffsetParts(1, 0, 0), ReminderOffsets.split(1440))
        assertEquals(OffsetParts(1, 0, 30), ReminderOffsets.split(1470))
        assertEquals(OffsetParts(1, 1, 0), ReminderOffsets.split(1500))
        assertEquals(OffsetParts(30, 0, 0), ReminderOffsets.split(ReminderOffsets.MAX))
    }
}
