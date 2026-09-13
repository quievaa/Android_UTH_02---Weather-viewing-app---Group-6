package com.example.android_uth_02_weather_viewing_app_group6.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.MyLocation
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material.icons.outlined.Layers
import androidx.compose.material.icons.outlined.LocationOn
import androidx.compose.material3.Icon
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import android.Manifest
import android.content.pm.PackageManager
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.ui.platform.LocalContext
import androidx.core.content.ContextCompat
import com.example.android_uth_02_weather_viewing_app_group6.data.location.LocationTracker
import com.example.android_uth_02_weather_viewing_app_group6.ui.components.GlassCard
import com.example.android_uth_02_weather_viewing_app_group6.ui.components.OsmdroidRadarMapView
import com.example.android_uth_02_weather_viewing_app_group6.ui.model.RadarLayer
import com.example.android_uth_02_weather_viewing_app_group6.ui.viewmodel.WeatherUiState
import com.example.android_uth_02_weather_viewing_app_group6.ui.viewmodel.WeatherViewModel

@Composable
fun RadarScreen(
    contentPadding: PaddingValues,
    viewModel: WeatherViewModel,
    locationTracker: LocationTracker? = null
) {
    val context = LocalContext.current
    val uiState by viewModel.uiState.collectAsState()
    val zoom by viewModel.radarZoom.collectAsState()
    val timeline by viewModel.radarTimelinePosition.collectAsState()
    val isPlaying by viewModel.isRadarPlaying.collectAsState()
    val layer by viewModel.selectedRadarLayer.collectAsState()

    val weatherSuccess = (uiState as? WeatherUiState.Success)?.weather
    val currentLat = weatherSuccess?.latitude ?: 10.8231
    val currentLon = weatherSuccess?.longitude ?: 106.6297
    val currentCityName = weatherSuccess?.cityName ?: "Thành phố Hồ Chí Minh"

    var showLayerMenu by remember { mutableStateOf(false) }
    var recenterCounter by remember { mutableStateOf(0) }

    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        val fineGranted = permissions[Manifest.permission.ACCESS_FINE_LOCATION] ?: false
        val coarseGranted = permissions[Manifest.permission.ACCESS_COARSE_LOCATION] ?: false

        if (fineGranted || coarseGranted) {
            if (locationTracker != null) {
                Toast.makeText(context, "Đang lấy vị trí GPS hiện tại...", Toast.LENGTH_SHORT).show()
                viewModel.fetchLocationWeather(locationTracker) { success, msg ->
                    if (success) {
                        recenterCounter++
                    } else {
                        Toast.makeText(context, msg ?: "Không thể lấy vị trí GPS", Toast.LENGTH_SHORT).show()
                    }
                }
            }
        } else {
            Toast.makeText(
                context,
                "Ứng dụng cần quyền vị trí để định vị GPS trên bản đồ.",
                Toast.LENGTH_SHORT
            ).show()
        }
    }

    val requestGpsAndRecenter = {
        val hasFine = ContextCompat.checkSelfPermission(
            context,
            Manifest.permission.ACCESS_FINE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED

        val hasCoarse = ContextCompat.checkSelfPermission(
            context,
            Manifest.permission.ACCESS_COARSE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED

        if (hasFine || hasCoarse) {
            if (locationTracker != null) {
                Toast.makeText(context, "Đang lấy vị trí GPS hiện tại...", Toast.LENGTH_SHORT).show()
                viewModel.fetchLocationWeather(locationTracker) { success, msg ->
                    if (success) {
                        recenterCounter++
                    } else {
                        Toast.makeText(context, msg ?: "Không thể lấy vị trí GPS", Toast.LENGTH_SHORT).show()
                        recenterCounter++
                    }
                }
            } else {
                recenterCounter++
            }
        } else {
            permissionLauncher.launch(
                arrayOf(
                    Manifest.permission.ACCESS_FINE_LOCATION,
                    Manifest.permission.ACCESS_COARSE_LOCATION
                )
            )
        }
    }

    Box(modifier = Modifier.fillMaxSize()) {
        key(recenterCounter, currentLat, currentLon) {
            OsmdroidRadarMapView(
                latitude = currentLat,
                longitude = currentLon,
                cityName = currentCityName,
                selectedLayer = layer,
                zoomLevel = zoom
            )
        }

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(
                    start = 16.dp,
                    end = 16.dp,
                    top = contentPadding.calculateTopPadding() + 10.dp
                ),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(
                modifier = Modifier
                    .clip(RoundedCornerShape(24.dp))
                    .background(Color(0xCC0E7490))
                    .border(1.dp, Color(0x6638BDF8), RoundedCornerShape(24.dp))
                    .clickable { requestGpsAndRecenter() }
                    .padding(horizontal = 14.dp, vertical = 10.dp)
                    .testTag("radar_location_chip"),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Outlined.LocationOn,
                    contentDescription = "Location",
                    tint = Color.White,
                    modifier = Modifier.size(18.dp)
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = currentCityName,
                    color = Color.White,
                    fontSize = 15.sp,
                    fontWeight = FontWeight.SemiBold
                )
            }

            Box {
                Box(
                    modifier = Modifier
                        .size(44.dp)
                        .clip(CircleShape)
                        .background(Color(0xCC0E7490))
                        .border(1.dp, Color(0x6638BDF8), CircleShape)
                        .clickable { showLayerMenu = !showLayerMenu }
                        .testTag("radar_layer_toggle_button"),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Outlined.Layers,
                        contentDescription = "Layers",
                        tint = Color.White,
                        modifier = Modifier.size(22.dp)
                    )
                }

                if (showLayerMenu) {
                    LayerMenuDropdown(
                        selectedLayer = layer,
                        onSelectLayer = {
                            viewModel.selectRadarLayer(it)
                            showLayerMenu = false
                        },
                        onDismiss = { showLayerMenu = false }
                    )
                }
            }
        }

        Column(
            modifier = Modifier
                .align(Alignment.CenterEnd)
                .padding(end = 16.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            MapControlButton(
                icon = Icons.Default.Add,
                contentDescription = "Phóng to",
                onClick = { viewModel.zoomInRadar() },
                testTag = "radar_zoom_in"
            )

            MapControlButton(
                icon = Icons.Default.Remove,
                contentDescription = "Thu nhỏ",
                onClick = { viewModel.zoomOutRadar() },
                testTag = "radar_zoom_out"
            )

            MapControlButton(
                icon = Icons.Default.MyLocation,
                contentDescription = "Căn giữa GPS",
                onClick = { requestGpsAndRecenter() },
                testTag = "radar_recenter"
            )
        }

        GlassCard(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(
                    start = 16.dp,
                    end = 16.dp,
                    bottom = contentPadding.calculateBottomPadding() + 80.dp
                )
                .fillMaxWidth()
                .testTag("radar_timeline_bar"),
            cornerRadius = 24.dp,
            backgroundColor = Color(0xCC0F172A),
            borderColor = Color(0x3338BDF8)
        ) {
            Column(modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "QUÁ KHỨ",
                        color = if (timeline < 0.45f) Color.White else Color(0x9994A3B8),
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold
                    )

                    val timeLabel = when {
                        timeline < 0.25f -> "-2 Giờ trước"
                        timeline < 0.45f -> "-1 Giờ trước"
                        timeline in 0.45f..0.75f -> "Hiện tại (Now)"
                        else -> "+1 Giờ tới"
                    }

                    Text(
                        text = timeLabel,
                        color = Color(0xFF38BDF8),
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold
                    )

                    Text(
                        text = "TƯƠNG LAI",
                        color = if (timeline > 0.75f) Color.White else Color(0x9994A3B8),
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold
                    )
                }

                Spacer(modifier = Modifier.height(6.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(36.dp)
                            .clip(CircleShape)
                            .background(Color.White)
                            .clickable { viewModel.toggleRadarPlay() }
                            .testTag("radar_play_pause_button"),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = if (isPlaying) Icons.Default.Pause else Icons.Default.PlayArrow,
                            contentDescription = if (isPlaying) "Tạm dừng" else "Phát",
                            tint = Color(0xFF0F172A),
                            modifier = Modifier.size(22.dp)
                        )
                    }

                    Spacer(modifier = Modifier.width(12.dp))

                    Slider(
                        value = timeline,
                        onValueChange = { viewModel.setRadarTimeline(it) },
                        modifier = Modifier
                            .weight(1f)
                            .testTag("radar_timeline_slider"),
                        colors = SliderDefaults.colors(
                            thumbColor = Color.White,
                            activeTrackColor = Color(0xFF38BDF8),
                            inactiveTrackColor = Color(0x4D64748B)
                        )
                    )
                }
            }
        }
    }
}

