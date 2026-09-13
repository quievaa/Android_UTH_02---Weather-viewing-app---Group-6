package com.example.android_uth_02_weather_viewing_app_group6.ui.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import com.example.android_uth_02_weather_viewing_app_group6.R
import com.example.android_uth_02_weather_viewing_app_group6.ui.model.WeatherCondition
import kotlinx.coroutines.delay
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.sin
import kotlin.random.Random

// 3D Point container for perspective projection
private data class Particle3D(
    var x: Float,
    var y: Float,
    var z: Float,
    var size: Float,
    var speed: Float,
    var alpha: Float = 1f,
    var seed: Float = Random.nextFloat() * 100f
)

private data class SplashRing(
    var x: Float,
    var y: Float,
    var radius: Float = 2f,
    var maxRadius: Float = 18f,
    var alpha: Float = 0.8f,
    var active: Boolean = true
)

private data class LightningBolt(
    val points: List<Offset>,
    val branches: List<List<Offset>>,
    var alpha: Float = 1f
)

/**
 * Pure Native Kotlin Jetpack Compose 3D Weather Simulation Background.
 * Dynamically reacts in real-time to current weather conditions (Sunny, Cloudy, Rain, Thunderstorm, Night).
 */
@Composable
fun Weather3DBackground(
    condition: WeatherCondition,
    isNight: Boolean = false,
    windSpeedMps: Double = 5.0,
    temperatureC: Double = 28.0,
    modifier: Modifier = Modifier
) {
    // Parallax interactive camera offsets (from user touch drag)
    var cameraOffsetX by remember { mutableFloatStateOf(0f) }
    var cameraOffsetY by remember { mutableFloatStateOf(0f) }

    // Infinite animation clock
    val infiniteTransition = rememberInfiniteTransition(label = "weather_3d_clock")
    val timeLoop by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 1000f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 100000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "time_loop"
    )

    val sunPulse by infiniteTransition.animateFloat(
        initialValue = 0.92f,
        targetValue = 1.08f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 2800, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "sun_pulse"
    )

    val sunRayRotation by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 35000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "sun_ray_rot"
    )

    // Dynamic Atmosphere Sky Gradients
    val (targetSkyTop, targetSkyMid, targetSkyBot) = remember(condition, isNight) {
        when {
            isNight -> when (condition) {
                WeatherCondition.NIGHT_CLEAR -> Triple(Color(0xFF030712), Color(0xFF0F172A), Color(0xFF1E293B))
                WeatherCondition.NIGHT_CLOUDY -> Triple(Color(0xFF060B18), Color(0xFF111827), Color(0xFF1F2937))
                WeatherCondition.THUNDERSTORM -> Triple(Color(0xFF05050C), Color(0xFF0F1222), Color(0xFF1E1B4B))
                else -> Triple(Color(0xFF070B19), Color(0xFF0F172A), Color(0xFF1E293B))
            }
            condition == WeatherCondition.SUNNY -> Triple(Color(0xFF0284C7), Color(0xFF38BDF8), Color(0xFFBAE6FD))
            condition == WeatherCondition.PARTLY_CLOUDY -> Triple(Color(0xFF0369A1), Color(0xFF60A5FA), Color(0xFF93C5FD))
            condition == WeatherCondition.CLOUDY || condition == WeatherCondition.OVERCAST -> Triple(Color(0xFF334155), Color(0xFF64748B), Color(0xFF94A3B8))
            condition == WeatherCondition.LIGHT_RAIN || condition == WeatherCondition.RAIN -> Triple(Color(0xFF1E293B), Color(0xFF334155), Color(0xFF475569))
            condition == WeatherCondition.HEAVY_RAIN || condition == WeatherCondition.THUNDERSTORM -> Triple(Color(0xFF090D16), Color(0xFF1E1B4B), Color(0xFF312E81))
            else -> Triple(Color(0xFF0284C7), Color(0xFF38BDF8), Color(0xFFBAE6FD))
        }
    }

    val animatedTop by animateColorAsState(targetValue = targetSkyTop, animationSpec = tween(750), label = "sky_top")
    val animatedMid by animateColorAsState(targetValue = targetSkyMid, animationSpec = tween(750), label = "sky_mid")
    val animatedBot by animateColorAsState(targetValue = targetSkyBot, animationSpec = tween(750), label = "sky_bot")

    // Particles persistent caches
    val stars = remember {
        List(90) {
            Particle3D(
                x = Random.nextFloat() * 1000f,
                y = Random.nextFloat() * 600f,
                z = 10f + Random.nextFloat() * 200f,
                size = 1f + Random.nextFloat() * 2.5f,
                speed = 0.5f + Random.nextFloat() * 1.5f,
                alpha = 0.3f + Random.nextFloat() * 0.7f
            )
        }
    }

    val sunMotes = remember {
        List(40) {
            Particle3D(
                x = Random.nextFloat() * 1000f,
                y = Random.nextFloat() * 800f,
                z = 20f + Random.nextFloat() * 150f,
                size = 2f + Random.nextFloat() * 4.5f,
                speed = 0.8f + Random.nextFloat() * 1.6f,
                alpha = 0.2f + Random.nextFloat() * 0.6f
            )
        }
    }

    val rainDrops = remember {
        List(140) {
            Particle3D(
                x = Random.nextFloat() * 1200f - 100f,
                y = Random.nextFloat() * 1200f,
                z = 5f + Random.nextFloat() * 80f,
                size = 1.2f + Random.nextFloat() * 2f,
                speed = 12f + Random.nextFloat() * 10f
            )
        }
    }

    val splashes = remember { mutableStateListOf<SplashRing>() }

    // Procedural Lightning state
    var currentLightning by remember { mutableStateOf<LightningBolt?>(null) }
    var lightningFlashAlpha by remember { mutableFloatStateOf(0f) }

    if (condition == WeatherCondition.THUNDERSTORM) {
        LaunchedEffect(Unit) {
            while (true) {
                delay(Random.nextLong(2500L, 6000L))
                // Generate branching lightning
                val startX = 150f + Random.nextFloat() * 700f
                val mainPoints = mutableListOf<Offset>()
                var curX = startX
                var curY = 50f
                mainPoints.add(Offset(curX, curY))

                val branches = mutableListOf<List<Offset>>()
                for (step in 0..12) {
                    curY += 40f + Random.nextFloat() * 35f
                    curX += (Random.nextFloat() - 0.5f) * 70f
                    val p = Offset(curX, curY)
                    mainPoints.add(p)

                    if (Random.nextFloat() < 0.4f) {
                        var bX = curX
                        var bY = curY
                        val branchPoints = mutableListOf(Offset(bX, bY))
                        for (bStep in 0..4) {
                            bY += 25f + Random.nextFloat() * 20f
                            bX += if (Random.nextBoolean()) (15f + Random.nextFloat() * 25f) else (-40f + Random.nextFloat() * 25f)
                            branchPoints.add(Offset(bX, bY))
                        }
                        branches.add(branchPoints)
                    }
                }

                currentLightning = LightningBolt(mainPoints, branches, 1f)
                lightningFlashAlpha = 0.85f

                delay(120)
                lightningFlashAlpha = 0.2f
                delay(80)
                lightningFlashAlpha = 0.6f
                delay(150)
                lightningFlashAlpha = 0f
                currentLightning = null
            }
        }
    }

    val isRainOrTransitional = condition in listOf(
        WeatherCondition.LIGHT_RAIN,
        WeatherCondition.RAIN,
        WeatherCondition.HEAVY_RAIN,
        WeatherCondition.THUNDERSTORM,
        WeatherCondition.CLOUDY,
        WeatherCondition.OVERCAST
    )
    val isSunnyDay = !isNight && (condition == WeatherCondition.SUNNY || condition == WeatherCondition.PARTLY_CLOUDY)

    Box(
        modifier = modifier
            .fillMaxSize()
            .pointerInput(Unit) {
                detectDragGestures(
                    onDrag = { change, dragAmount ->
                        change.consume()
                        cameraOffsetX = (cameraOffsetX + dragAmount.x * 0.15f).coerceIn(-60f, 60f)
                        cameraOffsetY = (cameraOffsetY + dragAmount.y * 0.15f).coerceIn(-40f, 40f)
                    },
                    onDragEnd = {
                        cameraOffsetX = 0f
                        cameraOffsetY = 0f
                    }
                )
            }
    ) {
        // 1. High-Resolution Weather Background Image
        if (isRainOrTransitional) {
            Image(
                painter = painterResource(id = R.drawable.troi_mua),
                contentDescription = "Trời mưa / Chuyển mưa",
                contentScale = ContentScale.Crop,
                modifier = Modifier.fillMaxSize()
            )
        } else if (isSunnyDay) {
            Image(
                painter = painterResource(id = R.drawable.troi_nang),
                contentDescription = "Trời nắng",
                contentScale = ContentScale.Crop,
                modifier = Modifier.fillMaxSize()
            )
        }

        // 2. 3D Particle & Atmosphere Simulation Overlay (Three.js-style Canvas module)
        Canvas(modifier = Modifier.fillMaxSize()) {
            val w = size.width
            val h = size.height
            val t = timeLoop * 0.05f

            // Atmospheric Vignette & Contrast Overlay for foreground readability
            if (isRainOrTransitional || isSunnyDay) {
                drawRect(
                    brush = Brush.verticalGradient(
                        colors = listOf(
                            Color(0x550B132B),
                            Color(0x221C2541),
                            Color(0x770B132B)
                        ),
                        startY = 0f,
                        endY = h
                    )
                )
            } else {
                drawRect(
                    brush = Brush.verticalGradient(
                        colors = listOf(animatedTop, animatedMid, animatedBot),
                        startY = 0f,
                        endY = h
                    )
                )
            }

            // 3D Stars & Moon (Night Mode)
            if (isNight || condition == WeatherCondition.NIGHT_CLEAR || condition == WeatherCondition.NIGHT_CLOUDY) {
                draw3DStarfield(stars, w, h, t, cameraOffsetX, cameraOffsetY)
                draw3DMoon(w, h, sunPulse, cameraOffsetX, cameraOffsetY)
            }

            // 3D Sun & Rays & Dust Motes (Sunny Day)
            if (isSunnyDay) {
                draw3DSun(w, h, sunPulse, sunRayRotation, cameraOffsetX, cameraOffsetY)
                draw3DSunDustMotes(sunMotes, w, h, t, cameraOffsetX, cameraOffsetY)
            }

            // 3D Rain Streaks & Splash Ripples (Rain & Transitional conditions)
            if (isRainOrTransitional) {
                val isHeavy = condition == WeatherCondition.HEAVY_RAIN || condition == WeatherCondition.THUNDERSTORM
                draw3DRainSystem(
                    rainDrops = rainDrops,
                    splashes = splashes,
                    w = w,
                    h = h,
                    windSpeed = windSpeedMps.toFloat(),
                    heavy = isHeavy,
                    camX = cameraOffsetX,
                    camY = cameraOffsetY
                )
            }

            // Thunderstorm Lightning Bolt & Ambient Flash
            if (condition == WeatherCondition.THUNDERSTORM && currentLightning != null) {
                drawLightning(currentLightning!!, w, h)
            }
            if (lightningFlashAlpha > 0.05f) {
                drawRect(color = Color(0xFF93C5FD).copy(alpha = lightningFlashAlpha * 0.45f))
            }
        }
    }
}

