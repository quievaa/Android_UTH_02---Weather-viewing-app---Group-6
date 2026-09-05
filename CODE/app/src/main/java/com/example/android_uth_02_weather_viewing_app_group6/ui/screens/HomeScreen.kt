package com.example.android_uth_02_weather_viewing_app_group6.ui.screens

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.tween
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
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.outlined.Air
import androidx.compose.material.icons.outlined.CalendarMonth
import androidx.compose.material.icons.outlined.Compress
import androidx.compose.material.icons.outlined.KeyboardArrowDown
import androidx.compose.material.icons.outlined.Navigation
import androidx.compose.material.icons.outlined.Schedule
import androidx.compose.material.icons.outlined.WaterDrop
import androidx.compose.material.icons.outlined.WbSunny
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.android_uth_02_weather_viewing_app_group6.domain.model.CurrentWeather
import com.example.android_uth_02_weather_viewing_app_group6.ui.components.GlassCard
import com.example.android_uth_02_weather_viewing_app_group6.ui.components.TemperatureRangeBar
import com.example.android_uth_02_weather_viewing_app_group6.ui.components.Weather3DBackground
import com.example.android_uth_02_weather_viewing_app_group6.ui.components.WeatherIcon
import com.example.android_uth_02_weather_viewing_app_group6.ui.model.CityLocation
import com.example.android_uth_02_weather_viewing_app_group6.ui.model.DailyForecast
import com.example.android_uth_02_weather_viewing_app_group6.ui.model.HourlyForecast
import com.example.android_uth_02_weather_viewing_app_group6.ui.model.WeatherCondition
import com.example.android_uth_02_weather_viewing_app_group6.ui.viewmodel.WeatherUiState
import com.example.android_uth_02_weather_viewing_app_group6.ui.viewmodel.WeatherViewModel
import java.util.Locale

import android.Manifest
import android.content.pm.PackageManager
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.material.icons.filled.MyLocation
import androidx.compose.ui.platform.LocalContext
import androidx.core.content.ContextCompat
import com.example.android_uth_02_weather_viewing_app_group6.data.location.LocationTracker

import androidx.compose.material.icons.outlined.NearMe
import androidx.compose.runtime.LaunchedEffect
import com.example.android_uth_02_weather_viewing_app_group6.data.location.ReverseGeocoder

