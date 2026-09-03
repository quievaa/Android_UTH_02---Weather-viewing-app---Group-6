package com.example.android_uth_02_weather_viewing_app_group6.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.height
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

@Composable
fun TemperatureRangeBar(
    min: Int,
    max: Int,
    globalMin: Int = 10,
    globalMax: Int = 40,
    modifier: Modifier = Modifier,
    height: Dp = 4.dp
) {
    Canvas(
        modifier = modifier
            .height(height)
    ) {
        val totalRange = (globalMax - globalMin).coerceAtLeast(1).toFloat()
        val startFraction = ((min - globalMin) / totalRange).coerceIn(0f, 1f)
        val endFraction = ((max - globalMin) / totalRange).coerceIn(0f, 1f)

        val w = size.width
        val h = size.height
        val corner = CornerRadius(h / 2, h / 2)

        // Background Track
        drawRoundRect(
            color = Color(0x33000000),
            size = Size(w, h),
            cornerRadius = corner
        )

        // Colored Range Bar
        val startX = w * startFraction
        val barWidth = (w * (endFraction - startFraction)).coerceAtLeast(h)

        val gradientBrush = Brush.horizontalGradient(
            colors = listOf(
                Color(0xFF38BDF8),
                Color(0xFF818CF8),
                Color(0xFFF472B6),
                Color(0xFFFB923C)
            ),
            startX = startX,
            endX = startX + barWidth
        )

        drawRoundRect(
            brush = gradientBrush,
            topLeft = Offset(startX, 0f),
            size = Size(barWidth, h),
            cornerRadius = corner
        )
    }
}