// Draw 3D Perspective Starfield
private fun DrawScope.draw3DStarfield(
    stars: List<Particle3D>,
    w: Float,
    h: Float,
    time: Float,
    camX: Float,
    camY: Float
) {
    stars.forEachIndexed { _, star ->
        val parallaxScale = 80f / (80f + star.z)
        val sx = (star.x * (w / 1000f) + camX * parallaxScale * 0.3f) % w
        val sy = (star.y * (h / 800f) + camY * parallaxScale * 0.3f) % (h * 0.65f)
        val twinkle = 0.4f + 0.6f * sin(time * star.speed + star.seed).coerceIn(0f, 1f)
        val radius = star.size * parallaxScale

        drawCircle(
            color = Color.White.copy(alpha = (star.alpha * twinkle).coerceIn(0f, 1f)),
            radius = radius,
            center = Offset(sx, sy)
        )
    }
}

// Draw 3D Realistic Moon with Lunar Halo
private fun DrawScope.draw3DMoon(
    w: Float,
    h: Float,
    pulse: Float,
    camX: Float,
    camY: Float
) {
    val cx = w * 0.82f + camX * 0.15f
    val cy = h * 0.14f + camY * 0.15f
    val moonR = 36f

    // Outer soft lunar aura
    drawCircle(
        brush = Brush.radialGradient(
            colors = listOf(Color(0x5593C5FD), Color(0x1560A5FA), Color.Transparent),
            center = Offset(cx, cy),
            radius = moonR * 3.5f * pulse
        ),
        radius = moonR * 3.5f * pulse,
        center = Offset(cx, cy)
    )

    // Moon Orb
    drawCircle(
        brush = Brush.radialGradient(
            colors = listOf(Color(0xFFFFFFFF), Color(0xFFE2E8F0), Color(0xFFCBD5E1)),
            center = Offset(cx - moonR * 0.3f, cy - moonR * 0.3f),
            radius = moonR * 1.2f
        ),
        radius = moonR,
        center = Offset(cx, cy)
    )

    // 3D Moon Craters
    drawCircle(color = Color(0x2294A3B8), radius = moonR * 0.22f, center = Offset(cx - 8f, cy - 6f))
    drawCircle(color = Color(0x1E64748B), radius = moonR * 0.16f, center = Offset(cx + 10f, cy + 8f))
    drawCircle(color = Color(0x18475569), radius = moonR * 0.12f, center = Offset(cx + 4f, cy - 14f))
}

