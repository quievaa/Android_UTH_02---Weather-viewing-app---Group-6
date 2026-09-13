package com.example.android_uth_02_weather_viewing_app_group6.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.Canvas
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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material.icons.filled.MoreHoriz
import androidx.compose.material.icons.outlined.Air
import androidx.compose.material.icons.outlined.InvertColors
import androidx.compose.material.icons.outlined.WaterDrop
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.android_uth_02_weather_viewing_app_group6.ui.components.GlassCard
import com.example.android_uth_02_weather_viewing_app_group6.ui.components.TemperatureRangeBar
import com.example.android_uth_02_weather_viewing_app_group6.ui.components.Weather3DBackground
import com.example.android_uth_02_weather_viewing_app_group6.ui.components.WeatherIcon
import com.example.android_uth_02_weather_viewing_app_group6.ui.model.DailyForecast
import com.example.android_uth_02_weather_viewing_app_group6.ui.model.WeatherCondition
import com.example.android_uth_02_weather_viewing_app_group6.ui.viewmodel.WeatherUiState
import com.example.android_uth_02_weather_viewing_app_group6.ui.viewmodel.WeatherViewModel

@Composable
fun ForecastScreen(
    contentPadding: PaddingValues,
    viewModel: WeatherViewModel
) {
    val uiState by viewModel.uiState.collectAsState()
    val expandedIndex by viewModel.expandedForecastIndex.collectAsState()

    val (currentTemp, currentCond) = when (val state = uiState) {
        is WeatherUiState.Success -> Pair(state.weather.temperatureC, state.weather.description)
        else -> Pair(31.0, "Nắng đẹp")
    }

    val forecastList = remember(currentTemp, currentCond) {
        viewModel.getTenDayForecastList(currentTemp, currentCond)
    }

    val weatherSuccess = (uiState as? WeatherUiState.Success)?.weather
    val isNight = weatherSuccess?.iconCode?.endsWith("n") == true || weatherSuccess?.description?.lowercase()?.contains("đêm") == true
    val weatherCond = weatherSuccess?.let { WeatherCondition.fromDescription(it.description, isNight) } ?: WeatherCondition.fromDescription(currentCond, isNight)
    val windSpeed = weatherSuccess?.windSpeedMps ?: 5.0

    Box(
        modifier = Modifier.fillMaxSize()
    ) {
        // Dynamic Three.js 3D Weather Background
        Weather3DBackground(
            condition = weatherCond,
            isNight = isNight,
            windSpeedMps = windSpeed,
            temperatureC = currentTemp
        )

        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(
                start = 16.dp,
                end = 16.dp,
                top = contentPadding.calculateTopPadding() + 8.dp,
                bottom = contentPadding.calculateBottomPadding() + 90.dp
            ),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // Header Section: "Dự báo 7-10 ngày"
            item {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 4.dp, vertical = 6.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = "Dự báo 10 ngày",
                            color = Color.White,
                            fontSize = 28.sp,
                            fontWeight = FontWeight.Bold,
                            letterSpacing = (-0.5).sp
                        )
                        Text(
                            text = (uiState as? WeatherUiState.Success)?.weather?.cityName ?: "TP. Hồ Chí Minh",
                            color = Color(0xCC38BDF8),
                            fontSize = 13.sp
                        )
                    }

                    IconButton(
                        onClick = { viewModel.retry() },
                        modifier = Modifier.testTag("forecast_refresh_button")
                    ) {
                        Icon(
                            imageVector = Icons.Default.MoreHoriz,
                            contentDescription = "More",
                            tint = Color.White,
                            modifier = Modifier.size(28.dp)
                        )
                    }
                }
            }

            // Accordion Items with stable keys
            itemsIndexed(
                items = forecastList,
                key = { index: Int, forecast: DailyForecast -> "${forecast.dayName}_$index" }
            ) { index: Int, forecast: DailyForecast ->
                val isExpanded = expandedIndex == index
                ForecastAccordionCard(
                    forecast = forecast,
                    isExpanded = isExpanded,
                    onCardClick = { viewModel.toggleForecastExpand(index) },
                    testTag = "forecast_day_$index"
                )
            }
        }
    }
}

