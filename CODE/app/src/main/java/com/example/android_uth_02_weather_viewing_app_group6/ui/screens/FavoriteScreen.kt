package com.example.android_uth_02_weather_viewing_app_group6.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.Canvas
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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.CompareArrows
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.outlined.Air
import androidx.compose.material.icons.outlined.WaterDrop
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.android_uth_02_weather_viewing_app_group6.data.model.FavoriteCity
import com.example.android_uth_02_weather_viewing_app_group6.ui.components.GlassCard
import com.example.android_uth_02_weather_viewing_app_group6.ui.components.Weather3DBackground
import com.example.android_uth_02_weather_viewing_app_group6.ui.components.WeatherIcon
import com.example.android_uth_02_weather_viewing_app_group6.ui.model.WeatherCondition
import com.example.android_uth_02_weather_viewing_app_group6.ui.viewmodel.WeatherViewModel

@Composable
fun FavoriteScreen(
    contentPadding: PaddingValues,
    viewModel: WeatherViewModel,
    onCitySelected: (String) -> Unit = {},
    onNavigateSearch: () -> Unit = {}
) {
    val favoriteCities by viewModel.favoriteCities.collectAsState()
    var isCompareMode by remember { mutableStateOf(false) }

    Box(
        modifier = Modifier.fillMaxSize()
    ) {
        // Dynamic 3D Atmosphere Background
        Weather3DBackground(
            condition = WeatherCondition.PARTLY_CLOUDY,
            isNight = false,
            windSpeedMps = 3.5,
            temperatureC = 28.0
        )

        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(
                start = 16.dp,
                end = 16.dp,
                top = contentPadding.calculateTopPadding() + 8.dp,
                bottom = contentPadding.calculateBottomPadding() + 90.dp
            ),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            // Header Section: "Địa điểm yêu thích" + Action buttons
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
                            text = "Địa điểm yêu thích",
                            color = Color.White,
                            fontSize = 28.sp,
                            fontWeight = FontWeight.Bold,
                            letterSpacing = (-0.5).sp
                        )
                        Text(
                            text = "Đã lưu ${favoriteCities.size} thành phố",
                            color = Color(0xCC38BDF8),
                            fontSize = 13.sp
                        )
                    }

                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        if (favoriteCities.size >= 2) {
                            IconButton(
                                onClick = { isCompareMode = !isCompareMode },
                                modifier = Modifier
                                    .clip(CircleShape)
                                    .background(if (isCompareMode) Color(0x660284C7) else Color(0x331E293B))
                            ) {
                                Icon(
                                    imageVector = Icons.AutoMirrored.Filled.CompareArrows,
                                    contentDescription = "So sánh",
                                    tint = if (isCompareMode) Color(0xFF38BDF8) else Color.White
                                )
                            }
                        }

                        IconButton(
                            onClick = onNavigateSearch,
                            modifier = Modifier
                                .clip(CircleShape)
                                .background(Color(0x331E293B))
                        ) {
                            Icon(
                                imageVector = Icons.Default.Add,
                                contentDescription = "Thêm thành phố",
                                tint = Color.White
                            )
                        }
                    }
                }
            }

            // Compare Split View Card if enabled
            if (isCompareMode && favoriteCities.size >= 2) {
                item {
                    CitiesComparisonCard(
                        cityA = favoriteCities[0],
                        cityB = favoriteCities[1]
                    )
                }
            }

            // Empty state when no favorite cities saved
            if (favoriteCities.isEmpty()) {
                item {
                    EmptyFavoritesCard(
                        onExploreClick = onNavigateSearch,
                        onAddDefault = { cityName ->
                            viewModel.toggleFavorite(cityName = cityName)
                        }
                    )
                }
            } else {
                // Saved Cities List
                items(favoriteCities, key = { it.id }) { city ->
                    FavoriteCityGlassCard(
                        city = city,
                        onClick = { onCitySelected(city.cityName) },
                        onDelete = { viewModel.removeFavorite(city.id) }
                    )
                }
            }
        }
    }
}

@Composable
private fun FavoriteCityGlassCard(
    city: FavoriteCity,
    onClick: () -> Unit,
    onDelete: () -> Unit
) {
    // Generate deterministic mockup weather condition for list item preview
    val condition = when (city.cityName.lowercase()) {
        "ha noi", "hà nội" -> WeatherCondition.LIGHT_RAIN
        "da nang", "đà nẵng" -> WeatherCondition.PARTLY_CLOUDY
        "da lat", "đà lạt" -> WeatherCondition.CLOUDY
        "nha trang" -> WeatherCondition.SUNNY
        else -> WeatherCondition.SUNNY
    }

    val temp = when (city.cityName.lowercase()) {
        "ha nội", "hà nội" -> "28°"
        "da nang", "đà nẵng" -> "30°"
        "da lat", "đà lạt" -> "19°"
        "nha trang" -> "31°"
        else -> "32°"
    }

    GlassCard(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
            .testTag("favorite_city_${city.id}"),
        cornerRadius = 22.dp,
        backgroundColor = Color(0x331E293B),
        borderColor = Color(0x3394A3B8)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(18.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.weight(1f)
            ) {
                WeatherIcon(
                    condition = condition,
                    size = 40.dp
                )

                Spacer(modifier = Modifier.width(14.dp))

                Column {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = city.cityName,
                            color = Color.White,
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Icon(
                            imageVector = Icons.Default.Favorite,
                            contentDescription = null,
                            tint = Color(0xFFF43F5E),
                            modifier = Modifier.size(15.dp)
                        )
                    }

                    Spacer(modifier = Modifier.height(2.dp))

                    Text(
                        text = "${city.country} • ${condition.labelVi}",
                        color = Color(0xCCFFFFFF),
                        fontSize = 13.sp
                    )
                }
            }

            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = temp,
                    color = Color.White,
                    fontSize = 28.sp,
                    fontWeight = FontWeight.Light
                )

                Spacer(modifier = Modifier.width(8.dp))

                IconButton(
                    onClick = onDelete,
                    modifier = Modifier.size(36.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Delete,
                        contentDescription = "Xóa khỏi yêu thích",
                        tint = Color(0x99F87171),
                        modifier = Modifier.size(18.dp)
                    )
                }
            }
        }
    }
}

