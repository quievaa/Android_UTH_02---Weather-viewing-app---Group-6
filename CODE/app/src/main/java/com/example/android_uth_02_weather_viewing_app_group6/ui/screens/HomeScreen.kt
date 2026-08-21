package com.example.android_uth_02_weather_viewing_app_group6.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Air
import androidx.compose.material.icons.filled.ArrowDownward
import androidx.compose.material.icons.filled.ArrowUpward
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.Cloud
import androidx.compose.material.icons.filled.DeviceThermostat
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Navigation
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Speed
import androidx.compose.material.icons.filled.WaterDrop
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.android_uth_02_weather_viewing_app_group6.domain.model.CurrentWeather
import com.example.android_uth_02_weather_viewing_app_group6.ui.components.HomeSearchBar
import com.example.android_uth_02_weather_viewing_app_group6.ui.components.SectionTitle
import com.example.android_uth_02_weather_viewing_app_group6.ui.components.WeatherDetailCard
import com.example.android_uth_02_weather_viewing_app_group6.ui.components.getWeatherIcon
import com.example.android_uth_02_weather_viewing_app_group6.ui.viewmodel.WeatherUiState
import com.example.android_uth_02_weather_viewing_app_group6.ui.viewmodel.WeatherViewModel
import java.util.Locale

@Composable
fun HomeScreen(
    contentPadding: PaddingValues,
    onForecastClick: () -> Unit,
    onSearchClick: () -> Unit = {},
    viewModel: WeatherViewModel,
) {
    val uiState by viewModel.uiState.collectAsState()

    HomeScreenContent(
        contentPadding = contentPadding,
        uiState = uiState,
        onForecastClick = onForecastClick,
        onSearchClick = onSearchClick,
        onRetry = viewModel::retry,
    )
}

@Composable
fun HomeScreenContent(
    contentPadding: PaddingValues,
    uiState: WeatherUiState,
    onForecastClick: () -> Unit,
    onSearchClick: () -> Unit,
    onRetry: () -> Unit,
) {
    BoxWithConstraints(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp),
    ) {
        val isExpandedLayout = maxWidth >= 600.dp

        LazyColumn(
            contentPadding = contentPadding,
            verticalArrangement = Arrangement.spacedBy(16.dp),
            modifier = Modifier.fillMaxSize(),
        ) {
            // 1. Thanh Search Bar ở đầu trang
            item {
                Spacer(modifier = Modifier.height(4.dp))
                HomeSearchBar(
                    hint = "Tìm kiếm thành phố, địa điểm...",
                    onSearchClick = onSearchClick,
                )
            }

            // 2. Nội dung chính theo trạng thái UI
            when (uiState) {
                is WeatherUiState.Loading -> {
                    item { LoadingWeatherCard() }
                }
                is WeatherUiState.Error -> {
                    item {
                        ErrorWeatherCard(
                            message = uiState.message,
                            onRetry = onRetry,
                        )
                    }
                }
                is WeatherUiState.Success -> {
                    val weather = uiState.weather
                    if (isExpandedLayout) {
                        // Responsive Layout cho màn hình ngang / Tablet: 2 cột
                        item {
                            Row(
                                horizontalArrangement = Arrangement.spacedBy(16.dp),
                                modifier = Modifier.fillMaxWidth(),
                            ) {
                                // Cột trái: Hero Card
                                Box(modifier = Modifier.weight(1f)) {
                                    HeroWeatherCard(
                                        weather = weather,
                                        onForecastClick = onForecastClick,
                                    )
                                }
                                // Cột phải: Detailed Cards
                                Column(
                                    modifier = Modifier.weight(1f),
                                    verticalArrangement = Arrangement.spacedBy(12.dp),
                                ) {
                                    WeatherMetricsGrid(weather = weather, isWide = true)
                                }
                            }
                        }
                    } else {
                        // Layout cho màn hình dọc tiêu chuẩn
                        item {
                            HeroWeatherCard(
                                weather = weather,
                                onForecastClick = onForecastClick,
                            )
                        }

                        item {
                            SectionTitle("Chỉ số thời tiết chi tiết")
                            WeatherMetricsGrid(weather = weather, isWide = false)
                        }

                        // Card thông tin vị trí tọa độ
                        item {
                            SectionTitle("Thông tin địa lý")
                            CoordinatesCard(weather = weather)
                            Spacer(modifier = Modifier.height(16.dp))
                        }
                    }
                }
            }
        }
    }
}

