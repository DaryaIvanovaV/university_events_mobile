package com.KSU.EventsParser.ui.theme

import androidx.compose.material3.Typography
import androidx.compose.ui.text.ExperimentalTextApi
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontVariation
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.em
import androidx.compose.ui.unit.sp
import com.KSU.EventsParser.R

/**
 * Шрифти та стилі тексту з макета.
 *
 * У макеті вказані Libre Baskerville (заголовки) і DM Sans (текст). Обидва
 * перевірено розбором таблиці cmap: у них НЕМАЄ жодної кириличної гліфи —
 * увесь український текст мовчки замінявся б запасним шрифтом. Тому взято
 * найближчі відповідники з повним покриттям української (включно з Ґ ґ Є є І і Ї ї
 * та апострофом ’):
 *   Libre Baskerville → PT Serif  (перехідна антиква, розроблена під кирилицю)
 *   DM Sans           → Inter     (гротеск для інтерфейсів)
 *
 * CSS px переносимо в sp, CSS line-height (множник) — у lineHeight,
 * CSS letter-spacing в em — у letterSpacing (Compose підтримує .em напряму).
 */

/**
 * Inter — змінний шрифт з осями opsz і wght; реєструємо потрібні насиченості.
 * FontVariation ще позначено як експериментальний API, звідси OptIn.
 */
@OptIn(ExperimentalTextApi::class)
private fun interFont(weight: Int) = Font(
    resId = R.font.inter_variable,
    weight = FontWeight(weight),
    variationSettings = FontVariation.Settings(FontVariation.weight(weight)),
)

val InterFamily = FontFamily(
    interFont(300),
    interFont(400),
    interFont(500),
    interFont(600),
    interFont(700),
)

val PtSerifFamily = FontFamily(
    Font(R.font.pt_serif_regular, FontWeight.Normal),
    Font(R.font.pt_serif_bold, FontWeight.Bold),
)

object AppType {

    // ─── Шапка ─────────────────────────────────────────────────────────────────
    /** fontSize:11, letterSpacing:"0.06em", uppercase — «ХДУ». */
    val HeaderOverline = TextStyle(
        fontFamily = InterFamily,
        fontSize = 11.sp,
        fontWeight = FontWeight.Normal,
        letterSpacing = 0.06.em,
    )

    /** fontSize:20, Libre Baskerville 700, letterSpacing:"-0.01em". */
    val HeaderTitle = TextStyle(
        fontFamily = PtSerifFamily,
        fontSize = 20.sp,
        fontWeight = FontWeight.Bold,
        letterSpacing = (-0.01).em,
    )

    /** Кнопка «Назад»: fontSize:13. */
    val HeaderBack = TextStyle(
        fontFamily = InterFamily,
        fontSize = 13.sp,
        fontWeight = FontWeight.Normal,
    )

    // ─── Сітка місяця ──────────────────────────────────────────────────────────
    /** fontSize:11, fontWeight:600, letterSpacing:"0.05em". */
    val Weekday = TextStyle(
        fontFamily = InterFamily,
        fontSize = 11.sp,
        fontWeight = FontWeight.SemiBold,
        letterSpacing = 0.05.em,
    )

    /** fontSize:13, звичайний день. */
    val DayNumber = TextStyle(
        fontFamily = InterFamily,
        fontSize = 13.sp,
        fontWeight = FontWeight.Normal,
    )

    /** fontSize:13, fontWeight:700 — сьогодні. */
    val DayNumberToday = DayNumber.copy(fontWeight = FontWeight.Bold)

    // ─── Тижневий вигляд ───────────────────────────────────────────────────────
    /** fontSize:10, letterSpacing:"0.06em", uppercase. */
    val WeekStripWeekday = TextStyle(
        fontFamily = InterFamily,
        fontSize = 10.sp,
        letterSpacing = 0.06.em,
    )

    /** fontSize:9, fontWeight:600, lineHeight:1.3. */
    val WeekEventTitle = TextStyle(
        fontFamily = InterFamily,
        fontSize = 9.sp,
        fontWeight = FontWeight.SemiBold,
        lineHeight = 11.7.sp,
    )

    /** fontSize:8. */
    val WeekEventTime = TextStyle(
        fontFamily = InterFamily,
        fontSize = 8.sp,
    )

    // ─── Денний вигляд ─────────────────────────────────────────────────────────
    /** fontSize:18, Libre Baskerville 700. */
    val DayViewDate = TextStyle(
        fontFamily = PtSerifFamily,
        fontSize = 18.sp,
        fontWeight = FontWeight.Bold,
    )

    /** fontSize:12. */
    val DayViewWeekday = TextStyle(
        fontFamily = InterFamily,
        fontSize = 12.sp,
    )

    // ─── Картка події ──────────────────────────────────────────────────────────
    /** fontSize:13, fontWeight:600, lineHeight:1.3. */
    val CardTitle = TextStyle(
        fontFamily = InterFamily,
        fontSize = 13.sp,
        fontWeight = FontWeight.SemiBold,
        lineHeight = 16.9.sp,
    )

    /** fontSize:10 — дата над заголовком у списку. */
    val CardDate = TextStyle(
        fontFamily = InterFamily,
        fontSize = 10.sp,
    )