// Draw 3D Sun with Rotating Corona Rays
private fun DrawScope.draw3DSun(
    w: Float,
    h: Float,
    pulse: Float,
    rayRot: Float,
    camX: Float,
    camY: Float
) {
    val cx = w * 0.80f + camX * 0.2f
    val cy = h * 0.13f + camY * 0.2f
    val baseR = 48f

    // 1. Vast Corona Ambient Glow
    drawCircle(
        brush = Brush.radialGradient(
            colors = listOf(Color(0x88FFD54F), Color(0x33FFB300), Color.Transparent),
            center = Offset(cx, cy),
            radius = baseR * 4.2f * pulse
        ),
        radius = baseR * 4.2f * pulse,
        center = Offset(cx, cy)
    )

    // 2. Rotating Sun Rays
    val numRays = 12
    val rotRad = rayRot * (PI / 180f).toFloat()
    for (i in 0 until numRays) {
        val angle = (i.toFloat() / numRays) * (PI.toFloat() * 2f) + rotRad
        val rLen = baseR * (1.8f + 0.3f * sin(rayRot * 0.1f + i))
        val x1 = cx + cos(angle) * (baseR * 0.95f)
        val y1 = cy + sin(angle) * (baseR * 0.95f)
        val x2 = cx + cos(angle) * rLen
        val y2 = cy + sin(angle) * rLen

        drawLine(
            brush = Brush.linearGradient(
                colors = listOf(Color(0xCCFFF3B0), Color(0x00FFD54F)),
                start = Offset(x1, y1),
                end = Offset(x2, y2)
            ),
            start = Offset(x1, y1),
            end = Offset(x2, y2),
            strokeWidth = 5f,
            cap = StrokeCap.Round
        )
    }

    // 3. Central Glowing 3D Sun Core
    drawCircle(
        brush = Brush.radialGradient(
            colors = listOf(Color(0xFFFFFFFF), Color(0xFFFFF9C4), Color(0xFFFFCA28)),
            center = Offset(cx - 10f, cy - 10f),
            radius = baseR
        ),
        radius = baseR * pulse,
        center = Offset(cx, cy)
    )
}