@Composable
fun HomeScreen(
    contentPadding: PaddingValues,
    onForecastClick: () -> Unit,
    onSearchClick: () -> Unit = {},
    viewModel: WeatherViewModel,
    locationTracker: LocationTracker? = null
) {
    val context = LocalContext.current
    val uiState by viewModel.uiState.collectAsState()
    val isRefreshing by viewModel.isRefreshing.collectAsState()
    val favoriteCities by viewModel.favoriteCities.collectAsState()
    val isCurrentLocation by viewModel.isCurrentLocation.collectAsState()
    val locationSubName by viewModel.locationSubName.collectAsState()

    val reverseGeocoder = remember(context) { ReverseGeocoder(context) }

    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        val fineGranted = permissions[Manifest.permission.ACCESS_FINE_LOCATION] ?: false
        val coarseGranted = permissions[Manifest.permission.ACCESS_COARSE_LOCATION] ?: false

        if (fineGranted || coarseGranted) {
            if (locationTracker != null) {
                Toast.makeText(context, "Đang lấy vị trí GPS hiện tại...", Toast.LENGTH_SHORT).show()
                viewModel.fetchLocationWeather(locationTracker, reverseGeocoder) { success, msg ->
                    if (!success) {
                        Toast.makeText(context, msg ?: "Không thể lấy vị trí GPS", Toast.LENGTH_SHORT).show()
                    }
                }
            }
        } else {
            Toast.makeText(
                context,
                "Ứng dụng cần quyền vị trí để lấy thời tiết GPS hiện tại.",
                Toast.LENGTH_SHORT
            ).show()
        }
    }

    val requestGpsLocation = {
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
                Toast.makeText(context, "Đang định vị địa điểm của bạn...", Toast.LENGTH_SHORT).show()
                viewModel.fetchLocationWeather(locationTracker, reverseGeocoder) { success, msg ->
                    if (!success) {
                        Toast.makeText(context, msg ?: "Không thể lấy vị trí GPS", Toast.LENGTH_SHORT).show()
                    }
                }
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

    // Auto-request location once on launch if already granted
    LaunchedEffect(Unit) {
        val hasPermission = ContextCompat.checkSelfPermission(
            context,
            Manifest.permission.ACCESS_FINE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED || ContextCompat.checkSelfPermission(
            context,
            Manifest.permission.ACCESS_COARSE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED

        if (hasPermission && locationTracker != null && !viewModel.hasAutoFetchedLocation) {
            viewModel.hasAutoFetchedLocation = true
            viewModel.fetchLocationWeather(locationTracker, reverseGeocoder)
        }
    }

    val (currentTemp, currentCond) = when (val state = uiState) {
        is WeatherUiState.Success -> Pair(state.weather.temperatureC, state.weather.description)
        else -> Pair(31.0, "Nắng đẹp")
    }

    val isFav = when (val state = uiState) {
        is WeatherUiState.Success -> favoriteCities.any { it.cityName.equals(state.weather.cityName, ignoreCase = true) }
        else -> false
    }

    val hourlyList = remember(currentTemp, currentCond) {
        viewModel.getHourlyForecastList(currentTemp, currentCond)
    }
    val tenDayList = remember(currentTemp, currentCond) {
        viewModel.getTenDayForecastList(currentTemp, currentCond)
    }

    HomeScreenContent(
        contentPadding = contentPadding,
        uiState = uiState,
        isRefreshing = isRefreshing,
        isFavorite = isFav,
        isCurrentLocation = isCurrentLocation,
        locationSubName = locationSubName,
        availableCities = viewModel.availableCities,
        hourlyList = hourlyList,
        tenDayList = tenDayList,
        tempFormatter = { viewModel.formatTemperature(it) },
        windFormatter = { viewModel.formatWindSpeed(it) },
        onForecastClick = onForecastClick,
        onSearchClick = onSearchClick,
        onToggleFavorite = { viewModel.toggleFavoriteForCurrentCity() },
        onCitySelected = { city -> viewModel.loadCurrentWeather(city.name) },
        onUseCurrentLocation = requestGpsLocation,
        onRefresh = viewModel::retry,
        onRetry = viewModel::retry
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreenContent(
    contentPadding: PaddingValues,
    uiState: WeatherUiState,
    isRefreshing: Boolean,
    isFavorite: Boolean = false,
    isCurrentLocation: Boolean = false,
    locationSubName: String = "",
    availableCities: List<CityLocation> = emptyList(),
    hourlyList: List<HourlyForecast> = emptyList(),
    tenDayList: List<DailyForecast> = emptyList(),
    tempFormatter: (Double) -> String,
    windFormatter: (Double) -> String,
    onForecastClick: () -> Unit,
    onSearchClick: () -> Unit,
    onToggleFavorite: () -> Unit = {},
    onCitySelected: (CityLocation) -> Unit = {},
    onUseCurrentLocation: () -> Unit = {},
    onRefresh: () -> Unit,
    onRetry: () -> Unit,
) {
    var showCityDialog by remember { mutableStateOf(false) }

    val weatherSuccess = (uiState as? WeatherUiState.Success)?.weather
    val isNight = weatherSuccess?.iconCode?.endsWith("n") == true || weatherSuccess?.description?.lowercase()?.contains("đêm") == true
    val weatherCond = weatherSuccess?.let { WeatherCondition.fromDescription(it.description, isNight) } ?: WeatherCondition.SUNNY
    val windSpeed = weatherSuccess?.windSpeedMps ?: 5.0
    val currentTemp = weatherSuccess?.temperatureC ?: 30.0

    Box(
        modifier = Modifier.fillMaxSize()
    ) {
        // Dynamic Interactive 3D Three.js Weather Simulation Background
        Weather3DBackground(
            condition = weatherCond,
            isNight = isNight,
            windSpeedMps = windSpeed,
            temperatureC = currentTemp
        )

        PullToRefreshBox(
            isRefreshing = isRefreshing,
            onRefresh = onRefresh,
            modifier = Modifier.fillMaxSize()
        ) {
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
                // Top Search Bar & Quick Location Selector
                item {
                    HomeGlassTopBar(
                        onSearchClick = onSearchClick,
                        onCityPickerClick = { showCityDialog = true },
                        onRefreshClick = onRefresh
                    )
                }

                when (uiState) {
                    is WeatherUiState.Loading -> {
                        item {
                            LoadingGlassHeroCard()
                        }
                    }

                    is WeatherUiState.Error -> {
                        item {
                            ErrorGlassHeroCard(
                                message = uiState.message,
                                onRetry = onRetry
                            )
                        }
                    }

                    is WeatherUiState.Success -> {
                        val weather = uiState.weather

                        // 1. Hero Weather Glass Card with Favorite Bookmark Button
                        item {
                            HeroWeatherSection(
                                weather = weather,
                                isFavorite = isFavorite,
                                isCurrentLocation = isCurrentLocation,
                                locationSubName = locationSubName,
                                onToggleFavorite = onToggleFavorite,
                                onCityClick = { showCityDialog = true },
                                tempFormatter = tempFormatter
                            )
                        }

                        // 2. 24-Hour Hourly Forecast Strip
                        item {
                            HourlyForecastCard(hourlyForecast = hourlyList)
                        }

                        // 3. 10-Day Forecast Preview Card
                        item {
                            TenDayForecastPreviewCard(
                                forecasts = tenDayList.take(4),
                                onViewMore = onForecastClick
                            )
                        }

                        // 4. Detailed 2x2 Glass Metrics Grid
                        item {
                            MetricsGrid(
                                weather = weather,
                                windFormatter = windFormatter
                            )
                        }
                    }
                }
            }
        }

        // City Selector Dialog
        if (showCityDialog && availableCities.isNotEmpty()) {
            CitySelectorDialog(
                currentCityName = (uiState as? WeatherUiState.Success)?.weather?.cityName ?: "TP. Hồ Chí Minh",
                cities = availableCities,
                onDismiss = { showCityDialog = false },
                onUseCurrentLocation = {
                    onUseCurrentLocation()
                    showCityDialog = false
                },
                onSelect = { city ->
                    onCitySelected(city)
                    showCityDialog = false
                }
            )
        }
    }
}

@Composable
private fun HomeGlassTopBar(
    onSearchClick: () -> Unit,
    onCityPickerClick: () -> Unit,
    onRefreshClick: () -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(10.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Search trigger pill
        GlassCard(
            modifier = Modifier
                .weight(1f)
                .height(48.dp),
            cornerRadius = 24.dp,
            backgroundColor = Color(0x331E293B),
            borderColor = Color(0x3394A3B8),
            onClick = onSearchClick
        ) {
            Row(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Default.Search,
                    contentDescription = "Search",
                    tint = Color(0xCCFFFFFF),
                    modifier = Modifier.size(20.dp)
                )
                Spacer(modifier = Modifier.width(10.dp))
                Text(
                    text = "Tìm kiếm thành phố, địa điểm...",
                    color = Color(0x99FFFFFF),
                    fontSize = 14.sp,
                    maxLines = 1
                )
            }
        }

        // Location picker icon button
        GlassCard(
            modifier = Modifier.size(48.dp),
            cornerRadius = 24.dp,
            backgroundColor = Color(0x331E293B),
            borderColor = Color(0x3394A3B8),
            onClick = onCityPickerClick
        ) {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.LocationOn,
                    contentDescription = "Chọn thành phố",
                    tint = Color(0xFF38BDF8),
                    modifier = Modifier.size(22.dp)
                )
            }
        }

        // Refresh icon button
        GlassCard(
            modifier = Modifier.size(48.dp),
            cornerRadius = 24.dp,
            backgroundColor = Color(0x331E293B),
            borderColor = Color(0x3394A3B8),
            onClick = onRefreshClick
        ) {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.Refresh,
                    contentDescription = "Làm mới",
                    tint = Color.White,
                    modifier = Modifier.size(22.dp)
                )
            }
        }
    }
}

