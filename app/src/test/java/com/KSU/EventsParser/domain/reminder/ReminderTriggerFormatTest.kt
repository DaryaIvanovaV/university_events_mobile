package com.KSU.EventsParser.domain.reminder

import com.KSU.EventsParser.core.util.AppDateFormat
import java.time.ZoneId
import java.time.ZonedDateTime
import java.util.Locale
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * Складові підзаголовка нагадування: момент спрацювання, вибір «лише час» чи «дата й час»
 * і текст дати/часу в обох локалях. Шаблони «Нагадаю о %1$s» / «Нагадаю %1$s о %2$s»
 * живуть у strings.xml, тут перевіряється все, що їх наповнює.
 */
class ReminderTriggerFormatTest {

    private val zone = ZoneId.of("Europe/Kyiv")
    private val uk = Locale.forLanguageTag("uk")
    private val en = Locale.ENGLISH

    private fun start(day: Int, hour: Int, minute: Int = 0) =
        ZonedDateTime.of(2026, 10, day, hour, minute, 0, 0, zone)

    @Test
    fun sameDay_DW_1_1_eventAt1600_threeHours_timeOnly13_00() {
        val s = start(20, 16)
        val trigger = ReminderSchedule.triggerAt(s, 180)

        assertTrue(ReminderSchedule.isSameDay(s, trigger))
        assertEquals("13:00", AppDateFormat.time(trigger.toLocalTime()))
    }

    @Test
    fun crossMidnight_DW_1_2_eventAt0030_oneHour_previousDayAnd23_30() {
        val s = start(20, 0, 30)
        val trigger = ReminderSchedule.triggerAt(s, 60)

        assertFalse(ReminderSchedule.isSameDay(s, trigger))
        assertEquals("19 жовтня", AppDateFormat.dayMonth(trigger.toLocalDate(), uk))
        assertEquals("19 October", AppDateFormat.dayMonth(trigger.toLocalDate(), en))
        assertEquals("23:30", AppDateFormat.time(trigger.toLocalTime()))
    }

    @Test
    fun wholeDay_eventAt1600_oneDay_previousDayAtSameTime() {
        val s = start(21, 16)
        val trigger = ReminderSchedule.triggerAt(s, 1440)

        assertFalse(ReminderSchedule.isSameDay(s, trigger))
        // Родовий відмінок для дати з числом: «20 жовтня», а не «20 жовтень».
        assertEquals("20 жовтня", AppDateFormat.dayMonth(trigger.toLocalDate(), uk))
        assertEquals("20 October", AppDateFormat.dayMonth(trigger.toLocalDate(), en))
        assertEquals("16:00", AppDateFormat.time(trigger.toLocalTime()))
    }

    @Test
    fun fallbackStart_0900_triggerCountedFromFallback() {
        // Подія без розібраного часу: відлік від 09:00, за 3 год → 06:00 тієї самої доби.
        val s = start(20, 9)
        val trigger = ReminderSchedule.triggerAt(s, 180)

        assertTrue(ReminderSchedule.isSameDay(s, trigger))
        assertEquals("06:00", AppDateFormat.time(trigger.toLocalTime()))
    }
}
