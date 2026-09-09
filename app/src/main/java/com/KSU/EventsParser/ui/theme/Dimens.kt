package com.KSU.EventsParser.ui.theme

import androidx.compose.ui.unit.dp

/**
 * Розміри з макета. CSS px переносимо в dp один до одного.
 * У композаблах не має бути «магічних» чисел — усе береться звідси.
 */
object Dimens {

    // ─── Загальні відступи ─────────────────────────────────────────────────────
    /** padding:"...16px" — бічні поля екранів і карток. */
    val ScreenPadding = 16.dp
    val ScreenPaddingSmall = 12.dp

    // ─── Шапка ─────────────────────────────────────────────────────────────────
    /** padding:"12px 16px 10px" */
    val HeaderPaddingTop = 12.dp
    val HeaderPaddingBottom = 10.dp
    /** Шапка екрана деталей: padding:"12px 16px 14px" */
    val HeaderDetailPaddingBottom = 14.dp
    /** NavBtn: width:32, height:32, borderRadius:8 */
    val HeaderButtonSize = 32.dp
    val HeaderButtonRadius = 8.dp
    val HeaderButtonGap = 4.dp
    val HeaderOverlineGap = 2.dp

    // ─── Сітка місяця ──────────────────────────────────────────────────────────
    /** Кружечок числа дня: width:30, height:30 */
    val DayCircle = 30.dp
    /** padding:"3px 0" у комірці дня */
    val DayCellPadding = 3.dp
    /** Крапка події: width:4, height:4 */
    val EventDot = 4.dp
    val EventDotGap = 2.dp
    /** height:5 — смуга під числом, зарезервована під крапки */
    val EventDotRowHeight = 5.dp
    val EventDotRowTopGap = 1.dp
    /** Максимум крапок під числом: evs.slice(0,3) */
    const val MaxDotsPerDay = 3
    /** padding:"8px 12px 4px" у рядку днів тижня */
    val WeekdayRowPaddingH = 12.dp
    val WeekdayRowPaddingTop = 8.dp
    val WeekdayRowPaddingBottom = 4.dp
    /** gap:"2px 0" між рядками сітки */
    val MonthGridRowGap = 2.dp
    val MonthGridPaddingH = 12.dp
    val MonthGridPaddingV = 4.dp

    // ─── Тижневий вигляд ───────────────────────────────────────────────────────
    /** width:24, height:24 — кружечок числа в смузі днів */
    val WeekDayCircle = 24.dp
    /** padding:"8px 2px 6px" */
    val WeekHeaderPaddingTop = 8.dp
    val WeekHeaderPaddingH = 2.dp
    val WeekHeaderPaddingBottom = 6.dp
    /** padding:"6px 3px", gap:3 у колонці дня */
    val WeekColumnPaddingV = 6.dp
    val WeekColumnPaddingH = 3.dp
    val WeekColumnGap = 3.dp
    /** borderRadius:5, padding:"4px 5px" — плитка події в колонці */
    val WeekEventRadius = 5.dp
    val WeekEventPaddingV = 4.dp
    val WeekEventPaddingH = 5.dp
    /** minHeight:300 */
    val WeekMinHeight = 300.dp

    // ─── Картка події ──────────────────────────────────────────────────────────
    /** borderRadius:12 */
    val CardRadius = 12.dp
    /** padding:"10px 14px" */
    val CardPaddingV = 10.dp
    val CardPaddingH = 14.dp
    /** marginBottom:6 */
    val CardGap = 6.dp
    /** borderLeft:"3px solid" */
    val CardStripe = 3.dp
    /** gap:10 між текстом і шевроном */
    val CardContentGap = 10.dp
    /** boxShadow:"0 1px 4px ..." */
    val CardElevation = 1.dp
    val CardTitleGap = 3.dp
    val CardMetaGap = 6.dp
    val CardDateGap = 2.dp
    val CardPlaceGap = 4.dp

