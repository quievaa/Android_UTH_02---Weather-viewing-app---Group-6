package com.example.android_uth_02_weather_viewing_app_group6.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Air
import androidx.compose.material.icons.outlined.CloudSync
import androidx.compose.material.icons.outlined.Dns
import androidx.compose.material.icons.outlined.Groups
import androidx.compose.material.icons.outlined.Hub
import androidx.compose.material.icons.outlined.Info
import androidx.compose.material.icons.outlined.Key
import androidx.compose.material.icons.outlined.Language
import androidx.compose.material.icons.outlined.Notifications
import androidx.compose.material.icons.outlined.Refresh
import androidx.compose.material.icons.outlined.Speed
import androidx.compose.material.icons.outlined.Thermostat
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.android_uth_02_weather_viewing_app_group6.data.remote.model.ApiHealthStatus
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

    // Multi-API States
    val isMultiApiEnabled by viewModel.isMultiApiEnabled.collectAsState()
    val primaryApiProvider by viewModel.primaryApiProvider.collectAsState()
    val weatherApiKey by viewModel.weatherApiKey.collectAsState()
    val openWeatherKey by viewModel.openWeatherKey.collectAsState()
    val apiHealthList by viewModel.apiHealthList.collectAsState()
    val isCheckingApis by viewModel.isCheckingApis.collectAsState()

    // Custom Endpoint States (No-Code API Changing)
    val customEndpointUrl by viewModel.customEndpointUrl.collectAsState()
    val customEndpointName by viewModel.customEndpointName.collectAsState()
    val customEndpointEnabled by viewModel.customEndpointEnabled.collectAsState()
    val customEndpointKey by viewModel.customEndpointKey.collectAsState()
    val customEndpointFormat by viewModel.customEndpointFormat.collectAsState()
    val customEndpointTestResult by viewModel.customEndpointTestResult.collectAsState()
    val isTestingCustomEndpoint by viewModel.isTestingCustomEndpoint.collectAsState()

    var showWindDialog by remember { mutableStateOf(false) }
    var showApiKeyDialog by remember { mutableStateOf(false) }
    var showCustomEndpointDialog by remember { mutableStateOf(false) }

    // Initial ping health check on screen load if empty
    LaunchedEffect(Unit) {
        if (apiHealthList.isEmpty()) {
            viewModel.testAllApisHealth()
        }
    }

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

            // ==========================================
            // 1. CẤU HÌNH MULTI-API & HỆ THỐNG DỰ PHÒNG
            // ==========================================
            item {
                SettingsSectionHeader("Cấu hình API & Hệ thống Dự phòng (Multi-API)")
            }

            item {
                GlassCard(
                    modifier = Modifier.fillMaxWidth(),
                    cornerRadius = 20.dp,
                    backgroundColor = Color(0x331E293B),
                    borderColor = Color(0x3338BDF8)
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        SettingsToggleRow(
                            icon = Icons.Outlined.CloudSync,
                            title = "Dự phòng Đa API song song (Failover)",
                            subtitle = if (isMultiApiEnabled)
                                "Đang bật: Kết hợp 3 API (Open-Meteo, WeatherAPI, OpenWeatherMap). Tự động chuyển đổi nếu 1 API bị lỗi/sập để tránh gián đoạn."
                            else
                                "Đang tắt: Chỉ sử dụng API chính đã chọn bên dưới.",
                            isChecked = isMultiApiEnabled,
                            onToggle = { viewModel.toggleMultiApi() },
                            testTag = "toggle_multi_api"
                        )

                        Spacer(modifier = Modifier.height(2.dp))

                        // Chọn API ưu tiên
                        Text(
                            text = "Nhà cung cấp API chính (Ưu tiên số 1)",
                            color = Color.White,
                            fontSize = 14.sp,
                            fontWeight = FontWeight.SemiBold
                        )

                        val providers = mutableListOf(
                            Triple("OPEN_METEO", "Open-Meteo", "Miễn phí 100%"),
                            Triple("WEATHER_API", "WeatherAPI", "Tốc độ cao"),
                            Triple("OPEN_WEATHER", "OpenWeather", "Quốc tế")
                        )

                        if (customEndpointEnabled && customEndpointUrl.isNotBlank()) {
                            providers.add(0, Triple("CUSTOM_ENDPOINT", customEndpointName.take(11), "Tùy chỉnh"))
                        }

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            providers.forEach { (key, name, tag) ->
                                val isSelected = primaryApiProvider == key
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
                                        .clickable { viewModel.setPrimaryApiProvider(key) }
                                        .padding(vertical = 10.dp, horizontal = 4.dp),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                        Text(
                                            text = name,
                                            color = Color.White,
                                            fontSize = 12.sp,
                                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium
                                        )
                                        Spacer(modifier = Modifier.height(2.dp))
                                        Text(
                                            text = tag,
                                            color = if (isSelected) Color(0xFFBAE6FD) else Color(0x9994A3B8),
                                            fontSize = 10.sp
                                        )
                                    }
                                }
                            }
                        }

                        // Thay đổi API Server Tùy chỉnh (No-Code Endpoint)
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(12.dp))
                                .clickable { showCustomEndpointDialog = true }
                                .background(if (customEndpointEnabled && customEndpointUrl.isNotBlank()) Color(0x260284C7) else Color(0x1A38BDF8))
                                .border(1.dp, if (customEndpointEnabled) Color(0xFF38BDF8) else Color(0x3394A3B8), RoundedCornerShape(12.dp))
                                .padding(12.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(
                                modifier = Modifier.weight(1f),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    imageVector = Icons.Outlined.Dns,
                                    contentDescription = null,
                                    tint = if (customEndpointEnabled) Color(0xFF38BDF8) else Color(0xFF94A3B8),
                                    modifier = Modifier.size(20.dp)
                                )
                                Spacer(modifier = Modifier.width(10.dp))
                                Column {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Text(
                                            text = "Thay đổi API Server khác (Không cần code)",
                                            color = Color.White,
                                            fontSize = 14.sp,
                                            fontWeight = FontWeight.Medium
                                        )
                                        if (customEndpointEnabled && customEndpointUrl.isNotBlank()) {
                                            Spacer(modifier = Modifier.width(6.dp))
                                            Box(
                                                modifier = Modifier
                                                    .clip(RoundedCornerShape(4.dp))
                                                    .background(Color(0xFF22C55E))
                                                    .padding(horizontal = 4.dp, vertical = 1.dp)
                                            ) {
                                                Text("Đang dùng", color = Color.White, fontSize = 9.sp, fontWeight = FontWeight.Bold)
                                            }
                                        }
                                    }
                                    Text(
                                        text = if (customEndpointUrl.isNotBlank())
                                            "URL: $customEndpointUrl"
                                        else
                                            "Nhập URL API / Proxy / Server riêng của bạn",
                                        color = Color(0x9994A3B8),
                                        fontSize = 11.sp,
                                        maxLines = 1
                                    )
                                }
                            }

                            Text(
                                text = "Đổi API ›",
                                color = Color(0xFF38BDF8),
                                fontSize = 13.sp,
                                fontWeight = FontWeight.SemiBold
                            )
                        }

                        // Quản lý API Key
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(12.dp))
                                .clickable { showApiKeyDialog = true }
                                .background(Color(0x1A38BDF8))
                                .padding(12.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(
                                modifier = Modifier.weight(1f),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    imageVector = Icons.Outlined.Key,
                                    contentDescription = null,
                                    tint = Color(0xFF38BDF8),
                                    modifier = Modifier.size(20.dp)
                                )
                                Spacer(modifier = Modifier.width(10.dp))
                                Column {
                                    Text(
                                        text = "Cấu hình API Key cá nhân",
                                        color = Color.White,
                                        fontSize = 14.sp,
                                        fontWeight = FontWeight.Medium
                                    )
                                    Text(
                                        text = if (weatherApiKey.isNotBlank() || openWeatherKey.isNotBlank())
                                            "Đã cấu hình API Key tuỳ chỉnh"
                                        else
                                            "Đang dùng API Key mặc định của hệ thống",
                                        color = Color(0x9994A3B8),
                                        fontSize = 11.sp
                                    )
                                }
                            }

                            Text(
                                text = "Thiết lập ›",
                                color = Color(0xFF38BDF8),
                                fontSize = 13.sp,
                                fontWeight = FontWeight.SemiBold
                            )
                        }
                    }
                }
            }

            // ==========================================
            // 2. BẢNG KIỂM TRA TRẠNG THÁI API (HEALTH CHECK)
            // ==========================================
            item {
                SettingsSectionHeader("Trạng thái Kết nối & Độ trễ các API")
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
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = Icons.Outlined.Hub,
                                    contentDescription = null,
                                    tint = Color(0xFF38BDF8),
                                    modifier = Modifier.size(20.dp)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = "Kiểm tra sức khỏe API (Ping Test)",
                                    color = Color.White,
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.SemiBold
                                )
                            }

                            val infiniteTransition = rememberInfiniteTransition(label = "spin")
                            val angle by infiniteTransition.animateFloat(
                                initialValue = 0f,
                                targetValue = 360f,
                                animationSpec = infiniteRepeatable(
                                    animation = tween(1000, easing = LinearEasing),
                                    repeatMode = RepeatMode.Restart
                                ),
                                label = "angle"
                            )

                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(10.dp))
                                    .background(Color(0x3338BDF8))
                                    .clickable(enabled = !isCheckingApis) {
                                        viewModel.testAllApisHealth()
                                    }
                                    .padding(horizontal = 10.dp, vertical = 6.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(
                                        imageVector = Icons.Outlined.Refresh,
                                        contentDescription = "Test",
                                        tint = Color(0xFF38BDF8),
                                        modifier = Modifier
                                            .size(16.dp)
                                            .rotate(if (isCheckingApis) angle else 0f)
                                    )
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text(
                                        text = if (isCheckingApis) "Đang đo..." else "Kiểm tra",
                                        color = Color(0xFF38BDF8),
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                            }
                        }

                        // Danh sách API
                        val defaultApis = if (apiHealthList.isNotEmpty()) {
                            apiHealthList
                        } else {
                            listOf(
                                ApiHealthStatus("Open-Meteo", true, 95, "Sẵn sàng hoạt động (Không cần Key)", primaryApiProvider == "OPEN_METEO"),
                                ApiHealthStatus("WeatherAPI.com", true, 130, "Sẵn sàng hoạt động (Key mặc định)", primaryApiProvider == "WEATHER_API"),
                                ApiHealthStatus("OpenWeatherMap", true, 180, "Sẵn sàng hoạt động (Key mặc định)", primaryApiProvider == "OPEN_WEATHER")
                            )
                        }

                        defaultApis.forEach { status ->
                            ApiHealthItem(status = status)
                        }
                    }
                }
            }

            // ==========================================
            // 3. ĐƠN VỊ THỜI TIẾT
            // ==========================================
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

            // ==========================================
            // 4. LỘ TRÌNH & CẢNH BÁO
            // ==========================================
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

            // ==========================================
            // 5. THÔNG TIN ỨNG DỤNG
            // ==========================================
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
                                text = "v1.2.0 Multi-API Pro",
                                color = Color(0x9994A3B8),
                                fontSize = 14.sp
                            )
                        }
                    }
                }
            }
        }
    }

    // ==========================================
    // DIALOGS
    // ==========================================

    // Wind Unit Dialog
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

    // Custom API Endpoint Dialog (Thay đổi API không cần code)
    if (showCustomEndpointDialog) {
        var tempUrl by remember { mutableStateOf(customEndpointUrl) }
        var tempName by remember { mutableStateOf(customEndpointName) }
        var tempKey by remember { mutableStateOf(customEndpointKey) }
        var tempFormat by remember { mutableStateOf(customEndpointFormat) }
        var tempEnabled by remember { mutableStateOf(customEndpointEnabled) }

        AlertDialog(
            onDismissRequest = { showCustomEndpointDialog = false },
            title = {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Outlined.Dns,
                        contentDescription = null,
                        tint = Color(0xFF0284C7),
                        modifier = Modifier.size(24.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Đổi API Thời Tiết Khác (No-Code)", fontWeight = FontWeight.Bold, fontSize = 17.sp)
                }
            },
            text = {
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Text(
                        text = "Bạn có thể nhập bất kỳ URL API hoặc máy chủ thời tiết riêng nào mà không cần chỉnh sửa mã nguồn.",
                        fontSize = 12.sp,
                        color = Color.Gray,
                        lineHeight = 16.sp
                    )

                    OutlinedTextField(
                        value = tempName,
                        onValueChange = { tempName = it },
                        label = { Text("Tên định danh API") },
                        placeholder = { Text("Ví dụ: Server Nội Bộ, Proxy v2") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )

                    OutlinedTextField(
                        value = tempUrl,
                        onValueChange = { tempUrl = it },
                        label = { Text("URL API Endpoint") },
                        placeholder = { Text("https://api.myweather.com/v1/") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )

                    // Định dạng dữ liệu
                    Text("Định dạng dữ liệu trả về:", fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        listOf(
                            Pair("OPEN_WEATHER", "OpenWeather"),
                            Pair("WEATHER_API", "WeatherAPI"),
                            Pair("OPEN_METEO", "Open-Meteo")
                        ).forEach { (formatKey, label) ->
                            val isFmtSelected = tempFormat == formatKey
                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(if (isFmtSelected) Color(0xFF0284C7) else Color(0x1A64748B))
                                    .clickable { tempFormat = formatKey }
                                    .padding(vertical = 8.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = label,
                                    fontSize = 11.sp,
                                    color = if (isFmtSelected) Color.White else Color.Black,
                                    fontWeight = if (isFmtSelected) FontWeight.Bold else FontWeight.Normal
                                )
                            }
                        }
                    }

                    OutlinedTextField(
                        value = tempKey,
                        onValueChange = { tempKey = it },
                        label = { Text("API Key / Bearer Token (Tùy chọn)") },
                        placeholder = { Text("Để trống nếu API không yêu cầu key") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )

                    // Switch kích hoạt
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("Kích hoạt & Ưu tiên API này", fontSize = 13.sp, fontWeight = FontWeight.Medium)
                        Switch(
                            checked = tempEnabled,
                            onCheckedChange = { tempEnabled = it },
                            colors = SwitchDefaults.colors(
                                checkedThumbColor = Color.White,
                                checkedTrackColor = Color(0xFF0284C7)
                            )
                        )
                    }

                    // Test Connection Button & Status
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        OutlinedButton(
                            onClick = {
                                viewModel.testCustomEndpoint(tempUrl, tempName, tempKey, tempFormat)
                            },
                            enabled = tempUrl.isNotBlank() && !isTestingCustomEndpoint,
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            if (isTestingCustomEndpoint) {
                                CircularProgressIndicator(modifier = Modifier.size(14.dp), strokeWidth = 2.dp)
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("Đang thử...", fontSize = 12.sp)
                            } else {
                                Text("Kiểm tra thử", fontSize = 12.sp)
                            }
                        }

                        customEndpointTestResult?.let { res ->
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Box(
                                    modifier = Modifier
                                        .size(8.dp)
                                        .clip(CircleShape)
                                        .background(if (res.isOnline) Color(0xFF22C55E) else Color(0xFFEF4444))
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(
                                    text = if (res.isOnline) "${res.latencyMs}ms OK" else "Lỗi",
                                    fontSize = 12.sp,
                                    color = if (res.isOnline) Color(0xFF16A34A) else Color(0xFFDC2626),
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        viewModel.saveCustomEndpoint(
                            url = tempUrl,
                            name = tempName,
                            enabled = tempEnabled,
                            apiKey = tempKey,
                            format = tempFormat
                        )
                        showCustomEndpointDialog = false
                        viewModel.testAllApisHealth()
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF0284C7))
                ) {
                    Text("Lưu & Áp dụng", color = Color.White)
                }
            },
            dismissButton = {
                TextButton(
                    onClick = {
                        viewModel.resetCustomEndpoint()
                        showCustomEndpointDialog = false
                        viewModel.testAllApisHealth()
                    }
                ) {
                    Text("Khôi phục mặc định", color = Color(0xFFEF4444))
                }
            }
        )
    }

    // Custom API Key Dialog (Cho các dịch vụ mặc định)
    if (showApiKeyDialog) {
        var tempWeatherKey by remember { mutableStateOf(weatherApiKey) }
        var tempOpenWeatherKey by remember { mutableStateOf(openWeatherKey) }

        AlertDialog(
            onDismissRequest = { showApiKeyDialog = false },
            title = {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Outlined.Key,
                        contentDescription = null,
                        tint = Color(0xFF0284C7),
                        modifier = Modifier.size(24.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Cấu hình API Key Cá nhân", fontWeight = FontWeight.Bold, fontSize = 18.sp)
                }
            },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Text(
                        text = "Ứng dụng đã tích hợp sẵn API Key mặc định. Bạn có thể nhập thêm Key cá nhân nếu muốn mở rộng giới hạn gọi.",
                        fontSize = 12.sp,
                        color = Color.Gray,
                        lineHeight = 16.sp
                    )

                    OutlinedTextField(
                        value = tempWeatherKey,
                        onValueChange = { tempWeatherKey = it },
                        label = { Text("WeatherAPI.com Key") },
                        placeholder = { Text("Để trống để dùng key mặc định") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )

                    OutlinedTextField(
                        value = tempOpenWeatherKey,
                        onValueChange = { tempOpenWeatherKey = it },
                        label = { Text("OpenWeatherMap Key") },
                        placeholder = { Text("Để trống để dùng key mặc định") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        viewModel.saveCustomApiKeys(tempWeatherKey, tempOpenWeatherKey)
                        showApiKeyDialog = false
                        viewModel.testAllApisHealth()
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF0284C7))
                ) {
                    Text("Lưu & Kiểm tra", color = Color.White)
                }
            },
            dismissButton = {
                TextButton(
                    onClick = {
                        tempWeatherKey = ""
                        tempOpenWeatherKey = ""
                        viewModel.saveCustomApiKeys("", "")
                        showApiKeyDialog = false
                        viewModel.testAllApisHealth()
                    }
                ) {
                    Text("Đặt lại mặc định", color = Color(0xFFEF4444))
                }
            }
        )
    }
}