@Composable
private fun HeroWeatherSection(
    weather: CurrentWeather,
    isFavorite: Boolean,
    isCurrentLocation: Boolean = false,
    locationSubName: String = "",
    onToggleFavorite: () -> Unit,
    onCityClick: () -> Unit,
    tempFormatter: (Double) -> String
) {
    val favTint by animateColorAsState(
        targetValue = if (isFavorite) Color(0xFFF43F5E) else Color(0x99FFFFFF),
        animationSpec = tween(250),
        label = "fav_tint"
    )

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 8.dp, bottom = 12.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Top Location Header (iOS Apple Weather Style: Clean Large Title + Subtitle + Heart Button)
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 8.dp)
        ) {
            // Main City & Location Name (Center)
            Column(
                modifier = Modifier
                    .align(Alignment.Center)
                    .clickable { onCityClick() }
                    .testTag("home_city_selector"),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Center
                ) {
                    if (isCurrentLocation) {
                        Icon(
                            imageVector = Icons.Outlined.Navigation,
                            contentDescription = "Vị trí GPS",
                            tint = Color.White,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                    }
                    Text(
                        text = if (isCurrentLocation) "Vị trí của tôi" else weather.cityName,
                        color = Color.White,
                        fontSize = 28.sp,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = (-0.5).sp
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Icon(
                        imageVector = Icons.Outlined.KeyboardArrowDown,
                        contentDescription = "Đổi thành phố",
                        tint = Color(0x99FFFFFF),
                        modifier = Modifier.size(20.dp)
                    )
                }

                // Subtitle (District / City or Region)
                val cleanSub = locationSubName.removePrefix("Vị trí của tôi • ").trim()
                if (isCurrentLocation && cleanSub.isNotBlank() && cleanSub != "Vị trí của tôi") {
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = cleanSub,
                        color = Color(0xCCFFFFFF),
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Normal
                    )
                }
            }

            // Favorite Bookmark Button (Top Right)
            Box(
                modifier = Modifier
                    .align(Alignment.CenterEnd)
                    .size(40.dp)
                    .clip(CircleShape)
                    .background(if (isFavorite) Color(0x33F43F5E) else Color(0x261E293B))
                    .border(1.dp, if (isFavorite) Color(0x66F43F5E) else Color(0x2694A3B8), CircleShape)
                    .clickable { onToggleFavorite() }
                    .testTag("home_favorite_button"),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = if (isFavorite) Icons.Default.Favorite else Icons.Default.FavoriteBorder,
                    contentDescription = "Yêu thích",
                    tint = favTint,
                    modifier = Modifier.size(20.dp)
                )
            }
        }

        Spacer(modifier = Modifier.height(10.dp))

        // Huge Main Temperature (iOS style)
        Text(
            text = tempFormatter(weather.temperatureC),
            color = Color.White,
            fontSize = 72.sp,
            fontWeight = FontWeight.Light,
            letterSpacing = (-2).sp
        )

        // Weather Condition Description
        Text(
            text = weather.description.replaceFirstChar {
                if (it.isLowerCase()) it.titlecase(Locale.getDefault()) else it.toString()
            },
            color = Color.White,
            fontSize = 22.sp,
            fontWeight = FontWeight.Medium
        )

        Spacer(modifier = Modifier.height(6.dp))

        // Feels like + High/Low Temperatures
        Text(
            text = "Cảm giác như ${tempFormatter(weather.feelsLikeC)}  •  Cao: ${tempFormatter(weather.maxTemperatureC)}  Thấp: ${tempFormatter(weather.minTemperatureC)}",
            color = Color(0xE6FFFFFF),
            fontSize = 14.sp,
            fontWeight = FontWeight.Normal
        )
    }
}