@Composable
private fun MapControlButton(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    contentDescription: String,
    onClick: () -> Unit,
    testTag: String
) {
    Box(
        modifier = Modifier
            .size(44.dp)
            .shadow(8.dp, CircleShape)
            .clip(CircleShape)
            .background(Color(0xCC0F172A))
            .border(1.dp, Color(0x4D38BDF8), CircleShape)
            .clickable { onClick() }
            .testTag(testTag),
        contentAlignment = Alignment.Center
    ) {
        Icon(
            imageVector = icon,
            contentDescription = contentDescription,
            tint = Color.White,
            modifier = Modifier.size(20.dp)
        )
    }
}

@Composable
private fun LayerMenuDropdown(
    selectedLayer: RadarLayer,
    onSelectLayer: (RadarLayer) -> Unit,
    onDismiss: () -> Unit
) {
    Box(
        modifier = Modifier
            .padding(top = 52.dp)
            .clip(RoundedCornerShape(16.dp))
            .background(Color(0xE60F172A))
            .border(1.dp, Color(0x3338BDF8), RoundedCornerShape(16.dp))
            .padding(8.dp)
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
            RadarLayer.entries.forEach { layerItem ->
                val isSelected = layerItem == selectedLayer
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(8.dp))
                        .background(if (isSelected) Color(0x3338BDF8) else Color.Transparent)
                        .clickable { onSelectLayer(layerItem) }
                        .padding(horizontal = 12.dp, vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = layerItem.titleVi,
                        color = if (isSelected) Color(0xFF38BDF8) else Color.White,
                        fontSize = 13.sp,
                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                    )
                }
            }
        }
    }
}
