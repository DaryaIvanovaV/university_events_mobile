package com.KSU.EventsParser.ui.theme

import androidx.compose.ui.graphics.Color

/**
 * Кольори з макета Design/CS Faculty Calendar.html.
 *
 * У макеті вони записані у форматі CSS oklch(), якого немає в Compose, тому кожне
 * значення переведено в sRGB чисельно (OKLab → linear sRGB → гамма-кодування),
 * а не підібрано на око. Поруч із кожним кольором лишено оригінальний запис oklch,
 * щоб можна було звірити з макетом.
 *
 * Використовується тема "navy" — вона стоїть у макеті за замовчуванням
 * (TWEAK_DEFAULTS.theme = "navy"). Схеми forest і burgundy не переносимо:
 * перемикача теми в застосунку немає.
 */
object AppColors {

    // ─── Основна палітра (THEMES.navy) ─────────────────────────────────────────
    /** oklch(0.28 0.10 255) — активний стан, вибраний день, активна вкладка. */
    val Primary = Color(0xFF002758)

    /** oklch(0.38 0.12 255) */
    val PrimaryMid = Color(0xFF064180)

    /** oklch(0.72 0.13 85) — акцент: сьогоднішній день, увімкнений перемикач. */
    val Accent = Color(0xFFCA9D33)

    /** oklch(0.95 0.06 85) */
    val AccentLight = Color(0xFFFFECC1)

    /** oklch(0.22 0.09 255) — тло шапки. */
    val HeaderBg = Color(0xFF001843)

    /** oklch(0.60 0.16 255) */
    val TodayBg = Color(0xFF3280DD)

    /** oklch(0.75 0.06 85) — надрядковий напис у шапці («ХДУ»). */
    val HeaderOverline = Color(0xFFD8AC5E)

    // ─── Поверхні ──────────────────────────────────────────────────────────────
    /** oklch(0.97 0.01 250) — тло застосунку. */
    val Background = Color(0xFFF0F6FC)

    /** Картки, сітка календаря, нижня навігація. */
    val Surface = Color(0xFFFFFFFF)

    /** oklch(0.93 0.01 250) — роздільник. */
    val Divider = Color(0xFFE3E8EE)

    /** oklch(0.95 0.01 250) — світліший роздільник між рядками посилань. */
    val DividerLight = Color(0xFFEAEFF5)

    /** oklch(0.92 0.01 250) — межа сітки календаря та верх нижньої навігації. */
    val GridBorder = Color(0xFFE0E5EB)

    /** oklch(0.93 0.02 250) — тло неактивного чипа-фільтра. */
    val ChipIdleBg = Color(0xFFDEE9F5)

    // ─── Текст ─────────────────────────────────────────────────────────────────
    /** oklch(0.15 0.06 255) — заголовок картки. */
    val TextPrimary = Color(0xFF000A23)

    /** oklch(0.18 0.06 255) — заголовки в картках деталей. */
    val TextTitle = Color(0xFF00112B)

    /** oklch(0.18 0.04 255) — число дня в сітці. */
    val TextDay = Color(0xFF051223)

    /** oklch(0.25 0.05 255) — основний текст опису. */
    val TextBody = Color(0xFF102239)

    /** oklch(0.35 0.06 255) — текст неактивного чипа. */
    val TextChipIdle = Color(0xFF243C59)

    /** oklch(0.45 0.06 255) — іконки навігації по днях. */
    val TextIcon = Color(0xFF3E5776)

    /** oklch(0.55 0.04 255) — підписи секцій, дні тижня. */
    val TextLabel = Color(0xFF627389)

    /** oklch(0.55 0.06 255) — іконки в рядках інформації. */
    val TextLabelIcon = Color(0xFF5A7394)

    /** oklch(0.60 0.04 255) — другорядний текст, час у картці. */
    val TextMuted = Color(0xFF708298)

    /** oklch(0.65 0.03 255) — текст порожнього стану. */
    val TextDisabled = Color(0xFF8390A2)

    /** oklch(0.65 0.04 255) — неактивна вкладка нижньої навігації. */
    val TextNavIdle = Color(0xFF7F91A8)

    /** oklch(0.70 0.04 255) — «шеврон» праворуч у картці. */
    val Chevron = Color(0xFF8EA0B8)

    /** oklch(0.82 0.02 255) — вимкнений перемикач. */
    val ToggleOff = Color(0xFFBCC5D1)

    val OnPrimary = Color(0xFFFFFFFF)

    // ─── Кнопки в шапці ────────────────────────────────────────────────────────
    /** oklch(0.35 0.08 255 / 0.4) — напівпрозоре тло кнопок «‹» і «›». */
    val HeaderButtonBg = Color(0x66284066)

    // ─── Тіні ──────────────────────────────────────────────────────────────────
    /** oklch(0.5 0.04 255 / 0.08) — тінь картки. */
    val CardShadow = Color(0xFF5C6E85)

    // ─── Плитка посилання (LinkRow, тип "site") ────────────────────────────────
    /** oklch(0.55 0.14 255) */
    val LinkTint = Color(0xFF3072C1)

    /** oklch(0.95 0.04 255) */
    val LinkTintBg = Color(0xFFDDF0FF)

    /** oklch(0.48 0.10 60) — плитка коду підключення (тип "doc" у макеті). */
    val CodeTint = Color(0xFF864E18)

    /** oklch(0.95 0.05 75) */
    val CodeTintBg = Color(0xFFFFEACA)
}
