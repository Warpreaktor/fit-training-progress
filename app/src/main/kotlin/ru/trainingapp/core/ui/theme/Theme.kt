package ru.trainingapp.core.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable

private val LightColorScheme = lightColorScheme(
    primary = TrainingPrimary,
    secondary = TrainingSecondary,
    background = TrainingBackground,
)

private val DarkColorScheme = darkColorScheme(
    primary = TrainingPrimary,
    secondary = TrainingSecondary,
)

@Composable
fun TrainingAppTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit,
) {
    MaterialTheme(
        colorScheme = if (darkTheme) DarkColorScheme else LightColorScheme,
        typography = Typography,
        content = content,
    )
}
