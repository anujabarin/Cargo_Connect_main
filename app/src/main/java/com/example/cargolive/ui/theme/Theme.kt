package com.example.cargolive.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.material3.Typography
import androidx.compose.runtime.Composable

// Define your color palette
private val DarkColorScheme = darkColorScheme(
    primary = CargoBlue,
    secondary = CargoAccent
)

private val LightColorScheme = lightColorScheme(
    primary = CargoBlue,
    secondary = CargoAccent
)

@Composable
fun CargoLiveTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    val colorScheme = if (darkTheme) DarkColorScheme else LightColorScheme

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography(), // You can define a custom one if needed
        content = content
    )
}