    // ─── Картки на екрані деталей ──────────────────────────────────────────────
    /** borderRadius:14, padding:"14px 16px", marginBottom:12 */
    val PanelRadius = 14.dp
    val PanelPaddingV = 14.dp
    val PanelPaddingH = 16.dp
    val PanelGap = 12.dp
    /** boxShadow:"0 1px 6px ..." */
    val PanelElevation = 1.dp
    /** height:1, margin:"10px 0" — роздільник між рядками інформації */
    val PanelDividerGap = 10.dp
    /** gap:10 між іконкою та текстом у рядку інформації */
    val InfoRowGap = 10.dp
    val InfoRowLabelGap = 1.dp
    val InfoRowIconTopOffset = 1.dp
    val SectionLabelGap = 8.dp
    val SectionLabelGapWide = 10.dp

    // ─── Чипи ──────────────────────────────────────────────────────────────────
    /** borderRadius:20 */
    val ChipRadius = 20.dp
    /** padding:"1px 7px" — чип типу в картці */
    val ChipPaddingV = 1.dp
    val ChipPaddingH = 7.dp
    /** padding:"3px 10px" — чип типу в шапці деталей */
    val ChipLargePaddingV = 3.dp
    val ChipLargePaddingH = 10.dp
    /** padding:"4px 10px" — чип аудиторії */
    val ChipTagPaddingV = 4.dp
    val ChipTagPaddingH = 10.dp
    val ChipGap = 6.dp
    /** padding:"5px 12px" — чип фільтра */
    val FilterChipPaddingV = 5.dp
    val FilterChipPaddingH = 12.dp

    // ─── Рядок посилання ───────────────────────────────────────────────────────
    /** width:34, height:34, borderRadius:10 */
    val LinkIconBox = 34.dp
    val LinkIconBoxRadius = 10.dp
    /** padding:"8px 0" */
    val LinkRowPaddingV = 8.dp
    val LinkRowGap = 10.dp

    // ─── Перемикач ─────────────────────────────────────────────────────────────
    /** width:44, height:26, borderRadius:13 */
    val ToggleWidth = 44.dp
    val ToggleHeight = 26.dp
    /** width:20, height:20, top:3, left:3 / 21 */
    val ToggleThumb = 20.dp
    val ToggleThumbInset = 3.dp

    // ─── Нижня навігація ───────────────────────────────────────────────────────
    /** padding:"8px 4px 6px", gap:3 */
    val NavPaddingTop = 8.dp
    val NavPaddingH = 4.dp
    val NavPaddingBottom = 6.dp
    val NavItemGap = 3.dp
    /** Icon size:21 */
    val NavIcon = 21.dp
    /** Крапка-індикатор: width:7, height:7, border:1.5 */
    val NavBadge = 7.dp
    val NavBadgeBorder = 1.5.dp

    // ─── Іконки ────────────────────────────────────────────────────────────────
    val IconTiny = 11.dp
    val IconSmall = 15.dp
    val IconLinkExternal = 15.dp
    val IconLinkBox = 16.dp
    val IconMedium = 16.dp
    val IconChevronCard = 16.dp
    val IconHeader = 18.dp
    val IconInfoRow = 15.dp
    val IconDayNav = 20.dp
    val IconBellDetail = 18.dp

    // ─── Роздільники ───────────────────────────────────────────────────────────
    val Hairline = 1.dp

    // ─── Порожні стани ─────────────────────────────────────────────────────────
    /** padding:"24px 0" — «Немає подій цього дня» під сіткою */
    val EmptyDayPaddingV = 24.dp
    /** padding:"40px 0" — порожній день у DayView */
    val EmptyStatePaddingV = 40.dp
    val EmptyStateIconGap = 8.dp

    // ─── Списки ────────────────────────────────────────────────────────────────
    /** padding:"10px 16px" у смузі фільтрів */
    val FilterBarPaddingV = 10.dp
    /** padding:"12px 16px 6px" — заголовок місяця в списку */
    val ListSectionPaddingTop = 12.dp
    val ListSectionPaddingBottom = 6.dp
    /** padding:"8px 0 24px" */
    val ListBottomPadding = 24.dp
    /** padding:"10px 16px" — список подій обраного дня під сіткою */
    val SelectedDayListPaddingV = 10.dp
}