/**
 * Hero Card hiển thị thời tiết hiện tại nổi bật
 */
@Composable
fun HeroWeatherCard(
    weather: CurrentWeather,
    onForecastClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    ElevatedCard(
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.elevatedCardColors(
            containerColor = MaterialTheme.colorScheme.primaryContainer,
        ),
        elevation = CardDefaults.elevatedCardElevation(defaultElevation = 4.dp),
        modifier = modifier.fillMaxWidth(),
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(
                    Brush.linearGradient(
                        colors = listOf(
                            MaterialTheme.colorScheme.primaryContainer,
                            MaterialTheme.colorScheme.surfaceVariant,
                        ),
                    ),
                ),
        ) {
            Column(
                modifier = Modifier.padding(20.dp),
                verticalArrangement = Arrangement.spacedBy(14.dp),
            ) {
                // Header thành phố & icon vị trí
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween,
                    modifier = Modifier.fillMaxWidth(),
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.weight(1f),
                    ) {
                        Icon(
                            imageVector = Icons.Default.LocationOn,
                            contentDescription = "Location",
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(24.dp),
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = weather.cityName,
                            style = MaterialTheme.typography.headlineSmall,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onPrimaryContainer,
                        )
                    }

                    Surface(
                        shape = CircleShape,
                        color = MaterialTheme.colorScheme.primary.copy(alpha = 0.12f),
                        modifier = Modifier.size(54.dp),
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Icon(
                                imageVector = getWeatherIcon(weather.description),
                                contentDescription = weather.description,
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(32.dp),
                            )
                        }
                    }
                }

                // Mô tả thời tiết
                Text(
                    text = weather.description.replaceFirstChar {
                        if (it.isLowerCase()) it.titlecase(Locale.getDefault()) else it.toString()
                    },
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.8f),
                )

                // Nhiệt độ lớn và cảm giác như
                Row(
                    verticalAlignment = Alignment.Bottom,
                    horizontalArrangement = Arrangement.SpaceBetween,
                    modifier = Modifier.fillMaxWidth(),
                ) {
                    Text(
                        text = "${weather.temperatureC.toInt()}°C",
                        style = MaterialTheme.typography.displayMedium.copy(
                            fontSize = 48.sp,
                            fontWeight = FontWeight.ExtraBold,
                        ),
                        color = MaterialTheme.colorScheme.onPrimaryContainer,
                    )

                    Column(horizontalAlignment = Alignment.End) {
                        Text(
                            text = "Cảm giác như ${weather.feelsLikeC.toInt()}°C",
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.Medium,
                            color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.85f),
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.ArrowUpward,
                                contentDescription = "Max temp",
                                tint = Color(0xFFE53935),
                                modifier = Modifier.size(14.dp),
                            )
                            Text(
                                text = "${weather.maxTemperatureC.toInt()}°",
                                style = MaterialTheme.typography.bodySmall,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onPrimaryContainer,
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Icon(
                                imageVector = Icons.Default.ArrowDownward,
                                contentDescription = "Min temp",
                                tint = Color(0xFF1E88E5),
                                modifier = Modifier.size(14.dp),
                            )
                            Text(
                                text = "${weather.minTemperatureC.toInt()}°",
                                style = MaterialTheme.typography.bodySmall,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onPrimaryContainer,
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(4.dp))

                // Nút chuyển sang màn hình dự báo thời tiết 5 ngày
                Button(
                    onClick = onForecastClick,
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.primary,
                    ),
                    modifier = Modifier.fillMaxWidth(),
                ) {
                    Icon(
                        imageVector = Icons.Default.CalendarMonth,
                        contentDescription = null,
                        modifier = Modifier.size(18.dp),
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Xem dự báo 5 ngày tới",
                        fontWeight = FontWeight.SemiBold,
                    )
                }
            }
        }
    }
}

/**
 * Lưới hiển thị các Card chỉ số thời tiết chi tiết: Nhiệt độ, Độ ẩm, Áp suất, Gió
 */