@Composable
private fun HourlyForecastCard(
    hourlyForecast: List<HourlyForecast>
) {
    GlassCard(
        modifier = Modifier
            .fillMaxWidth()
            .testTag("hourly_forecast_card"),
        cornerRadius = 24.dp,
        backgroundColor = Color(0x331E293B),
        borderColor = Color(0x3394A3B8)
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Outlined.Schedule,
                    contentDescription = null,
                    tint = Color(0xCCFFFFFF),
                    modifier = Modifier.size(16.dp)
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = "Dự báo hàng giờ",
                    color = Color(0xCCFFFFFF),
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Medium
                )
            }

            Spacer(modifier = Modifier.height(14.dp))

            LazyRow(
                horizontalArrangement = Arrangement.spacedBy(20.dp)
            ) {
                items(hourlyForecast) { item ->
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = item.time,
                            color = Color.White,
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Medium
                        )

                        Spacer(modifier = Modifier.height(10.dp))

                        WeatherIcon(
                            condition = item.condition,
                            size = 28.dp
                        )

                        Spacer(modifier = Modifier.height(10.dp))

                        Text(
                            text = "${item.temp}°",
                            color = Color.White,
                            fontSize = 15.sp,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun TenDayForecastPreviewCard(
    forecasts: List<DailyForecast>,
    onViewMore: () -> Unit
) {
    GlassCard(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onViewMore() }
            .testTag("ten_day_preview_card"),
        cornerRadius = 24.dp,
        backgroundColor = Color(0x331E293B),
        borderColor = Color(0x3394A3B8)
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Outlined.CalendarMonth,
                        contentDescription = null,
                        tint = Color(0xCCFFFFFF),
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "Dự báo nhiều ngày tới",
                        color = Color(0xCCFFFFFF),
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Medium
                    )
                }

                Text(
                    text = "Xem chi tiết →",
                    color = Color(0xFFBAE6FD),
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Medium
                )
            }

            Spacer(modifier = Modifier.height(14.dp))

            forecasts.forEach { item ->
                ForecastPreviewRowItem(item = item)
                Spacer(modifier = Modifier.height(10.dp))
            }
        }
    }
}

