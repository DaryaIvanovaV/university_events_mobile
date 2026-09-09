package com.KSU.EventsParser.ui.components

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.vector.addPathNodes
import androidx.compose.ui.unit.dp

/**
 * Іконки з макета.
 *
 * У макеті це вбудовані SVG у стилі Feather: сітка 24×24, лише обведення
 * завтовшки 2, круглі кінці й з’єднання, без заливки. Стандартні іконки
 * Material тут не підходять — вони заливні й іншої ваги, тому набір
 * відтворено з тих самих даних контурів, що й у HTML.
 *
 * Колір задається через параметр tint у Icon(): SolidColor(Black) нижче —
 * лише база, поверх якої накладається ColorFilter.
 */
private fun featherIcon(
    name: String,
    strokes: List<String> = emptyList(),
    fills: List<String> = emptyList(),
): ImageVector = ImageVector.Builder(
    name = name,
    defaultWidth = 24.dp,
    defaultHeight = 24.dp,
    viewportWidth = 24f,
    viewportHeight = 24f,
).apply {
    fills.forEach { data ->
        addPath(
            pathData = addPathNodes(data),
            fill = SolidColor(Color.Black),
        )
    }
    strokes.forEach { data ->
        addPath(
            pathData = addPathNodes(data),
            stroke = SolidColor(Color.Black),
            strokeLineWidth = 2f,
            strokeLineCap = StrokeCap.Round,
            strokeLineJoin = StrokeJoin.Round,
        )
    }
}.build()

/** Прямокутник зі скругленням 2 — основа іконок календаря. */
private const val CALENDAR_FRAME =
    "M5 4H19A2 2 0 0 1 21 6V20A2 2 0 0 1 19 22H5A2 2 0 0 1 3 20V6A2 2 0 0 1 5 4Z"

object AppIcons {

    /** Вкладка «Місяць». */
    val Calendar: ImageVector by lazy {
        featherIcon(
            "calendar",
            strokes = listOf(CALENDAR_FRAME, "M16 2V6", "M8 2V6", "M3 10H21"),
        )
    }

    /** Вкладка «Тиждень». */
    val Week: ImageVector by lazy {
        featherIcon(
            "week",
            strokes = listOf(CALENDAR_FRAME, "M3 10H21", "M8 4V22", "M16 4V22"),
        )
    }

    /** Вкладка «День». */
    val Grid: ImageVector by lazy {
        featherIcon(
            "grid",
            strokes = listOf(
                "M3 3H10V10H3Z",
                "M14 3H21V10H14Z",
                "M3 14H10V21H3Z",
                "M14 14H21V21H14Z",
            ),
        )
    }

    /** Вкладка «Події». */
    val List: ImageVector by lazy {
        featherIcon(
            "list",
            strokes = listOf("M8 6H21", "M8 12H21", "M8 18H21"),
            fills = listOf(
                "M1.5 6A1.5 1.5 0 1 1 4.5 6A1.5 1.5 0 1 1 1.5 6Z",
                "M1.5 12A1.5 1.5 0 1 1 4.5 12A1.5 1.5 0 1 1 1.5 12Z",
                "M1.5 18A1.5 1.5 0 1 1 4.5 18A1.5 1.5 0 1 1 1.5 18Z",
            ),
        )
    }

    /** Вкладка «Налаштування». */
    val Settings: ImageVector by lazy {
        featherIcon(
            "settings",
            strokes = listOf(
                "M9 12A3 3 0 1 1 15 12A3 3 0 1 1 9 12Z",
                "M19.4 15a1.65 1.65 0 0 0 .33 1.82l.06.06a2 2 0 0 1 0 2.83 2 2 0 0 1-2.83 0l-.06-.06a1.65 1.65 0 0 0-1.82-.33 " +
                    "1.65 1.65 0 0 0-1 1.51V21a2 2 0 0 1-2 2 2 2 0 0 1-2-2v-.09A1.65 1.65 0 0 0 9 19.4a1.65 1.65 0 0 0-1.82.33l-.06.06a2 2 0 0 1-2.83 0 " +
                    "2 2 0 0 1 0-2.83l.06-.06a1.65 1.65 0 0 0 .33-1.82 1.65 1.65 0 0 0-1.51-1H3a2 2 0 0 1-2-2 2 2 0 0 1 2-2h.09A1.65 1.65 0 0 0 4.6 9a1.65 " +
                    "1.65 0 0 0-.33-1.82l-.06-.06a2 2 0 0 1 0-2.83 2 2 0 0 1 2.83 0l.06.06a1.65 1.65 0 0 0 1.82.33H9a1.65 1.65 0 0 0 1-1.51V3a2 2 0 0 1 2-2 " +
                    "2 2 0 0 1 2 2v.09a1.65 1.65 0 0 0 1 1.51 1.65 1.65 0 0 0 1.82-.33l.06-.06a2 2 0 0 1 2.83 0 2 2 0 0 1 0 2.83l-.06.06a1.65 1.65 0 0 0-.33 " +
                    "1.82V9a1.65 1.65 0 0 0 1.51 1H21a2 2 0 0 1 2 2 2 2 0 0 1-2 2h-.09a1.65 1.65 0 0 0-1.51 1z",
            ),
        )
    }

