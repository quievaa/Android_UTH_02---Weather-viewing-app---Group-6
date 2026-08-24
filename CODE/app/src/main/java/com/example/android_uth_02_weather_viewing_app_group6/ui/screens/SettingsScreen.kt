package com.example.android_uth_02_weather_viewing_app_group6.ui.screens

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.android_uth_02_weather_viewing_app_group6.ui.components.SectionTitle
import com.example.android_uth_02_weather_viewing_app_group6.ui.components.SettingRow
import com.example.android_uth_02_weather_viewing_app_group6.ui.viewmodel.WeatherViewModel

@Composable
fun SettingsScreen(
    contentPadding: PaddingValues,
    viewModel: WeatherViewModel
) {
    val isCelsius by viewModel.isCelsius.collectAsState()
    val windUnit by viewModel.windUnit.collectAsState()
    var showWindDialog by remember { mutableStateOf(false) }

    LazyColumn(
        contentPadding = contentPadding,
        verticalArrangement = Arrangement.spacedBy(12.dp),
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp),
    ) {
        item {
            Spacer(modifier = Modifier.height(8.dp))
            SectionTitle("Đơn vị đo lường")
        }
        
        item {
            SettingRow(
                icon = Icons.Default.Thermostat,
                title = "Đơn vị nhiệt độ",
                subtitle = if (isCelsius) "Đang sử dụng độ C (°C)" else "Đang sử dụng độ F (°F)",
                checked = isCelsius,
                onCheckedChange = { viewModel.toggleTemperatureUnit() },
            )
        }

        item {
            SettingClickRow(
                icon = Icons.Default.Air,
                title = "Tốc độ gió",
                subtitle = "Đơn vị: $windUnit",
                onClick = { showWindDialog = true }
            )
        }

        item {
            HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))
            SectionTitle("Thông tin ứng dụng")
        }

        item {
            SettingClickRow(
                icon = Icons.Default.Info,
                title = "Phiên bản ứng dụng",
                subtitle = "1.0.0",
                onClick = {}
            )
        }

        item {
            SettingClickRow(
                icon = Icons.Default.Groups,
                title = "Nhóm phát triển",
                subtitle = "Group 6 - UTH",
                onClick = {}
            )
        }
    }

    if (showWindDialog) {
        AlertDialog(
            onDismissRequest = { showWindDialog = false },
            title = { Text("Chọn đơn vị tốc độ gió") },
            text = {
                Column {
                    listOf("m/s", "km/h").forEach { unit ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable {
                                    viewModel.updateWindUnit(unit)
                                    showWindDialog = false
                                }
                                .padding(vertical = 12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            RadioButton(
                                selected = (windUnit == unit),
                                onClick = {
                                    viewModel.updateWindUnit(unit)
                                    showWindDialog = false
                                }
                            )
                            Text(text = unit, modifier = Modifier.padding(start = 8.dp))
                        }
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = { showWindDialog = false }) {
                    Text("Đóng")
                }
            }
        )
    }
}

@Composable
fun SettingClickRow(
    icon: ImageVector,
    title: String,
    subtitle: String,
    onClick: () -> Unit
) {
    ElevatedCard(
        shape = RoundedCornerShape(12.dp),
        onClick = onClick,
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.weight(1f),
            ) {
                Icon(icon, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                Spacer(modifier = Modifier.width(12.dp))
                Column {
                    Text(title, fontWeight = FontWeight.SemiBold, style = MaterialTheme.typography.bodyLarge)
                    Text(subtitle, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }
            Icon(
                imageVector = Icons.Default.ChevronRight,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}