// Floating 3D Sun Dust / Solar Motes
private fun DrawScope.draw3DSunDustMotes(
    motes: List<Particle3D>,
    w: Float,
    h: Float,
    time: Float,
    camX: Float,
    camY: Float
) {
    motes.forEach { mote ->
        val pScale = 90f / (90f + mote.z)
        val sx = (mote.x * (w / 1000f) + sin(time * 0.4f + mote.seed) * 20f + camX * pScale * 0.5f) % w
        val sy = (mote.y * (h / 800f) - time * mote.speed * 8f + camY * pScale * 0.5f) % h
        val adjY = if (sy < 0) sy + h else sy
        val r = mote.size * pScale

        drawCircle(
            brush = Brush.radialGradient(
                colors = listOf(Color(0xDDFFF59D), Color(0x66FFD54F), Color.Transparent),
                center = Offset(sx, adjY),
                radius = r * 2f
            ),
            radius = r * 2f,
            center = Offset(sx, adjY)
        )
    }
}

// Draw Multi-Layered 3D Volumetric Cloud Clusters
private fun DrawScope.draw3DVolumetricClouds(
    w: Float,
    h: Float,
    t: Float,
    condition: WeatherCondition,
    isNight: Boolean,
    windSpeed: Float,
    camX: Float,
    camY: Float
) {
    val cloudColor = when {
        isNight -> Color(0x55334155)
        condition in listOf(WeatherCondition.THUNDERSTORM, WeatherCondition.HEAVY_RAIN) -> Color(0xCC1E293B)
        condition in listOf(WeatherCondition.CLOUDY, WeatherCondition.OVERCAST) -> Color(0xAA94A3B8)
        else -> Color(0xBBFFFFFF)
    }

    val windOffset = (t * (windSpeed.coerceIn(2f, 15f) * 1.8f)) % (w + 400f)

    // Layer 1: Distant Background Slow Layer
    drawCloudCluster(
        centerX = ((w * 0.3f + windOffset * 0.4f) % (w + 400f)) - 200f + camX * 0.1f,
        centerY = h * 0.18f + camY * 0.1f,
        scale = 1.3f,
        baseColor = cloudColor.copy(alpha = 0.45f)
    )

    // Layer 2: Mid-level Cumulus
    drawCloudCluster(
        centerX = ((w * 0.75f + windOffset * 0.7f) % (w + 400f)) - 200f + camX * 0.25f,
        centerY = h * 0.25f + camY * 0.25f,
        scale = 1.6f,
        baseColor = cloudColor.copy(alpha = 0.7f)
    )

    // Layer 3: Foreground Fast Drifting Fluffy Layer
    drawCloudCluster(
        centerX = ((w * 0.1f + windOffset * 1.1f) % (w + 400f)) - 200f + camX * 0.4f,
        centerY = h * 0.35f + camY * 0.4f,
        scale = 1.9f,
        baseColor = cloudColor
    )
}

