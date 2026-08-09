package com.example.android_uth_02_weather_viewing_app_group6.ui.screens

import androidx.compose.foundation.layout.Arrangement
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
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Cloud
import androidx.compose.material3.Button
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.android_uth_02_weather_viewing_app_group6.ui.components.SectionTitle
import com.example.android_uth_02_weather_viewing_app_group6.ui.components.WeatherMetric
import com.example.android_uth_02_weather_viewing_app_group6.ui.viewmodel.WeatherUiState
import com.example.android_uth_02_weather_viewing_app_group6.ui.viewmodel.WeatherViewModel
import java.util.Locale

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun HomeScreen(
    contentPadding: PaddingValues,
    onForecastClick: () -> Unit,
    viewModel: WeatherViewModel,
) {
    val uiState by viewModel.uiState.collectAsState()

    LazyColumn(
        contentPadding = contentPadding,
        verticalArrangement = Arrangement.spacedBy(16.dp),
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp),
    ) {
        item {
            Spacer(modifier = Modifier.height(4.dp))
            when (val state = uiState) {
                WeatherUiState.Loading -> LoadingWeatherCard()
                is WeatherUiState.Error -> ErrorWeatherCard(
                    message = state.message,
                    onRetry = viewModel::retry,
                )
                is WeatherUiState.Success -> {
                    val weather = state.weather
                    ElevatedCard(
                        colors = CardDefaults.elevatedCardColors(
                            containerColor = MaterialTheme.colorScheme.primaryContainer,
                        ),
                    ) {
                        Column(
                            verticalArrangement = Arrangement.spacedBy(16.dp),
                            modifier = Modifier.padding(20.dp),
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween,
                                modifier = Modifier.fillMaxWidth(),
                            ) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = weather.cityName,
                                        style = MaterialTheme.typography.titleLarge,
                                        fontWeight = FontWeight.SemiBold,
                                    )
                                    Text(
                                        text = weather.description.replaceFirstChar {
                                            if (it.isLowerCase()) it.titlecase(Locale.getDefault()) else it.toString()
                                        },
                                        style = MaterialTheme.typography.bodyLarge,
                                    )
                                }
                                Icon(
                                    imageVector = Icons.Default.Cloud,
                                    contentDescription = weather.description,
                                    tint = MaterialTheme.colorScheme.primary,
                                    modifier = Modifier.size(54.dp),
                                )
                            }

                            Text(
                                text = "${weather.temperatureC.toInt()} °C",
                                style = MaterialTheme.typography.displayMedium,
                                fontWeight = FontWeight.Bold,
                            )

                            Text(
                                text = "Cảm giác như ${weather.feelsLikeC.toInt()} °C",
                                style = MaterialTheme.typography.bodyLarge,
                            )

                            Button(onClick = onForecastClick) {
                                Icon(Icons.Default.Cloud, contentDescription = null)
                                Spacer(modifier = Modifier.width(8.dp))
                                Text("Xem dự báo 5 ngày")
                            }
                        }
                    }
                }
            }
        }

        if (uiState is WeatherUiState.Success) {
            val weather = (uiState as WeatherUiState.Success).weather
            item {
                FlowRow(
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                    modifier = Modifier.fillMaxWidth(),
                ) {
                    WeatherMetric("Độ ẩm", "${weather.humidityPercent}%")
                    WeatherMetric("Gió", "${String.format(Locale.US, "%.1f", weather.windSpeedMps)} m/s")
                    WeatherMetric("Áp suất", "${String.format(Locale.US, "%.0f", weather.pressureHpa)} hPa")
                    WeatherMetric("Cảm giác", "${weather.feelsLikeC.toInt()} °C")
                    WeatherMetric(
                        "Nhiệt độ thấp",
                        "${weather.minTemperatureC.toInt()} °C",
                    )
                    WeatherMetric(
                        "Nhiệt độ cao",
                        "${weather.maxTemperatureC.toInt()} °C",
                    )
                }
            }

            item {
                SectionTitle("Thông tin vị trí")
                ElevatedCard {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text(
                            "Tọa độ: ${weather.latitude?.let { String.format(Locale.US, "%.4f", it) } ?: "--"}, " +
                                "${weather.longitude?.let { String.format(Locale.US, "%.4f", it) } ?: "--"}"
                        )
                        weather.windDirectionDeg?.let {
                            Text("Hướng gió: $it°")
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun LoadingWeatherCard() {
    ElevatedCard {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(12.dp),
            modifier = Modifier
                .fillMaxWidth()
                .padding(32.dp),
        ) {
            CircularProgressIndicator()
            Text("Đang tải dữ liệu thời tiết...")
        }
    }
}

@Composable
private fun ErrorWeatherCard(
    message: String,
    onRetry: () -> Unit,
) {
    ElevatedCard {
        Column(
            verticalArrangement = Arrangement.spacedBy(10.dp),
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp),
        ) {
            Text(
                "Không thể tải thời tiết",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
            )
            Text(message)
            TextButton(onClick = onRetry) {
                Text("Thử lại")
            }
        }
    }
}
