package com.savares.dailyinstallmentsaver.ui.theme

import android.app.Activity
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat

private val DarkColorScheme = darkColorScheme(
    primary = TelegramDarkPrimary,
    onPrimary = Color.White,
    primaryContainer = TelegramDarkAccent,
    onPrimaryContainer = Color.White,
    secondary = TelegramDarkSecondaryText,
    onSecondary = Color.White,
    background = TelegramDarkBackground,
    onBackground = TelegramDarkText,
    surface = TelegramDarkSurface,
    onSurface = TelegramDarkText,
    surfaceVariant = TelegramDarkCard,
    onSurfaceVariant = TelegramDarkSecondaryText,
    outline = TelegramDarkSecondaryText,
    surfaceTint = Color.Transparent
)

@Composable
fun DailyInstallmentSaverTheme(
    darkTheme: Boolean = true,
    content: @Composable () -> Unit
) {
    val colorScheme = DarkColorScheme
    val view = LocalView.current
    
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as Activity).window
            window.statusBarColor = Color.Transparent.toArgb()
            window.navigationBarColor = Color.Transparent.toArgb()
            WindowCompat.getInsetsController(window, view).isAppearanceLightStatusBars = false
            WindowCompat.getInsetsController(window, view).isAppearanceLightNavigationBars = false
            WindowCompat.setDecorFitsSystemWindows(window, false)
        }
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}