private fun DrawScope.drawCloudCluster(
    centerX: Float,
    centerY: Float,
    scale: Float,
    baseColor: Color
) {
    val offsets = listOf(
        Offset(0f, 0f) to 55f,
        Offset(-40f, 8f) to 42f,
        Offset(40f, 6f) to 46f,
        Offset(-70f, 16f) to 32f,
        Offset(75f, 14f) to 36f,
        Offset(0f, -18f) to 48f
    )

    offsets.forEach { (offset, radius) ->
        val pos = Offset(centerX + offset.x * scale, centerY + offset.y * scale)
        val r = radius * scale
        drawCircle(
            brush = Brush.radialGradient(
                colors = listOf(baseColor, baseColor.copy(alpha = baseColor.alpha * 0.6f), Color.Transparent),
                center = pos,
                radius = r
            ),
            radius = r,
            center = pos
        )
    }
}

// 3D Rain System with Splashes
private fun DrawScope.draw3DRainSystem(
    rainDrops: List<Particle3D>,
    splashes: MutableList<SplashRing>,
    w: Float,
    h: Float,
    windSpeed: Float,
    heavy: Boolean,
    camX: Float,
    camY: Float
) {
    val groundY = h * 0.92f
    val slant = (windSpeed * 0.25f).coerceIn(-12f, 12f)
    val rainColor = if (heavy) Color(0xDDE0F2FE) else Color(0xBBBAE6FD)

    rainDrops.forEach { drop ->
        val pScale = 50f / (50f + drop.z)
        drop.y += drop.speed * (if (heavy) 1.6f else 1.0f)
        drop.x += slant

        if (drop.y > groundY) {
            // Spawn splash on ground
            if (splashes.size < 40 && Random.nextFloat() < 0.35f) {
                splashes.add(SplashRing(drop.x, groundY + Random.nextFloat() * 20f))
            }
            drop.y = -30f
            drop.x = Random.nextFloat() * (w + 200f) - 100f
        }

        val sx = drop.x + camX * pScale * 0.5f
        val sy = drop.y + camY * pScale * 0.5f
        val len = (14f + drop.speed * 1.5f) * pScale

        drawLine(
            color = rainColor.copy(alpha = (0.35f + 0.65f * pScale).coerceIn(0f, 1f)),
            start = Offset(sx, sy),
            end = Offset(sx + slant * 2f, sy + len),
            strokeWidth = drop.size * pScale,
            cap = StrokeCap.Round
        )
    }

    // Animate and Draw Splash Rings
    val iterator = splashes.iterator()
    while (iterator.hasNext()) {
        val s = iterator.next()
        s.radius += 1.2f
        s.alpha -= 0.05f
        if (s.alpha <= 0f || s.radius >= s.maxRadius) {
            iterator.remove()
        } else {
            drawOval(
                color = Color(0xCCBAE6FD).copy(alpha = s.alpha.coerceIn(0f, 1f)),
                topLeft = Offset(s.x - s.radius, s.y - s.radius * 0.35f),
                size = Size(s.radius * 2f, s.radius * 0.7f),
                style = Stroke(width = 1.8f)
            )
        }
    }
}