@Composable
private fun ForecastPreviewRowItem(
    item: DailyForecast
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = item.dayName,
            color = Color.White,
            fontSize = 15.sp,
            fontWeight = FontWeight.Medium,
            modifier = Modifier.width(72.dp)
        )

        WeatherIcon(
            condition = item.condition,
            size = 24.dp,
            modifier = Modifier.width(36.dp)
        )

        Spacer(modifier = Modifier.width(8.dp))

        Text(
            text = "${item.minTemp}°",
            color = Color(0xCCFFFFFF),
            fontSize = 14.sp,
            fontWeight = FontWeight.Normal,
            modifier = Modifier.width(28.dp)
        )

        TemperatureRangeBar(
            min = item.minTemp,
            max = item.maxTemp,
            modifier = Modifier
                .weight(1f)
                .padding(horizontal = 8.dp)
        )

        Text(
            text = "${item.maxTemp}°",
            color = Color.White,
            fontSize = 14.sp,
            fontWeight = FontWeight.SemiBold,
            modifier = Modifier.width(28.dp)
        )
    }
}

@Composable
private fun MetricsGrid(
    weather: CurrentWeather,
    windFormatter: (Double) -> String
) {
    Column(
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        // Row 1: Air Quality + UV Index
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            // Air Quality Card
            GlassCard(
                modifier = Modifier
                    .weight(1f)
                    .height(150.dp),
                cornerRadius = 22.dp,
                backgroundColor = Color(0x331E293B),
                borderColor = Color(0x3394A3B8)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(14.dp),
                    verticalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Outlined.Air,
                            contentDescription = null,
                            tint = Color(0xCCFFFFFF),
                            modifier = Modifier.size(15.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = "Chất lượng KK",
                            color = Color(0xCCFFFFFF),
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Medium
                        )
                    }

                    Column {
                        Text(
                            text = "42",
                            color = Color.White,
                            fontSize = 34.sp,
                            fontWeight = FontWeight.Light
                        )
                        Text(
                            text = "Tốt (Good)",
                            color = Color(0xFF34D399),
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Medium
                        )

                        Spacer(modifier = Modifier.height(4.dp))

                        Canvas(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(4.dp)
                        ) {
                            drawRoundRect(
                                color = Color(0x33FFFFFF),
                                size = Size(size.width, size.height),
                                cornerRadius = CornerRadius(2.dp.toPx(), 2.dp.toPx())
                            )
                            drawRoundRect(
                                brush = Brush.horizontalGradient(
                                    listOf(Color(0xFF34D399), Color(0xFF10B981))
                                ),
                                size = Size(size.width * 0.42f, size.height),
                                cornerRadius = CornerRadius(2.dp.toPx(), 2.dp.toPx())
                            )
                        }
                    }
                }
            }

            // UV Index Card
            GlassCard(
                modifier = Modifier
                    .weight(1f)
                    .height(150.dp),
                cornerRadius = 22.dp,
                backgroundColor = Color(0x331E293B),
                borderColor = Color(0x3394A3B8)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(14.dp),
                    verticalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Outlined.WbSunny,
                            contentDescription = null,
                            tint = Color(0xCCFFFFFF),
                            modifier = Modifier.size(15.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = "Chất lượng UV",
                            color = Color(0xCCFFFFFF),
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Medium
                        )
                    }

                    Column {
                        Text(
                            text = "6.5",
                            color = Color.White,
                            fontSize = 34.sp,
                            fontWeight = FontWeight.Light
                        )
                        Text(
                            text = "Cao (SPF 30+)",
                            color = Color(0xFFFBBF24),
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Medium
                        )

                        Spacer(modifier = Modifier.height(4.dp))

                        Canvas(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(4.dp)
                        ) {
                            drawRoundRect(
                                color = Color(0x33FFFFFF),
                                size = Size(size.width, size.height),
                                cornerRadius = CornerRadius(2.dp.toPx(), 2.dp.toPx())
                            )
                            drawRoundRect(
                                brush = Brush.horizontalGradient(
                                    listOf(Color(0xFFFBBF24), Color(0xFFFB923C))
                                ),
                                size = Size(size.width * 0.65f, size.height),
                                cornerRadius = CornerRadius(2.dp.toPx(), 2.dp.toPx())
                            )
                        }
                    }
                }
            }
        }

        // Row 2: Wind + Humidity
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            // Wind Card
            GlassCard(
                modifier = Modifier
                    .weight(1f)
                    .height(140.dp),
                cornerRadius = 22.dp,
                backgroundColor = Color(0x331E293B),
                borderColor = Color(0x3394A3B8)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(14.dp),
                    verticalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Outlined.Air,
                            contentDescription = null,
                            tint = Color(0xCCFFFFFF),
                            modifier = Modifier.size(15.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = "Tốc độ gió",
                            color = Color(0xCCFFFFFF),
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Medium
                        )
                    }

                    Column {
                        Text(
                            text = windFormatter(weather.windSpeedMps),
                            color = Color.White,
                            fontSize = 24.sp,
                            fontWeight = FontWeight.SemiBold
                        )
                        Text(
                            text = weather.windDirectionDeg?.let { "Hướng $it°" } ?: "Gió nhẹ",
                            color = Color(0xCCFFFFFF),
                            fontSize = 12.sp
                        )
                    }
                }
            }

            // Humidity Card
            GlassCard(
                modifier = Modifier
                    .weight(1f)
                    .height(140.dp),
                cornerRadius = 22.dp,
                backgroundColor = Color(0x331E293B),
                borderColor = Color(0x3394A3B8)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(14.dp),
                    verticalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Outlined.WaterDrop,
                            contentDescription = null,
                            tint = Color(0xCCFFFFFF),
                            modifier = Modifier.size(15.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = "Độ ẩm",
                            color = Color(0xCCFFFFFF),
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Medium
                        )
                    }

                    Column {
                        Text(
                            text = "${weather.humidityPercent}%",
                            color = Color.White,
                            fontSize = 28.sp,
                            fontWeight = FontWeight.SemiBold
                        )
                        Text(
                            text = if (weather.humidityPercent > 70) "Độ ẩm cao" else "Dễ chịu",
                            color = Color(0xCCFFFFFF),
                            fontSize = 12.sp
                        )
                    }
                }
            }
        }

        // Row 3: Pressure + Coordinates
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            // Pressure Card
            GlassCard(
                modifier = Modifier
                    .weight(1f)
                    .height(130.dp),
                cornerRadius = 22.dp,
                backgroundColor = Color(0x331E293B),
                borderColor = Color(0x3394A3B8)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(14.dp),
                    verticalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Outlined.Compress,
                            contentDescription = null,
                            tint = Color(0xCCFFFFFF),
                            modifier = Modifier.size(15.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = "Áp suất",
                            color = Color(0xCCFFFFFF),
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Medium
                        )
                    }

                    Column {
                        Text(
                            text = "${weather.pressureHpa.toInt()} hPa",
                            color = Color.White,
                            fontSize = 22.sp,
                            fontWeight = FontWeight.SemiBold
                        )
                        Text(
                            text = "Ổn định",
                            color = Color(0xCCFFFFFF),
                            fontSize = 12.sp
                        )
                    }
                }
            }

            // Coordinates Card
            GlassCard(
                modifier = Modifier
                    .weight(1f)
                    .height(130.dp),
                cornerRadius = 22.dp,
                backgroundColor = Color(0x331E293B),
                borderColor = Color(0x3394A3B8)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(14.dp),
                    verticalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Outlined.Navigation,
                            contentDescription = null,
                            tint = Color(0xCCFFFFFF),
                            modifier = Modifier.size(15.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = "Tọa độ GPS",
                            color = Color(0xCCFFFFFF),
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Medium
                        )
                    }

                    Column {
                        Text(
                            text = "Lat: ${weather.latitude?.let { String.format(Locale.US, "%.2f", it) } ?: "--"}",
                            color = Color.White,
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Medium
                        )
                        Text(
                            text = "Lon: ${weather.longitude?.let { String.format(Locale.US, "%.2f", it) } ?: "--"}",
                            color = Color(0xCCFFFFFF),
                            fontSize = 13.sp
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun LoadingGlassHeroCard() {
    GlassCard(
        modifier = Modifier.fillMaxWidth(),
        cornerRadius = 24.dp,
        backgroundColor = Color(0x331E293B),
        borderColor = Color(0x3394A3B8)
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp),
            modifier = Modifier
                .fillMaxWidth()
                .padding(48.dp)
        ) {
            CircularProgressIndicator(
                color = Color(0xFF38BDF8),
                strokeWidth = 3.5.dp
            )
            Text(
                text = "Đang tải dữ liệu thời tiết mới nhất...",
                color = Color.White,
                fontSize = 15.sp,
                fontWeight = FontWeight.Medium
            )
        }
    }
}

