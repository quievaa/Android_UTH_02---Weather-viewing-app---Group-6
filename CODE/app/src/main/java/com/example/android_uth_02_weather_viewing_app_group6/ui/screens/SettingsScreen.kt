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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Air
import androidx.compose.material.icons.outlined.Groups
import androidx.compose.material.icons.outlined.Info
import androidx.compose.material.icons.outlined.Language
import androidx.compose.material.icons.outlined.Notifications
import androidx.compose.material.icons.outlined.Thermostat
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Icon
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.android_uth_02_weather_viewing_app_group6.ui.components.GlassCard
import com.example.android_uth_02_weather_viewing_app_group6.ui.components.Weather3DBackground
import com.example.android_uth_02_weather_viewing_app_group6.ui.model.WeatherCondition
import com.example.android_uth_02_weather_viewing_app_group6.ui.viewmodel.WeatherViewModel

@Composable
fun SettingsScreen(
    contentPadding: PaddingValues,
    viewModel: WeatherViewModel
) {
    val isCelsius by viewModel.isCelsius.collectAsState()
    val windUnit by viewModel.windUnit.collectAsState()
    val roadAlerts by viewModel.roadAlertsEnabled.collectAsState()
    val vehicle by viewModel.selectedVehicle.collectAsState()
    var showWindDialog by remember { mutableStateOf(false) }

    Box(
        modifier = Modifier.fillMaxSize()
    ) {
        // Dynamic 3D Deep Space Starry Atmosphere
        Weather3DBackground(
            condition = WeatherCondition.NIGHT_CLEAR,
            isNight = true,
            windSpeedMps = 2.0,
            temperatureC = 25.0
        )
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(
                start = 16.dp,
                end = 16.dp,
                top = contentPadding.calculateTopPadding() + 8.dp,
                bottom = contentPadding.calculateBottomPadding() + 90.dp
            ),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            item {
                Text(
                    text = "Cài đặt ứng dụng",
                    color = Color.White,
                    fontSize = 28.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(horizontal = 4.dp, vertical = 6.dp)
                )
            }

            // Weather Units Section
            item {
                SettingsSectionHeader("Đơn vị thời tiết")
            }

            item {
                GlassCard(
                    modifier = Modifier.fillMaxWidth(),
                    cornerRadius = 20.dp,
                    backgroundColor = Color(0x331E293B),
                    borderColor = Color(0x3394A3B8)
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        SettingsToggleRow(
                            icon = Icons.Outlined.Thermostat,
                            title = "Đơn vị nhiệt độ",
                            subtitle = if (isCelsius) "Đang sử dụng độ C (°C)" else "Đang sử dụng độ F (°F)",
                            isChecked = isCelsius,
                            onToggle = { viewModel.toggleTemperatureUnit() },
                            testTag = "toggle_temp_unit"
                        )

                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(12.dp))
                                .clickable { showWindDialog = true }
                                .padding(vertical = 4.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = Icons.Outlined.Air,
                                    contentDescription = null,
                                    tint = Color(0xFF38BDF8),
                                    modifier = Modifier.size(22.dp)
                                )
                                Spacer(modifier = Modifier.width(12.dp))
                                Column {
                                    Text(
                                        text = "Đơn vị tốc độ gió",
                                        color = Color.White,
                                        fontSize = 15.sp,
                                        fontWeight = FontWeight.Medium
                                    )
                                    Text(
                                        text = "Đang chọn: $windUnit",
                                        color = Color(0x9994A3B8),
                                        fontSize = 12.sp
                                    )
                                }
                            }

                            Text(
                                text = "Thay đổi ›",
                                color = Color(0xFF38BDF8),
                                fontSize = 14.sp,
                                fontWeight = FontWeight.SemiBold
                            )
                        }
                    }
                }
            }

            // Road Weather & Navigation Section
            item {
                SettingsSectionHeader("Lộ trình & Cảnh báo thời tiết")
            }

            item {
                GlassCard(
                    modifier = Modifier.fillMaxWidth(),
                    cornerRadius = 20.dp,
                    backgroundColor = Color(0x331E293B),
                    borderColor = Color(0x3394A3B8)
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        SettingsToggleRow(
                            icon = Icons.Outlined.Notifications,
                            title = "Cảnh báo mưa đèo dốc nguy hiểm",
                            subtitle = "Tự động cảnh báo trước 30 phút tại các đèo như Bảo Lộc, Hải Vân, Ô Quy Hồ",
                            isChecked = roadAlerts,
                            onToggle = { viewModel.toggleRoadAlerts() },
                            testTag = "toggle_road_alerts"
                        )

                        Spacer(modifier = Modifier.height(2.dp))

                        // Vehicle Selection
                        Text(
                            text = "Phương tiện mặc định",
                            color = Color.White,
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Medium
                        )

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            listOf("Ô tô", "Xe máy", "Xe khách").forEach { v ->
                                val isSelected = vehicle == v
                                Box(
                                    modifier = Modifier
                                        .weight(1f)
                                        .clip(RoundedCornerShape(12.dp))
                                        .background(if (isSelected) Color(0xFF0284C7) else Color(0x260F172A))
                                        .border(
                                            1.dp,
                                            if (isSelected) Color(0xFF38BDF8) else Color(0x1AFFFFFF),
                                            RoundedCornerShape(12.dp)
                                        )
                                        .clickable { viewModel.selectVehicle(v) }
                                        .padding(vertical = 10.dp),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        text = v,
                                        color = Color.White,
                                        fontSize = 13.sp,
                                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                                    )
                                }
                            }
                        }
                    }
                }
            }

            // General & About Section
            item {
                SettingsSectionHeader("Thông tin ứng dụng")
            }

            item {
                GlassCard(
                    modifier = Modifier.fillMaxWidth(),
                    cornerRadius = 20.dp,
                    backgroundColor = Color(0x331E293B),
                    borderColor = Color(0x3394A3B8)
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(14.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = Icons.Outlined.Language,
                                    contentDescription = null,
                                    tint = Color(0xFF38BDF8),
                                    modifier = Modifier.size(20.dp)
                                )
                                Spacer(modifier = Modifier.width(10.dp))
                                Text(
                                    text = "Ngôn ngữ",
                                    color = Color.White,
                                    fontSize = 15.sp
                                )
                            }
                            Text(
                                text = "Tiếng Việt (VI)",
                                color = Color(0x9994A3B8),
                                fontSize = 14.sp
                            )
                        }

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = Icons.Outlined.Groups,
                                    contentDescription = null,
                                    tint = Color(0xFF38BDF8),
                                    modifier = Modifier.size(20.dp)
                                )
                                Spacer(modifier = Modifier.width(10.dp))
                                Text(
                                    text = "Nhóm phát triển",
                                    color = Color.White,
                                    fontSize = 15.sp
                                )
                            }
                            Text(
                                text = "Group 6 - UTH",
                                color = Color(0x9994A3B8),
                                fontSize = 14.sp
                            )
                        }

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = Icons.Outlined.Info,
                                    contentDescription = null,
                                    tint = Color(0xFF38BDF8),
                                    modifier = Modifier.size(20.dp)
                                )
                                Spacer(modifier = Modifier.width(10.dp))
                                Text(
                                    text = "Phiên bản",
                                    color = Color.White,
                                    fontSize = 15.sp
                                )
                            }
                            Text(
                                text = "v1.0.0 Pro",
                                color = Color(0x9994A3B8),
                                fontSize = 14.sp
                            )
                        }
                    }
                }
            }
        }
    }

    if (showWindDialog) {
        AlertDialog(
            onDismissRequest = { showWindDialog = false },
            title = { Text("Chọn đơn vị tốc độ gió", fontWeight = FontWeight.Bold) },
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
                                .padding(vertical = 10.dp),
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
private fun SettingsSectionHeader(title: String) {
    Text(
        text = title,
        color = Color(0xFF38BDF8),
        fontSize = 13.sp,
        fontWeight = FontWeight.Bold,
        letterSpacing = 0.5.sp,
        modifier = Modifier.padding(start = 4.dp)
    )
}

@Composable
private fun SettingsToggleRow(
    icon: ImageVector,
    title: String,
    subtitle: String,
    isChecked: Boolean,
    onToggle: () -> Unit,
    testTag: String
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row(
            modifier = Modifier.weight(1f),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = Color(0xFF38BDF8),
                modifier = Modifier.size(22.dp)
            )
            Spacer(modifier = Modifier.width(12.dp))
            Column {
                Text(
                    text = title,
                    color = Color.White,
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Medium
                )
                Text(
                    text = subtitle,
                    color = Color(0x9994A3B8),
                    fontSize = 12.sp,
                    lineHeight = 16.sp
                )
            }
        }

        Switch(
            checked = isChecked,
            onCheckedChange = { onToggle() },
            colors = SwitchDefaults.colors(
                checkedThumbColor = Color.White,
                checkedTrackColor = Color(0xFF0284C7),
                uncheckedThumbColor = Color(0xFF94A3B8),
                uncheckedTrackColor = Color(0x3364748B)
            ),
            modifier = Modifier.testTag(testTag)
        )
    }
}
