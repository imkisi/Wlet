package com.example.wlet.ui.theme

import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color

/**
 * Common vertical gradient used across the application for a cohesive look.
 * Transitions from solid white to the light greyish background.
 */
@Composable
fun getVerticalThemedGradient(): Brush {
    return Brush.verticalGradient(
        0.0f to Color.White,
        0.5f to Color.White,
        1.0f to Color(0xFFF0ECE9)
    )
}