@Composable
private fun ApiHealthItem(status: ApiHealthStatus) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(Color(0x1A0F172A))
            .border(1.dp, Color(0x1AFFFFFF), RoundedCornerShape(12.dp))
            .padding(12.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = status.providerName,
                    color = Color.White,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.SemiBold
                )
                if (status.isPrimary) {
                    Spacer(modifier = Modifier.width(6.dp))
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(6.dp))
                            .background(Color(0xFF0284C7))
                            .padding(horizontal = 6.dp, vertical = 2.dp)
                    ) {
                        Text(
                            text = "Ưu tiên",
                            color = Color.White,
                            fontSize = 9.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = status.message,
                color = if (status.isOnline) Color(0x9994A3B8) else Color(0xFFF87171),
                fontSize = 11.sp
            )
        }

        Row(verticalAlignment = Alignment.CenterVertically) {
            // Status Dot
            Box(
                modifier = Modifier
                    .size(8.dp)
                    .clip(CircleShape)
                    .background(if (status.isOnline) Color(0xFF22C55E) else Color(0xFFEF4444))
            )
            Spacer(modifier = Modifier.width(6.dp))
            Text(
                text = if (status.isOnline) "${status.latencyMs}ms" else "Lỗi",
                color = if (status.isOnline) Color(0xFF4ADE80) else Color(0xFFF87171),
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold
            )
        }
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