@Composable
private fun ErrorGlassHeroCard(
    message: String,
    onRetry: () -> Unit
) {
    GlassCard(
        modifier = Modifier.fillMaxWidth(),
        cornerRadius = 24.dp,
        backgroundColor = Color(0x4D7F1D1D),
        borderColor = Color(0x80EF4444)
    ) {
        Column(
            verticalArrangement = Arrangement.spacedBy(12.dp),
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp)
        ) {
            Text(
                text = "Không thể tải dữ liệu thời tiết",
                color = Color(0xFFFCA5A5),
                fontSize = 17.sp,
                fontWeight = FontWeight.Bold
            )
            Text(
                text = message,
                color = Color(0xFFFEE2E2),
                fontSize = 14.sp
            )
            FilledTonalButton(
                onClick = onRetry,
                shape = RoundedCornerShape(12.dp)
            ) {
                Icon(Icons.Default.Refresh, contentDescription = null, modifier = Modifier.size(16.dp))
                Spacer(modifier = Modifier.width(6.dp))
                Text("Thử lại")
            }
        }
    }
}

@Composable
private fun CitySelectorDialog(
    currentCityName: String,
    cities: List<CityLocation>,
    onDismiss: () -> Unit,
    onUseCurrentLocation: () -> Unit = {},
    onSelect: (CityLocation) -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text("Chọn địa điểm", fontWeight = FontWeight.Bold)
        },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(12.dp))
                        .background(Color(0x220284C7))
                        .clickable { onUseCurrentLocation() }
                        .padding(horizontal = 12.dp, vertical = 12.dp),
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.MyLocation,
                        contentDescription = "Vị trí hiện tại",
                        tint = Color(0xFF38BDF8)
                    )
                    Text(
                        text = "Sử dụng vị trí hiện tại (GPS)",
                        fontWeight = FontWeight.SemiBold,
                        color = Color(0xFF38BDF8)
                    )
                }

                cities.forEach { city ->
                    val isSelected = city.name.equals(currentCityName, ignoreCase = true)
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(12.dp))
                            .background(if (isSelected) Color(0x220284C7) else Color.Transparent)
                            .clickable { onSelect(city) }
                            .padding(horizontal = 12.dp, vertical = 10.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(
                                text = city.name,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                color = if (isSelected) Color(0xFF0284C7) else Color.Unspecified
                            )
                            Text(
                                text = city.country,
                                fontSize = 12.sp,
                                color = Color.Gray
                            )
                        }

                        Text(
                            text = "${city.currentTemp}°",
                            fontWeight = FontWeight.SemiBold,
                            fontSize = 18.sp
                        )
                    }
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text("Đóng")
            }
        }
    )
}
