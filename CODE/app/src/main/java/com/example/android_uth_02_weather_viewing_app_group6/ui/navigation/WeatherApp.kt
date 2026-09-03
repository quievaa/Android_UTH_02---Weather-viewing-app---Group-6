package com.example.android_uth_02_weather_viewing_app_group6.ui.navigation

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.android_uth_02_weather_viewing_app_group6.data.location.DefaultLocationTracker
import com.example.android_uth_02_weather_viewing_app_group6.data.repository.AppPreferences
import com.example.android_uth_02_weather_viewing_app_group6.ui.components.BottomFloatingNavBar
import com.example.android_uth_02_weather_viewing_app_group6.ui.screens.FavoriteScreen
import com.example.android_uth_02_weather_viewing_app_group6.ui.screens.ForecastScreen
import com.example.android_uth_02_weather_viewing_app_group6.ui.screens.HomeScreen
import com.example.android_uth_02_weather_viewing_app_group6.ui.screens.RadarScreen
import com.example.android_uth_02_weather_viewing_app_group6.ui.screens.SearchScreen
import com.example.android_uth_02_weather_viewing_app_group6.ui.screens.SettingsScreen
import com.example.android_uth_02_weather_viewing_app_group6.ui.screens.SplashScreen
import com.example.android_uth_02_weather_viewing_app_group6.ui.screens.TripPlannerScreen
import com.example.android_uth_02_weather_viewing_app_group6.ui.viewmodel.WeatherViewModel

@Composable
fun WeatherApp() {
    var showSplash by rememberSaveable { mutableStateOf(true) }
    var currentScreen by rememberSaveable { mutableStateOf(WeatherScreen.Home) }

    if (showSplash) {
        SplashScreen(
            onTimeout = { showSplash = false }
        )
    } else {
        MainWeatherScaffold(
            currentScreen = currentScreen,
            onScreenSelected = { currentScreen = it },
        )
    }
}

@Composable
fun MainWeatherScaffold(
    currentScreen: WeatherScreen,
    onScreenSelected: (WeatherScreen) -> Unit,
) {
    val context = LocalContext.current
    val locationTracker = remember(context) { DefaultLocationTracker(context) }
    val appPreferences = remember(context) { AppPreferences(context) }

    val weatherViewModel: WeatherViewModel = viewModel(
        factory = WeatherViewModelFactory(appPreferences = appPreferences),
    )

    val searchHistory by weatherViewModel.searchHistory.collectAsState()
    val favoriteCities by weatherViewModel.favoriteCityNames.collectAsState()

    Scaffold(
        containerColor = Color(0xFF0B0F19),
        bottomBar = {
            if (currentScreen.isBottomNavTab) {
                BottomFloatingNavBar(
                    selectedScreen = currentScreen,
                    onScreenSelected = onScreenSelected
                )
            }
        },
        modifier = Modifier.windowInsetsPadding(WindowInsets.safeDrawing),
    ) { innerPadding ->
        Box(modifier = Modifier.fillMaxSize()) {
            when (currentScreen) {
                WeatherScreen.Home -> HomeScreen(
                    contentPadding = innerPadding,
                    onForecastClick = { onScreenSelected(WeatherScreen.Forecast) },
                    onSearchClick = { onScreenSelected(WeatherScreen.Search) },
                    viewModel = weatherViewModel,
                )

                WeatherScreen.Radar -> RadarScreen(
                    contentPadding = innerPadding,
                    viewModel = weatherViewModel
                )

                WeatherScreen.TripPlanner -> TripPlannerScreen(
                    contentPadding = innerPadding,
                    viewModel = weatherViewModel
                )

                WeatherScreen.Forecast -> ForecastScreen(
                    contentPadding = innerPadding,
                    viewModel = weatherViewModel,
                )

                WeatherScreen.Favorite -> FavoriteScreen(
                    contentPadding = innerPadding,
                    viewModel = weatherViewModel,
                    onCitySelected = { cityName ->
                        weatherViewModel.loadCurrentWeather(cityName)
                        onScreenSelected(WeatherScreen.Home)
                    },
                    onNavigateSearch = { onScreenSelected(WeatherScreen.Search) }
                )

                WeatherScreen.Search -> SearchScreen(
                    contentPadding = innerPadding,
                    searchHistory = searchHistory,
                    onClearHistory = {
                        weatherViewModel.clearHistory()
                    },
                    onCitySelected = { cityName: String ->
                        weatherViewModel.loadCurrentWeather(cityName)
                        onScreenSelected(WeatherScreen.Home)
                    },
                    onLocationRequested = {
                        weatherViewModel.fetchLocationWeather(locationTracker) { success, _ ->
                            if (success) {
                                onScreenSelected(WeatherScreen.Home)
                            }
                        }
                    },
                    favoriteCities = favoriteCities,
                    onToggleFavorite = { cityName: String ->
                        weatherViewModel.toggleFavorite(cityName = cityName)
                    },
                )

                WeatherScreen.Settings -> SettingsScreen(
                    contentPadding = innerPadding,
                    viewModel = weatherViewModel,
                )
            }
        }
    }
}