    val Bell: ImageVector by lazy {
        featherIcon(
            "bell",
            strokes = listOf(
                "M18 8A6 6 0 0 0 6 8c0 7-3 9-3 9h18s-3-2-3-9",
                "M13.73 21a2 2 0 0 1-3.46 0",
            ),
        )
    }

    val ChevronLeft: ImageVector by lazy {
        featherIcon("chevronLeft", strokes = listOf("M15 18L9 12L15 6"))
    }

    val ChevronRight: ImageVector by lazy {
        featherIcon("chevronRight", strokes = listOf("M9 18L15 12L9 6"))
    }

    /** Місце проведення. */
    val Pin: ImageVector by lazy {
        featherIcon(
            "pin",
            strokes = listOf(
                "M21 10c0 7-9 13-9 13s-9-6-9-13a9 9 0 0 1 18 0z",
                "M9 10A3 3 0 1 1 15 10A3 3 0 1 1 9 10Z",
            ),
        )
    }

    /** Дата й час. */
    val Clock: ImageVector by lazy {
        featherIcon(
            "clock",
            strokes = listOf(
                "M12 2A10 10 0 1 1 12 22A10 10 0 1 1 12 2Z",
                "M12 6V12L16 14",
            ),
        )
    }

    /** Організатор. */
    val User: ImageVector by lazy {
        featherIcon(
            "user",
            strokes = listOf(
                "M20 21v-2a4 4 0 0 0-4-4H8a4 4 0 0 0-4 4v2",
                "M12 3A4 4 0 1 1 12 11A4 4 0 1 1 12 3Z",
            ),
        )
    }

    /** Цільова аудиторія. */
    val Users: ImageVector by lazy {
        featherIcon(
            "users",
            strokes = listOf(
                "M17 21v-2a4 4 0 0 0-4-4H5a4 4 0 0 0-4 4v2",
                "M9 3A4 4 0 1 1 9 11A4 4 0 1 1 9 3Z",
                "M23 21v-2a4 4 0 0 0-3-3.87",
                "M16 3.13a4 4 0 0 1 0 7.75",
            ),
        )
    }

    /** Зовнішнє посилання. */
    val ExternalLink: ImageVector by lazy {
        featherIcon(
            "externalLink",
            strokes = listOf(
                "M18 13v6a2 2 0 0 1-2 2H5a2 2 0 0 1-2-2V8a2 2 0 0 1 2-2h6",
                "M15 3H21V9",
                "M10 14L21 3",
            ),
        )
    }

    /** Плитка посилання. */
    val Globe: ImageVector by lazy {
        featherIcon(
            "globe",
            strokes = listOf(
                "M12 2A10 10 0 1 1 12 22A10 10 0 1 1 12 2Z",
                "M2 12H22",
                "M12 2a15 15 0 0 1 4 10 15 15 0 0 1-4 10 15 15 0 0 1-4-10 15 15 0 0 1 4-10z",
            ),
        )
    }

    /** Копіювання коду підключення. */
    val Copy: ImageVector by lazy {
        featherIcon(
            "copy",
            strokes = listOf(
                "M11 9H20A2 2 0 0 1 22 11V20A2 2 0 0 1 20 22H11A2 2 0 0 1 9 20V11A2 2 0 0 1 11 9Z",
                "M5 15H4a2 2 0 0 1-2-2V4a2 2 0 0 1 2-2h9a2 2 0 0 1 2 2v1",
            ),
        )
    }

    /** «Додати в календар». */
    val PlusCircle: ImageVector by lazy {
        featherIcon(
            "plusCircle",
            strokes = listOf(
                "M12 2A10 10 0 1 1 12 22A10 10 0 1 1 12 2Z",
                "M12 8V16",
                "M8 12H16",
            ),
        )
    }

    /** Позначка обраної мови. */
    val Check: ImageVector by lazy {
        featherIcon("check", strokes = listOf("M20 6L9 17L4 12"))
    }

    /** Очищення поля фільтра. */
    val Close: ImageVector by lazy {
        featherIcon("close", strokes = listOf("M18 6L6 18", "M6 6L18 18"))
    }

    /** Банер «офлайн» і стани помилок. */
    val AlertCircle: ImageVector by lazy {
        featherIcon(
            "alertCircle",
            strokes = listOf(
                "M12 2A10 10 0 1 1 12 22A10 10 0 1 1 12 2Z",
                "M12 7V13",
                "M12 17H12.01",
            ),
        )
    }
}