    /** fontSize:11 — час і місце. */
    val CardMeta = TextStyle(
        fontFamily = InterFamily,
        fontSize = 11.sp,
    )

    // ─── Чипи ──────────────────────────────────────────────────────────────────
    /** fontSize:10, fontWeight:500 — чип типу в картці. */
    val ChipSmall = TextStyle(
        fontFamily = InterFamily,
        fontSize = 10.sp,
        fontWeight = FontWeight.Medium,
    )

    /** fontSize:11, fontWeight:600 — чип типу в шапці деталей. */
    val ChipLarge = TextStyle(
        fontFamily = InterFamily,
        fontSize = 11.sp,
        fontWeight = FontWeight.SemiBold,
    )

    /** fontSize:11, fontWeight:500 — чип аудиторії. */
    val ChipTag = TextStyle(
        fontFamily = InterFamily,
        fontSize = 11.sp,
        fontWeight = FontWeight.Medium,
    )

    /** fontSize:12, fontWeight:500 — чип фільтра. */
    val ChipFilter = TextStyle(
        fontFamily = InterFamily,
        fontSize = 12.sp,
        fontWeight = FontWeight.Medium,
    )

    // ─── Екран деталей ─────────────────────────────────────────────────────────
    /** fontSize:20, serif 700, lineHeight:1.3. */
    val DetailTitle = TextStyle(
        fontFamily = PtSerifFamily,
        fontSize = 20.sp,
        fontWeight = FontWeight.Bold,
        lineHeight = 26.sp,
    )

    /** fontSize:11, fontWeight:600, letterSpacing:"0.06em", uppercase — підпис секції. */
    val SectionLabel = TextStyle(
        fontFamily = InterFamily,
        fontSize = 11.sp,
        fontWeight = FontWeight.SemiBold,
        letterSpacing = 0.06.em,
    )

    /** fontSize:10, letterSpacing:"0.05em", uppercase — підпис у рядку інформації. */
    val InfoLabel = TextStyle(
        fontFamily = InterFamily,
        fontSize = 10.sp,
        letterSpacing = 0.05.em,
    )

    /** fontSize:13, lineHeight:1.4 — значення в рядку інформації. */
    val InfoValue = TextStyle(
        fontFamily = InterFamily,
        fontSize = 13.sp,
        lineHeight = 18.2.sp,
    )

    /** fontSize:13, lineHeight:1.6 — опис. */
    val Description = TextStyle(
        fontFamily = InterFamily,
        fontSize = 13.sp,
        lineHeight = 20.8.sp,
    )

    /** fontSize:13, fontWeight:500 — назва посилання, підпис перемикача. */
    val RowTitle = TextStyle(
        fontFamily = InterFamily,
        fontSize = 13.sp,
        fontWeight = FontWeight.Medium,
    )

    /** fontSize:11 — адреса під назвою посилання, підзаголовок перемикача. */
    val RowSubtitle = TextStyle(
        fontFamily = InterFamily,
        fontSize = 11.sp,
    )

    // ─── Нижня навігація ───────────────────────────────────────────────────────
    /** fontSize:9, letterSpacing:"0.02em". */
    val NavLabel = TextStyle(
        fontFamily = InterFamily,
        fontSize = 9.sp,
        fontWeight = FontWeight.Normal,
        letterSpacing = 0.02.em,
    )

    val NavLabelActive = NavLabel.copy(fontWeight = FontWeight.SemiBold)

    // ─── Порожні стани ─────────────────────────────────────────────────────────
    /** fontSize:13. */
    val EmptyState = TextStyle(
        fontFamily = InterFamily,
        fontSize = 13.sp,
    )

    /** fontSize:32 — емодзі порожнього стану. */
    val EmptyStateIcon = TextStyle(
        fontFamily = InterFamily,
        fontSize = 32.sp,
    )
}

/**
 * Material 3 Typography — щоб стандартні компоненти успадкували Inter,
 * навіть якщо десь використано їхні типові стилі.
 */
val AppTypography = Typography().run {
    copy(
        displayLarge = displayLarge.copy(fontFamily = InterFamily),
        displayMedium = displayMedium.copy(fontFamily = InterFamily),
        displaySmall = displaySmall.copy(fontFamily = InterFamily),
        headlineLarge = headlineLarge.copy(fontFamily = PtSerifFamily),
        headlineMedium = headlineMedium.copy(fontFamily = PtSerifFamily),
        headlineSmall = headlineSmall.copy(fontFamily = PtSerifFamily),
        titleLarge = titleLarge.copy(fontFamily = PtSerifFamily),
        titleMedium = titleMedium.copy(fontFamily = InterFamily),
        titleSmall = titleSmall.copy(fontFamily = InterFamily),
        bodyLarge = bodyLarge.copy(fontFamily = InterFamily),
        bodyMedium = bodyMedium.copy(fontFamily = InterFamily),
        bodySmall = bodySmall.copy(fontFamily = InterFamily),
        labelLarge = labelLarge.copy(fontFamily = InterFamily),
        labelMedium = labelMedium.copy(fontFamily = InterFamily),
        labelSmall = labelSmall.copy(fontFamily = InterFamily),
    )
}
