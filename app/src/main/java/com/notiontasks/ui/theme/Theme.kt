package com.notiontasks.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp

// ─── Monochrome palette matching SyncTasks aesthetic ─────────────────────────

val Black = Color(0xFF0A0A0A)
val DarkGray = Color(0xFF1C1C1E)
val MidGray = Color(0xFF3A3A3C)
val LightGray = Color(0xFFAEAEB2)
val OffWhite = Color(0xFFF2F2F7)
val White = Color(0xFFFFFFFF)
val AccentBlue = Color(0xFF007AFF)
val DestructiveRed = Color(0xFFFF3B30)

private val LightColors = lightColorScheme(
    primary = Black,
    onPrimary = White,
    primaryContainer = OffWhite,
    onPrimaryContainer = Black,
    secondary = MidGray,
    onSecondary = White,
    secondaryContainer = OffWhite,
    background = White,
    onBackground = Black,
    surface = White,
    onSurface = Black,
    surfaceVariant = OffWhite,
    onSurfaceVariant = MidGray,
    outline = Color(0xFFD1D1D6),
    error = DestructiveRed
)

private val DarkColors = darkColorScheme(
    primary = White,
    onPrimary = Black,
    primaryContainer = DarkGray,
    onPrimaryContainer = White,
    secondary = LightGray,
    onSecondary = Black,
    secondaryContainer = DarkGray,
    background = Black,
    onBackground = White,
    surface = DarkGray,
    onSurface = White,
    surfaceVariant = MidGray,
    onSurfaceVariant = LightGray,
    outline = MidGray,
    error = DestructiveRed
)

val AppTypography = Typography(
    displayLarge = TextStyle(fontWeight = FontWeight.Bold, fontSize = 32.sp, letterSpacing = (-0.5).sp),
    titleLarge = TextStyle(fontWeight = FontWeight.SemiBold, fontSize = 20.sp, letterSpacing = 0.sp),
    titleMedium = TextStyle(fontWeight = FontWeight.Medium, fontSize = 16.sp, letterSpacing = 0.sp),
    titleSmall = TextStyle(fontWeight = FontWeight.Medium, fontSize = 14.sp, letterSpacing = 0.sp),
    bodyLarge = TextStyle(fontWeight = FontWeight.Normal, fontSize = 16.sp, letterSpacing = 0.sp),
    bodyMedium = TextStyle(fontWeight = FontWeight.Normal, fontSize = 14.sp, letterSpacing = 0.sp),
    bodySmall = TextStyle(fontWeight = FontWeight.Normal, fontSize = 12.sp, letterSpacing = 0.sp),
    labelSmall = TextStyle(fontWeight = FontWeight.Medium, fontSize = 11.sp, letterSpacing = 0.5.sp),
)

@Composable
fun NotionTasksTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    MaterialTheme(
        colorScheme = if (darkTheme) DarkColors else LightColors,
        typography = AppTypography,
        content = content
    )
}
