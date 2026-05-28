package com.example.faiz_gear.ui.theme

import android.app.Activity
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat

private val FaizColorScheme = darkColorScheme(
    primary = NeonRed,
    secondary = MetalGray,
    tertiary = AmberYellow,
    background = DeepBlack,
    surface = MatteBlack,
    onPrimary = Color.White,
    onSecondary = Color.White,
    onTertiary = DeepBlack,
    onBackground = TextMain,
    onSurface = TextMain,
)

@Composable
fun Faiz_GearTheme(
    darkTheme: Boolean = true,
    dynamicColor: Boolean = false,
    content: @Composable () -> Unit
) {
    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as Activity).window
            window.statusBarColor = DeepBlack.toArgb()
            window.navigationBarColor = DeepBlack.toArgb()
            WindowCompat.getInsetsController(window, view).isAppearanceLightStatusBars = false
        }
    }

    MaterialTheme(
        colorScheme = FaizColorScheme,
        typography = Typography,
        content = content
    )
}