@Composable
fun WeatherMetricsGrid(
    weather: CurrentWeather,
    isWide: Boolean,
    modifier: Modifier = Modifier,
) {
    Column(
        verticalArrangement = Arrangement.spacedBy(12.dp),
        modifier = modifier.fillMaxWidth(),
    ) {
        // Hàng 1: Nhiệt độ & Độ ẩm
        Row(
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            modifier = Modifier.fillMaxWidth(),
        ) {
            WeatherDetailCard(
                title = "NHIỆT ĐỘ",
                value = "${weather.temperatureC.toInt()}°C",
                subtitle = "Thấp ${weather.minTemperatureC.toInt()}° • Cao ${weather.maxTemperatureC.toInt()}°",
                icon = Icons.Default.DeviceThermostat,
                iconColor = Color(0xFFFF7043),
                modifier = Modifier.weight(1f),
            )
            WeatherDetailCard(
                title = "ĐỘ ẨM",
                value = "${weather.humidityPercent}%",
                subtitle = if (weather.humidityPercent > 70) "Độ ẩm cao" else "Dễ chịu",
                icon = Icons.Default.WaterDrop,
                iconColor = Color(0xFF29B6F6),
                modifier = Modifier.weight(1f),
            )
        }

        // Hàng 2: Tốc độ gió & Áp suất
        Row(
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            modifier = Modifier.fillMaxWidth(),
        ) {
            WeatherDetailCard(
                title = "TỐC ĐỘ GIÓ",
                value = "${String.format(Locale.US, "%.1f", weather.windSpeedMps)} m/s",
                subtitle = weather.windDirectionDeg?.let { "Hướng $it°" } ?: "Gió nhẹ",
                icon = Icons.Default.Air,
                iconColor = Color(0xFF26A69A),
                modifier = Modifier.weight(1f),
            )
            WeatherDetailCard(
                title = "ÁP SUẤT",
                value = "${weather.pressureHpa.toInt()} hPa",
                subtitle = "Áp suất khí quyển",
                icon = Icons.Default.Speed,
                iconColor = Color(0xFFAB47BC),
                modifier = Modifier.weight(1f),
            )
        }
    }
}

/**
 * Card hiển thị vị trí và tọa độ địa lý
 */
@Composable
fun CoordinatesCard(
    weather: CurrentWeather,
    modifier: Modifier = Modifier,
) {
    ElevatedCard(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.elevatedCardColors(
            containerColor = MaterialTheme.colorScheme.surfaceContainerHigh,
        ),
        modifier = modifier.fillMaxWidth(),
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween,
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.weight(1f),
            ) {
                Surface(
                    shape = CircleShape,
                    color = MaterialTheme.colorScheme.primary.copy(alpha = 0.12f),
                    modifier = Modifier.size(36.dp),
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(
                            imageVector = Icons.Default.Navigation,
                            contentDescription = "Coordinates",
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(20.dp),
                        )
                    }
                }
                Spacer(modifier = Modifier.width(12.dp))
                Column {
                    Text(
                        text = "Tọa độ địa lý",
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                    Text(
                        text = "Vĩ độ: ${weather.latitude?.let { String.format(Locale.US, "%.3f", it) } ?: "--"} | Kinh độ: ${weather.longitude?.let { String.format(Locale.US, "%.3f", it) } ?: "--"}",
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.onSurface,
                    )
                }
            }
        }
    }
}

@Composable
private fun LoadingWeatherCard() {
    ElevatedCard(
        shape = RoundedCornerShape(20.dp),
        modifier = Modifier.fillMaxWidth(),
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp),
            modifier = Modifier
                .fillMaxWidth()
                .padding(40.dp),
        ) {
            CircularProgressIndicator(
                color = MaterialTheme.colorScheme.primary,
                strokeWidth = 3.5.dp,
            )
            Text(
                text = "Đang tải dữ liệu thời tiết mới nhất...",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
    }
}

@Composable
private fun ErrorWeatherCard(
    message: String,
    onRetry: () -> Unit,
) {
    ElevatedCard(
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.elevatedCardColors(
            containerColor = MaterialTheme.colorScheme.errorContainer,
        ),
        modifier = Modifier.fillMaxWidth(),
    ) {
        Column(
            verticalArrangement = Arrangement.spacedBy(12.dp),
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp),
        ) {
            Text(
                text = "Không thể tải dữ liệu thời tiết",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onErrorContainer,
            )
            Text(
                text = message,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onErrorContainer.copy(alpha = 0.9f),
            )
            FilledTonalButton(
                onClick = onRetry,
                shape = RoundedCornerShape(10.dp),
            ) {
                Icon(Icons.Default.Refresh, contentDescription = null, modifier = Modifier.size(16.dp))
                Spacer(modifier = Modifier.width(6.dp))
                Text("Thử lại")
            }
        }
    }
}