// Draw Branching Lightning Bolt
private fun DrawScope.drawLightning(bolt: LightningBolt, w: Float, h: Float) {
    for (i in 0 until bolt.points.size - 1) {
        val p1 = bolt.points[i]
        val p2 = bolt.points[i + 1]
        drawLine(
            color = Color(0x8893C5FD),
            start = p1,
            end = p2,
            strokeWidth = 7f,
            cap = StrokeCap.Round
        )
        drawLine(
            color = Color.White,
            start = p1,
            end = p2,
            strokeWidth = 2.5f,
            cap = StrokeCap.Round
        )
    }

    bolt.branches.forEach { branch ->
        for (i in 0 until branch.size - 1) {
            val p1 = branch[i]
            val p2 = branch[i + 1]
            drawLine(
                color = Color(0xCCBFDBFE),
                start = p1,
                end = p2,
                strokeWidth = 1.8f,
                cap = StrokeCap.Round
            )
        }
    }
}

// Draw 3D Horizon / Mountain Silhouettes
private fun DrawScope.draw3DHorizonSilhouettes(
    w: Float,
    h: Float,
    isNight: Boolean,
    condition: WeatherCondition,
    camX: Float,
    camY: Float
) {
    val hillColor = when {
        isNight -> Color(0x88020617)
        condition in listOf(WeatherCondition.THUNDERSTORM, WeatherCondition.HEAVY_RAIN) -> Color(0x990F172A)
        else -> Color(0x550F172A)
    }

    val path1 = Path().apply {
        moveTo(0f, h)
        lineTo(0f, h * 0.90f + camY * 0.08f)
        cubicTo(
            w * 0.25f + camX * 0.1f, h * 0.86f,
            w * 0.50f + camX * 0.1f, h * 0.92f,
            w * 0.75f + camX * 0.1f, h * 0.87f
        )
        cubicTo(
            w * 0.90f + camX * 0.1f, h * 0.85f,
            w * 0.98f, h * 0.89f,
            w, h * 0.88f
        )
        lineTo(w, h)
        close()
    }
    drawPath(path = path1, brush = Brush.verticalGradient(listOf(hillColor, hillColor.copy(alpha = 0.9f))))
}
