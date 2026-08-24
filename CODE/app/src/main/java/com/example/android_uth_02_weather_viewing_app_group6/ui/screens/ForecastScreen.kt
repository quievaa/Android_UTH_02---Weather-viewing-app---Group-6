package com.example.android_uth_02_weather_viewing_app_group6.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Cloud
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.android_uth_02_weather_viewing_app_group6.ui.components.SectionTitle
import com.example.android_uth_02_weather_viewing_app_group6.ui.model.ForecastItem
import com.example.android_uth_02_weather_viewing_app_group6.ui.viewmodel.WeatherViewModel

@Composable
fun ForecastScreen(
    contentPadding: PaddingValues,
    viewModel: WeatherViewModel
) {
    // Sử dụng collectAsState để quan sát thay đổi đơn vị
    val isCelsius by viewModel.isCelsius.collectAsState()
    
    val forecast = listOf(
        ForecastItem("Thứ Hai", "Nhiều mây", if (isCelsius) "27°C / 33°C" else "81°F / 91°F"),
        ForecastItem("Thứ Ba", "Mưa nhỏ", if (isCelsius) "26°C / 31°C" else "79°F / 88°F"),
        ForecastItem("Thứ Tư", "Nắng", if (isCelsius) "28°C / 34°C" else "82°F / 93°F"),
        ForecastItem("Thứ Năm", "Mưa dông", if (isCelsius) "25°C / 30°C" else "77°F / 86°F"),
        ForecastItem("Thứ Sáu", "Mây rải rác", if (isCelsius) "27°C / 32°C" else "81°F / 90°F"),
    )

    LazyColumn(
        contentPadding = contentPadding,
        verticalArrangement = Arrangement.spacedBy(12.dp),
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp),
    ) {
        item {
            Spacer(modifier = Modifier.height(4.dp))
            SectionTitle("5-day forecast")
        }
        items(forecast) { item ->
            ElevatedCard(shape = RoundedCornerShape(8.dp)) {
                Row(
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.Cloud, contentDescription = null)
                        Spacer(modifier = Modifier.width(12.dp))
                        Column {
                            Text(item.day, fontWeight = FontWeight.SemiBold)
                            Text(item.condition, style = MaterialTheme.typography.bodyMedium)
                        }
                    }
                    Text(item.temperature, fontWeight = FontWeight.Medium)
                }
            }
        }
    }
}
