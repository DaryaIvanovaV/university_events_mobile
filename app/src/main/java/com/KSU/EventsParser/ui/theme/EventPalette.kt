package com.KSU.EventsParser.ui.theme

import androidx.compose.runtime.Immutable
import androidx.compose.ui.graphics.Color
import com.KSU.EventsParser.domain.model.EventType

/**
 * Кольори типів подій.
 *
 * У макеті таких сімейств п'ять (conf / hack / open / exam / meet), а в API
 * типів десять. Тому п'ять відтінків узято з макета ДОСЛІВНО, а ще п'ять
 * дораховано за тією ж структурною формулою: пастельне тло (L≈0.93),
 * насичена крапка (L≈0.48) і темний текст (L≈0.28) на спільному тоні.
 *
 * `other` навмисно нейтрально-сірий: у нього потрапляє будь-який невідомий тип
 * із сервера, і він не повинен вдавати із себе осмислену категорію.
 */
@Immutable
data class EventColors(
    /** Тло чипа типу. */
    val bg: Color,
    /** Крапка в сітці календаря та ліва смуга картки. */
    val dot: Color,
    /** Текст на тлі [bg]. */
    val text: Color,
)

object EventPalette {

    // ── Взято з макета дослівно ────────────────────────────────────────────────
    /** EVENT_COLORS.conf — oklch(0.93 0.06 255) / (0.45 0.16 255) / (0.25 0.10 255) */
    private val Conference = EventColors(Color(0xFFCEEBFF), Color(0xFF0052AB), Color(0xFF001F50))

    /** EVENT_COLORS.meet — oklch(0.93 0.06 300) / (0.50 0.16 300) / (0.28 0.10 300) */
    private val Seminar = EventColors(Color(0xFFEEE0FF), Color(0xFF7347AF), Color(0xFF311852))

    /** EVENT_COLORS.hack — oklch(0.93 0.08 145) / (0.45 0.18 145) / (0.25 0.12 145) */
    private val Workshop = EventColors(Color(0xFFC7F7C7), Color(0xFF006C00), Color(0xFF002E00))

    /** EVENT_COLORS.exam — oklch(0.94 0.06 30) / (0.55 0.18 30) / (0.30 0.12 20) */
    private val Deadline = EventColors(Color(0xFFFFDDD4), Color(0xFFC53829), Color(0xFF5C0012))

    /** EVENT_COLORS.open — oklch(0.94 0.07 85) / (0.60 0.18 85) / (0.35 0.12 75) */
    private val Consultation = EventColors(Color(0xFFFFE8B6), Color(0xFFB07300), Color(0xFF5D2C00))

    // ── Дораховано за формулою макета для типів, яких у ньому не було ──────────
    /** тон 265 — синьо-фіолетовий, поруч із conference. */
    private val Lecture = EventColors(Color(0xFFD1E8FF), Color(0xFF2D54BC), Color(0xFF0D225E))

    /** тон 220 — блакитний. */
    private val Webinar = EventColors(Color(0xFFB4F4FF), Color(0xFF006E9E), Color(0xFF00324D))

    /** тон 165 — бірюзово-зелений, поруч із workshop. */
    private val Hackathon = EventColors(Color(0xFFBDF7DC), Color(0xFF007944), Color(0xFF00381A))

    /** тон 15 — малиновий, поруч із deadline. */
    private val Exam = EventColors(Color(0xFFFFD6D9), Color(0xFFA8203D), Color(0xFF520116))

    /** Нейтральний сірий — сюди ж потрапляє будь-який невідомий тип. */
    private val Other = EventColors(Color(0xFFE7ECF2), Color(0xFF6A727D), Color(0xFF293442))

    fun of(type: EventType): EventColors = when (type) {
        EventType.CONFERENCE -> Conference
        EventType.LECTURE -> Lecture
        EventType.WEBINAR -> Webinar
        EventType.SEMINAR -> Seminar
        EventType.WORKSHOP -> Workshop
        EventType.HACKATHON -> Hackathon
        EventType.DEADLINE -> Deadline
        EventType.EXAM -> Exam
        EventType.CONSULTATION -> Consultation
        EventType.OTHER -> Other
    }
}