@Composable
private fun CitiesComparisonCard(
    cityA: FavoriteCity,
    cityB: FavoriteCity
) {
    GlassCard(
        modifier = Modifier.fillMaxWidth(),
        cornerRadius = 24.dp,
        backgroundColor = Color(0x4D0E7490),
        borderColor = Color(0x6638BDF8)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = "⚡ So sánh nhanh thời tiết",
                color = Color(0xFFBAE6FD),
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold
            )

            Spacer(modifier = Modifier.height(12.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                // Column City A
                Column(
                    modifier = Modifier.weight(1f),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = cityA.cityName,
                        color = Color.White,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    WeatherIcon(condition = WeatherCondition.SUNNY, size = 32.dp)
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(text = "31°C", color = Color.White, fontSize = 22.sp, fontWeight = FontWeight.Light)
                    Text(text = "Độ ẩm 70%", color = Color(0xCCFFFFFF), fontSize = 12.sp)
                    Text(text = "Gió 12 km/h", color = Color(0xCCFFFFFF), fontSize = 12.sp)
                }

                // Vertical Divider
                Box(
                    modifier = Modifier
                        .width(1.dp)
                        .height(100.dp)
                        .background(Color(0x33FFFFFF))
                )

                // Column City B
                Column(
                    modifier = Modifier.weight(1f),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = cityB.cityName,
                        color = Color.White,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    WeatherIcon(condition = WeatherCondition.LIGHT_RAIN, size = 32.dp)
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(text = "28°C", color = Color.White, fontSize = 22.sp, fontWeight = FontWeight.Light)
                    Text(text = "Độ ẩm 85%", color = Color(0xCCFFFFFF), fontSize = 12.sp)
                    Text(text = "Gió 18 km/h", color = Color(0xCCFFFFFF), fontSize = 12.sp)
                }
            }
        }
    }
}

@Composable
private fun EmptyFavoritesCard(
    onExploreClick: () -> Unit,
    onAddDefault: (String) -> Unit
) {
    val popularDefaults = listOf("TP. Hồ Chí Minh", "Hà Nội", "Đà Nẵng", "Đà Lạt", "Nha Trang")

    GlassCard(
        modifier = Modifier.fillMaxWidth(),
        cornerRadius = 24.dp,
        backgroundColor = Color(0x331E293B),
        borderColor = Color(0x3394A3B8)
    ) {
        Column(
            modifier = Modifier.padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(60.dp)
                    .clip(CircleShape)
                    .background(Color(0x22F43F5E)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.Favorite,
                    contentDescription = null,
                    tint = Color(0xFFF43F5E),
                    modifier = Modifier.size(32.dp)
                )
            }

            Text(
                text = "Chưa có địa điểm yêu thích",
                color = Color.White,
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold
            )

            Text(
                text = "Lưu lại các thành phố bạn quan tâm để theo dõi thời tiết nhanh chóng mọi lúc.",
                color = Color(0xCCFFFFFF),
                fontSize = 14.sp,
                lineHeight = 20.sp
            )

            Button(
                onClick = onExploreClick,
                shape = RoundedCornerShape(16.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF0284C7)),
                modifier = Modifier.fillMaxWidth()
            ) {
                Icon(Icons.Default.Search, contentDescription = null, modifier = Modifier.size(18.dp))
                Spacer(modifier = Modifier.width(8.dp))
                Text("Tìm kiếm thành phố ngay", fontWeight = FontWeight.SemiBold)
            }

            Spacer(modifier = Modifier.height(4.dp))

            Text(
                text = "Hoặc thêm nhanh các thành phố lớn:",
                color = Color(0x9994A3B8),
                fontSize = 12.sp
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceAround
            ) {
                popularDefaults.take(3).forEach { city ->
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(12.dp))
                            .background(Color(0x260F172A))
                            .border(1.dp, Color(0x3338BDF8), RoundedCornerShape(12.dp))
                            .clickable { onAddDefault(city) }
                            .padding(horizontal = 10.dp, vertical = 6.dp)
                    ) {
                        Text(
                            text = "+ $city",
                            color = Color(0xFF38BDF8),
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Medium
                        )
                    }
                }
            }
        }
    }
}

