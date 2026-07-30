package com.example.android_uth_02_weather_viewing_app_group6.ui.navigation

import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import com.example.android_uth_02_weather_viewing_app_group6.ui.screens.FavoriteScreen
import com.example.android_uth_02_weather_viewing_app_group6.ui.screens.ForecastScreen
import com.example.android_uth_02_weather_viewing_app_group6.ui.screens.HomeScreen
import com.example.android_uth_02_weather_viewing_app_group6.ui.screens.SearchScreen
import com.example.android_uth_02_weather_viewing_app_group6.ui.screens.SettingsScreen
import com.example.android_uth_02_weather_viewing_app_group6.ui.screens.SplashScreen
import kotlinx.coroutines.delay

@Composable
fun WeatherApp() {
    var showSplash by rememberSaveable { mutableStateOf(true) }
    var currentScreen by rememberSaveable { mutableStateOf(WeatherScreen.Home) }

    LaunchedEffect(Unit) {
        delay(1200)
        showSplash = false
    }

    if (showSplash) {
        SplashScreen()
    } else {
        MainWeatherScaffold(
            currentScreen = currentScreen,
            onScreenSelected = { currentScreen = it },
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainWeatherScaffold(
    currentScreen: WeatherScreen,
    onScreenSelected: (WeatherScreen) -> Unit,
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(currentScreen.title) },
                actions = {
                    IconButton(onClick = { onScreenSelected(WeatherScreen.Search) }) {
                        Icon(Icons.Default.Search, contentDescription = "Open search")
                    }
                    IconButton(onClick = { onScreenSelected(WeatherScreen.Settings) }) {
                        Icon(Icons.Default.Settings, contentDescription = "Open settings")
                    }
                },
            )
        },
        bottomBar = {
            NavigationBar {
                WeatherScreen.entries.forEach { screen ->
                    NavigationBarItem(
                        selected = currentScreen == screen,
                        onClick = { onScreenSelected(screen) },
                        icon = {
                            Icon(
                                imageVector = screen.icon,
                                contentDescription = screen.title,
                            )
                        },
                        label = { Text(screen.title) },
                    )
                }
            }
        },
        modifier = Modifier.windowInsetsPadding(WindowInsets.safeDrawing),
    ) { innerPadding ->
        when (currentScreen) {
            WeatherScreen.Home -> HomeScreen(
                contentPadding = innerPadding,
                onForecastClick = { onScreenSelected(WeatherScreen.Forecast) },
            )

            WeatherScreen.Search -> SearchScreen(contentPadding = innerPadding)
            WeatherScreen.Forecast -> ForecastScreen(contentPadding = innerPadding)
            WeatherScreen.Favorite -> FavoriteScreen(contentPadding = innerPadding)
            WeatherScreen.Settings -> SettingsScreen(contentPadding = innerPadding)
        }
    }
}
