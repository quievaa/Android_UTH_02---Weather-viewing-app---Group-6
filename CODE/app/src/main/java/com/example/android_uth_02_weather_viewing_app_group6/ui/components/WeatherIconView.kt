package com.example.android_uth_02_weather_viewing_app_group6.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Cloud
import androidx.compose.material.icons.outlined.Nightlight
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Fill
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.example.android_uth_02_weather_viewing_app_group6.ui.model.WeatherCondition
import com.example.android_uth_02_weather_viewing_app_group6.ui.theme.WeatherSunAmber
import com.example.android_uth_02_weather_viewing_app_group6.ui.theme.WeatherSunYellow

@Composable
fun WeatherIcon(
    condition: WeatherCondition,
    modifier: Modifier = Modifier,
    size: Dp = 28.dp,
    tint: Color = Color.White
) {
    Box(
        modifier = modifier.size(size),
        contentAlignment = Alignment.Center
    ) {
        when (condition) {
            WeatherCondition.SUNNY -> {
                Canvas(modifier = Modifier.size(size)) {
                    val center = Offset(this.size.width / 2, this.size.height / 2)
                    val radius = this.size.width * 0.28f

                    // Draw glowing sun circle
                    drawCircle(
                        brush = Brush.radialGradient(
                            colors = listOf(Color(0xFFFFFBEB), WeatherSunYellow, WeatherSunAmber),
                            center = center,
                            radius = radius
                        ),
                        radius = radius,
                        center = center
                    )

                    // Draw 8 rays
                    val rayInner = radius * 1.35f
                    val rayOuter = radius * 1.75f
                    for (i in 0 until 8) {
                        val angle = (i * 45) * (Math.PI / 180).toFloat()
                        val start = Offset(
                            center.x + kotlin.math.cos(angle) * rayInner,
                            center.y + kotlin.math.sin(angle) * rayInner
                        )
                        val end = Offset(
                            center.x + kotlin.math.cos(angle) * rayOuter,
                            center.y + kotlin.math.sin(angle) * rayOuter
                        )
                        drawLine(
                            color = WeatherSunYellow,
                            start = start,
                            end = end,
                            strokeWidth = this.size.width * 0.08f,
                            cap = StrokeCap.Round
                        )
                    }
                }
            }

            WeatherCondition.PARTLY_CLOUDY -> {
                Canvas(modifier = Modifier.size(size)) {
                    val w = this.size.width
                    val h = this.size.height

                    // Sun in background upper-right
                    val sunCenter = Offset(w * 0.68f, h * 0.35f)
                    drawCircle(
                        brush = Brush.radialGradient(
                            colors = listOf(Color(0xFFFFFBEB), WeatherSunYellow),
                            center = sunCenter,
                            radius = w * 0.24f
                        ),
                        radius = w * 0.22f,
                        center = sunCenter
                    )

                    // Cloud in foreground
                    val cloudPath = Path().apply {
                        moveTo(w * 0.20f, h * 0.72f)
                        cubicTo(w * 0.12f, h * 0.72f, w * 0.08f, h * 0.60f, w * 0.14f, h * 0.52f)
                        cubicTo(w * 0.12f, h * 0.40f, w * 0.28f, h * 0.32f, w * 0.38f, h * 0.38f)
                        cubicTo(w * 0.46f, h * 0.26f, w * 0.68f, h * 0.26f, w * 0.72f, h * 0.40f)
                        cubicTo(w * 0.84f, h * 0.42f, w * 0.88f, h * 0.56f, w * 0.82f, h * 0.66f)
                        cubicTo(w * 0.84f, h * 0.72f, w * 0.76f, h * 0.72f, w * 0.70f, h * 0.72f)
                        close()
                    }

                    drawPath(
                        path = cloudPath,
                        brush = Brush.verticalGradient(
                            colors = listOf(Color(0xFFFFFFFF), Color(0xFFE2E8F0)),
                            startY = h * 0.26f,
                            endY = h * 0.72f
                        ),
                        style = Fill
                    )
                }
            }

            WeatherCondition.CLOUDY, WeatherCondition.OVERCAST -> {
                Icon(
                    imageVector = Icons.Outlined.Cloud,
                    contentDescription = "Cloudy",
                    tint = tint,
                    modifier = Modifier.size(size)
                )
            }

            WeatherCondition.LIGHT_RAIN, WeatherCondition.RAIN -> {
                Canvas(modifier = Modifier.size(size)) {
                    val w = this.size.width
                    val h = this.size.height

                    // Cloud
                    val cloudPath = Path().apply {
                        moveTo(w * 0.25f, h * 0.55f)
                        cubicTo(w * 0.12f, h * 0.55f, w * 0.10f, h * 0.38f, w * 0.22f, h * 0.30f)
                        cubicTo(w * 0.24f, h * 0.15f, w * 0.50f, h * 0.12f, w * 0.60f, h * 0.24f)
                        cubicTo(w * 0.75f, h * 0.22f, w * 0.88f, h * 0.35f, w * 0.80f, h * 0.52f)
                        cubicTo(w * 0.85f, h * 0.55f, w * 0.78f, h * 0.55f, w * 0.72f, h * 0.55f)
                        close()
                    }

                    drawPath(
                        path = cloudPath,
                        brush = Brush.verticalGradient(
                            colors = listOf(Color(0xFFE2E8F0), Color(0xFF94A3B8)),
                            startY = h * 0.12f,
                            endY = h * 0.55f
                        )
                    )

                    // Rain drops
                    val rainColor = Color(0xFF38BDF8)
                    drawLine(
                        color = rainColor,
                        start = Offset(w * 0.32f, h * 0.65f),
                        end = Offset(w * 0.24f, h * 0.85f),
                        strokeWidth = w * 0.08f,
                        cap = StrokeCap.Round
                    )
                    drawLine(
                        color = rainColor,
                        start = Offset(w * 0.52f, h * 0.65f),
                        end = Offset(w * 0.44f, h * 0.85f),
                        strokeWidth = w * 0.08f,
                        cap = StrokeCap.Round
                    )
                    drawLine(
                        color = rainColor,
                        start = Offset(w * 0.72f, h * 0.65f),
                        end = Offset(w * 0.64f, h * 0.85f),
                        strokeWidth = w * 0.08f,
                        cap = StrokeCap.Round
                    )
                }
            }

            WeatherCondition.HEAVY_RAIN, WeatherCondition.THUNDERSTORM -> {
                Canvas(modifier = Modifier.size(size)) {
                    val w = this.size.width
                    val h = this.size.height

                    // Dark storm cloud
                    val cloudPath = Path().apply {
                        moveTo(w * 0.22f, h * 0.50f)
                        cubicTo(w * 0.10f, h * 0.50f, w * 0.08f, h * 0.35f, w * 0.20f, h * 0.26f)
                        cubicTo(w * 0.22f, h * 0.10f, w * 0.52f, h * 0.08f, w * 0.62f, h * 0.20f)
                        cubicTo(w * 0.78f, h * 0.18f, w * 0.90f, h * 0.32f, w * 0.82f, h * 0.48f)
                        cubicTo(w * 0.86f, h * 0.50f, w * 0.80f, h * 0.50f, w * 0.74f, h * 0.50f)
                        close()
                    }

                    drawPath(
                        path = cloudPath,
                        brush = Brush.verticalGradient(
                            colors = listOf(Color(0xFF64748B), Color(0xFF334155)),
                            startY = h * 0.08f,
                            endY = h * 0.50f
                        )
                    )

                    // Lightning bolt
                    val boltPath = Path().apply {
                        moveTo(w * 0.48f, h * 0.48f)
                        lineTo(w * 0.38f, h * 0.68f)
                        lineTo(w * 0.50f, h * 0.68f)
                        lineTo(w * 0.42f, h * 0.92f)
                        lineTo(w * 0.60f, h * 0.62f)
                        lineTo(w * 0.48f, h * 0.62f)
                        close()
                    }

                    drawPath(
                        path = boltPath,
                        color = Color(0xFFFACC15),
                        style = Fill
                    )

                    // Rain drops
                    val rainColor = Color(0xFF38BDF8)
                    drawLine(
                        color = rainColor,
                        start = Offset(w * 0.24f, h * 0.60f),
                        end = Offset(w * 0.18f, h * 0.82f),
                        strokeWidth = w * 0.07f,
                        cap = StrokeCap.Round
                    )
                    drawLine(
                        color = rainColor,
                        start = Offset(w * 0.76f, h * 0.60f),
                        end = Offset(w * 0.70f, h * 0.82f),
                        strokeWidth = w * 0.07f,
                        cap = StrokeCap.Round
                    )
                }
            }

            WeatherCondition.NIGHT_CLEAR -> {
                Icon(
                    imageVector = Icons.Outlined.Nightlight,
                    contentDescription = "Clear Night",
                    tint = Color(0xFFFDE047),
                    modifier = Modifier.size(size)
                )
            }

            WeatherCondition.NIGHT_CLOUDY -> {
                Canvas(modifier = Modifier.size(size)) {
                    val w = this.size.width
                    val h = this.size.height

                    // Moon
                    val moonCenter = Offset(w * 0.68f, h * 0.35f)
                    drawCircle(
                        color = Color(0xFFFDE047),
                        radius = w * 0.22f,
                        center = moonCenter
                    )

                    // Foreground cloud
                    val cloudPath = Path().apply {
                        moveTo(w * 0.20f, h * 0.72f)
                        cubicTo(w * 0.12f, h * 0.72f, w * 0.08f, h * 0.60f, w * 0.14f, h * 0.52f)
                        cubicTo(w * 0.12f, h * 0.40f, w * 0.28f, h * 0.32f, w * 0.38f, h * 0.38f)
                        cubicTo(w * 0.46f, h * 0.26f, w * 0.68f, h * 0.26f, w * 0.72f, h * 0.40f)
                        cubicTo(w * 0.84f, h * 0.42f, w * 0.88f, h * 0.56f, w * 0.82f, h * 0.66f)
                        cubicTo(w * 0.84f, h * 0.72f, w * 0.76f, h * 0.72f, w * 0.70f, h * 0.72f)
                        close()
                    }

                    drawPath(
                        path = cloudPath,
                        brush = Brush.verticalGradient(
                            colors = listOf(Color(0xFFCBD5E1), Color(0xFF64748B)),
                            startY = h * 0.26f,
                            endY = h * 0.72f
                        )
                    )
                }
            }
        }
    }
}