@Composable
private fun ForecastAccordionCard(
    forecast: DailyForecast,
    isExpanded: Boolean,
    onCardClick: () -> Unit,
    testTag: String
) {
    GlassCard(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onCardClick() }
            .testTag(testTag),
        cornerRadius = 22.dp,
        backgroundColor = if (isExpanded) Color(0x3D1E293B) else Color(0x241E293B),
        borderColor = if (isExpanded) Color(0x33818CF8) else Color(0x1A94A3B8)
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            // Header Row: Day name | Icon + Pop% | Min temp [ Bar ] Max temp | Arrow
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Day Name
                Text(
                    text = forecast.dayName,
                    color = Color.White,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.SemiBold,
                    modifier = Modifier.width(72.dp)
                )

                // Weather Icon + Precipitation Chance
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.width(62.dp)
                ) {
                    WeatherIcon(
                        condition = forecast.condition,
                        size = 24.dp
                    )
                    if (forecast.pop > 0) {
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = "${forecast.pop}%",
                            color = Color(0xFF38BDF8),
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }

                // Min Temp
                Text(
                    text = "${forecast.minTemp}°",
                    color = Color(0xCCFFFFFF),
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Normal,
                    modifier = Modifier.width(28.dp)
                )

                // Temperature Range Bar
                TemperatureRangeBar(
                    min = forecast.minTemp,
                    max = forecast.maxTemp,
                    modifier = Modifier
                        .weight(1f)
                        .padding(horizontal = 8.dp)
                )

                // Max Temp
                Text(
                    text = "${forecast.maxTemp}°",
                    color = Color.White,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.SemiBold,
                    modifier = Modifier.width(28.dp)
                )

                Spacer(modifier = Modifier.width(6.dp))

                // Expand/Collapse Icon
                Icon(
                    imageVector = if (isExpanded) Icons.Default.KeyboardArrowUp else Icons.Default.KeyboardArrowDown,
                    contentDescription = if (isExpanded) "Thu gọn" else "Mở rộng",
                    tint = Color(0xCCFFFFFF),
                    modifier = Modifier.size(20.dp)
                )
            }

            // Expanded Details Section
            AnimatedVisibility(visible = isExpanded) {
                Column(
                    modifier = Modifier.padding(top = 16.dp)
                ) {
                    // Summary Narrative
                    Text(
                        text = forecast.summary,
                        color = Color(0xE6FFFFFF),
                        fontSize = 13.sp,
                        lineHeight = 19.sp
                    )

                    Spacer(modifier = Modifier.height(14.dp))

                    // 3 Metric Stat Tiles Row
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        // Tile 1: Wind
                        MetricTile(
                            icon = Icons.Outlined.Air,
                            value = "${forecast.windSpeedKmH} km/h",
                            label = "Gió",
                            modifier = Modifier.weight(1f)
                        )

                        // Tile 2: Humidity
                        MetricTile(
                            icon = Icons.Outlined.InvertColors,
                            value = "${forecast.humidityPercent}%",
                            label = "Độ ẩm",
                            modifier = Modifier.weight(1f)
                        )

                        // Tile 3: Rainfall
                        MetricTile(
                            icon = Icons.Outlined.WaterDrop,
                            value = "${forecast.rainfallMm} mm",
                            label = "Lượng mưa",
                            modifier = Modifier.weight(1f)
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun MetricTile(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    value: String,
    label: String,
    modifier: Modifier = Modifier
) {
    GlassCard(
        modifier = modifier.height(84.dp),
        cornerRadius = 16.dp,
        backgroundColor = Color(0x260F172A),
        borderColor = Color(0x1AFFFFFF)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(8.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = label,
                tint = Color(0xCC38BDF8),
                modifier = Modifier.size(18.dp)
            )

            Spacer(modifier = Modifier.height(4.dp))

            Text(
                text = value,
                color = Color.White,
                fontSize = 13.sp,
                fontWeight = FontWeight.SemiBold
            )

            Text(
                text = label,
                color = Color(0x9994A3B8),
                fontSize = 11.sp
            )
        }
    }
}

