package com.example.android_uth_02_weather_viewing_app_group6.ui.components

import android.net.Uri
import android.view.ViewGroup
import android.widget.FrameLayout
import androidx.annotation.OptIn
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.viewinterop.AndroidView
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.media3.common.C
import androidx.media3.common.MediaItem
import androidx.media3.common.Player
import androidx.media3.common.util.UnstableApi
import androidx.media3.exoplayer.DefaultLoadControl
import androidx.media3.exoplayer.DefaultRenderersFactory
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.ui.AspectRatioFrameLayout
import androidx.media3.ui.PlayerView
import com.example.android_uth_02_weather_viewing_app_group6.ui.model.WeatherCondition

/**
 * Lấy đường dẫn file video trong thư mục Android Assets (assets/weather_videos/)
 * Giúp tối ưu hóa tốc độ đọc file trực tiếp qua AssetDataSource của ExoPlayer
 */
fun getWeatherVideoAssetUri(condition: WeatherCondition): String {
    return when (condition) {
        WeatherCondition.SUNNY -> "asset:///weather_videos/bg_sunny.mp4"
        WeatherCondition.PARTLY_CLOUDY,
        WeatherCondition.NIGHT_CLEAR -> "asset:///weather_videos/bg_clear_day.mp4"
        WeatherCondition.CLOUDY,
        WeatherCondition.OVERCAST,
        WeatherCondition.NIGHT_CLOUDY -> "asset:///weather_videos/bg_cloudy_rain.mp4"
        WeatherCondition.LIGHT_RAIN,
        WeatherCondition.RAIN,
        WeatherCondition.HEAVY_RAIN,
        WeatherCondition.THUNDERSTORM -> "asset:///weather_videos/bg_rain.mp4"
    }
}

/**
 * Lấy dải màu nền tĩnh sống động theo thời tiết (Base Gradient)
 */
fun getWeatherBaseGradient(condition: WeatherCondition): List<Color> {
    return when (condition) {
        WeatherCondition.SUNNY -> listOf(
            Color(0xFF0284C7),
            Color(0xFF0369A1),
            Color(0xFF075985)
        )
        WeatherCondition.PARTLY_CLOUDY -> listOf(
            Color(0xFF38BDF8),
            Color(0xFF0284C7),
            Color(0xFF0C4A6E)
        )
        WeatherCondition.CLOUDY,
        WeatherCondition.OVERCAST -> listOf(
            Color(0xFF475569),
            Color(0xFF334155),
            Color(0xFF1E293B)
        )
        WeatherCondition.LIGHT_RAIN,
        WeatherCondition.RAIN,
        WeatherCondition.HEAVY_RAIN,
        WeatherCondition.THUNDERSTORM -> listOf(
            Color(0xFF1E293B),
            Color(0xFF0F172A),
            Color(0xFF020617)
        )
        WeatherCondition.NIGHT_CLEAR -> listOf(
            Color(0xFF0F172A),
            Color(0xFF020617),
            Color(0xFF020617)
        )
        WeatherCondition.NIGHT_CLOUDY -> listOf(
            Color(0xFF1E293B),
            Color(0xFF0F172A),
            Color(0xFF020617)
        )
    }
}

/**
 * Composable hiển thị video nền động thời tiết phát trực tiếp từ Android Assets:
 * 1. Đọc trực tiếp từ thư mục `assets/weather_videos/` với ExoPlayer AssetDataSource.
 * 2. Cấu hình giải mã phần cứng tối ưu, tắt âm thanh để giảm tải CPU.
 * 3. Lớp phủ Gradient đa tầng đảm bảo tương phản cho chữ trắng và GlassCard.
 */
@OptIn(UnstableApi::class)
@Composable
fun VideoWeatherBackground(
    condition: WeatherCondition,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current
    val videoAssetUri = remember(condition) { getWeatherVideoAssetUri(condition) }
    val baseGradient = remember(condition) { getWeatherBaseGradient(condition) }

    val exoPlayer = remember {
        val renderersFactory = DefaultRenderersFactory(context)
            .setExtensionRendererMode(DefaultRenderersFactory.EXTENSION_RENDERER_MODE_OFF)
            .setEnableDecoderFallback(true)

        val loadControl = DefaultLoadControl.Builder()
            .setBufferDurationsMs(500, 1500, 250, 500)
            .build()

        ExoPlayer.Builder(context, renderersFactory)
            .setLoadControl(loadControl)
            .build()
            .apply {
                repeatMode = Player.REPEAT_MODE_ALL
                volume = 0f
                videoScalingMode = C.VIDEO_SCALING_MODE_SCALE_TO_FIT_WITH_CROPPING
                trackSelectionParameters = trackSelectionParameters
                    .buildUpon()
                    .setTrackTypeDisabled(C.TRACK_TYPE_AUDIO, true)
                    .build()
            }
    }

    LaunchedEffect(videoAssetUri) {
        val uri = Uri.parse(videoAssetUri)
        exoPlayer.setMediaItem(MediaItem.fromUri(uri))
        exoPlayer.prepare()
        exoPlayer.playWhenReady = true
    }

    DisposableEffect(lifecycleOwner, exoPlayer) {
        val observer = LifecycleEventObserver { _, event ->
            when (event) {
                Lifecycle.Event.ON_PAUSE -> exoPlayer.pause()
                Lifecycle.Event.ON_RESUME -> exoPlayer.play()
                else -> {}
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)

        onDispose {
            lifecycleOwner.lifecycle.removeObserver(observer)
            exoPlayer.stop()
            exoPlayer.release()
        }
    }

    Box(modifier = modifier.fillMaxSize()) {
        // 1. Nền Gradient tức thì
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Brush.verticalGradient(baseGradient))
        )

        // 2. Video PlayerView đọc từ assets
        AndroidView(
            factory = { ctx ->
                PlayerView(ctx).apply {
                    player = exoPlayer
                    useController = false
                    setShowBuffering(PlayerView.SHOW_BUFFERING_NEVER)
                    setShutterBackgroundColor(android.graphics.Color.TRANSPARENT)
                    resizeMode = AspectRatioFrameLayout.RESIZE_MODE_ZOOM
                    layoutParams = FrameLayout.LayoutParams(
                        ViewGroup.LayoutParams.MATCH_PARENT,
                        ViewGroup.LayoutParams.MATCH_PARENT
                    )
                }
            },
            modifier = Modifier.fillMaxSize()
        )

        // 3. Lớp phủ tối tăng tương phản
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    Brush.verticalGradient(
                        colors = listOf(
                            Color.Black.copy(alpha = 0.35f),
                            Color.Black.copy(alpha = 0.15f),
                            Color.Black.copy(alpha = 0.45f),
                            Color.Black.copy(alpha = 0.70f)
                        )
                    )
                )
        )
    }
}
