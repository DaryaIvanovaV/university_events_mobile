package com.KSU.EventsParser.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable

/**
 * Палітра макета, загорнута в Material 3 ColorScheme, щоб стандартні
 * компоненти успадковували її самі.
 *
 * Динамічний колір (Material You) свідомо вимкнено: макет має фірмову
 * темно-синю гаму, і підміна її системними барвами зруйнувала б дизайн.
 * Темної теми в макеті немає, тому схема одна — світла.
 */
private val AppColorScheme = lightColorScheme(
    primary = AppColors.Primary,
    onPrimary = AppColors.OnPrimary,
    primaryContainer = AppColors.PrimaryMid,
    onPrimaryContainer = AppColors.OnPrimary,
    secondary = AppColors.Accent,
    onSecondary = AppColors.OnPrimary,
    secondaryContainer = AppColors.AccentLight,
    onSecondaryContainer = AppColors.TextTitle,
    background = AppColors.Background,
    onBackground = AppColors.TextPrimary,
    surface = AppColors.Surface,
    onSurface = AppColors.TextPrimary,
    surfaceVariant = AppColors.ChipIdleBg,
    onSurfaceVariant = AppColors.TextChipIdle,
    outline = AppColors.GridBorder,
    outlineVariant = AppColors.Divider,
)

@Composable
fun EventsParserTheme(
    content: @Composable () -> Unit,
) {
    MaterialTheme(
        colorScheme = AppColorScheme,
        typography = AppTypography,
        content = content,
    )
}
