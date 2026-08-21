package com.example.android_uth_02_weather_viewing_app_group6.ui.preview

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Devices
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.android_uth_02_weather_viewing_app_group6.domain.model.CurrentWeather
import com.example.android_uth_02_weather_viewing_app_group6.ui.screens.HomeScreenContent
import com.example.android_uth_02_weather_viewing_app_group6.ui.screens.SearchScreen
import com.example.android_uth_02_weather_viewing_app_group6.ui.screens.SplashScreen
import com.example.android_uth_02_weather_viewing_app_group6.ui.theme.Android_UTH_02_Weather_Viewing_App_Group6Theme
import com.example.android_uth_02_weather_viewing_app_group6.ui.viewmodel.WeatherUiState

private val mockWeather = CurrentWeather(
    cityName = "Thành phố Hồ Chí Minh",
    temperatureC = 31.5,
    feelsLikeC = 34.0,
    minTemperatureC = 26.0,
    maxTemperatureC = 33.5,
    description = "Trời quang đãng, nắng nhẹ",
    weatherMain = "Clear",
    humidityPercent = 72,
    pressureHpa = 1010.0,
    windSpeedMps = 3.8,
    windDirectionDeg = 120,
    latitude = 10.8231,
    longitude = 106.6297,
    iconCode = "01d",
)

@Preview(name = "Splash Screen", showBackground = true)
@Composable
private fun SplashScreenPreview() {
    Android_UTH_02_Weather_Viewing_App_Group6Theme {
        SplashScreen(onTimeout = {})
    }
}

@Preview(name = "Home Screen - Phone Portrait", device = Devices.PHONE, showBackground = true)
@Composable
private fun HomeScreenPortraitPreview() {
    Android_UTH_02_Weather_Viewing_App_Group6Theme {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.background),
        ) {
            HomeScreenContent(
                contentPadding = PaddingValues(top = 16.dp),
                uiState = WeatherUiState.Success(mockWeather),
                onForecastClick = {},
                onSearchClick = {},
                onRetry = {},
            )
        }
    }
}

@Preview(name = "Home Screen - Dark Mode", device = Devices.PHONE, showBackground = true)
@Composable
private fun HomeScreenDarkModePreview() {
    Android_UTH_02_Weather_Viewing_App_Group6Theme(darkTheme = true) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.background),
        ) {
            HomeScreenContent(
                contentPadding = PaddingValues(top = 16.dp),
                uiState = WeatherUiState.Success(mockWeather),
                onForecastClick = {},
                onSearchClick = {},
                onRetry = {},
            )
        }
    }
}

@Preview(name = "Home Screen - Landscape", device = "spec:width=891dp,height=411dp,orientation=landscape", showBackground = true)
@Composable
private fun HomeScreenLandscapePreview() {
    Android_UTH_02_Weather_Viewing_App_Group6Theme {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.background),
        ) {
            HomeScreenContent(
                contentPadding = PaddingValues(top = 16.dp),
                uiState = WeatherUiState.Success(mockWeather),
                onForecastClick = {},
                onSearchClick = {},
                onRetry = {},
            )
        }
    }
}

@Preview(name = "Home Screen - Tablet / Foldable", device = Devices.TABLET, showBackground = true)
@Composable
private fun HomeScreenTabletPreview() {
    Android_UTH_02_Weather_Viewing_App_Group6Theme {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.background),
        ) {
            HomeScreenContent(
                contentPadding = PaddingValues(top = 16.dp),
                uiState = WeatherUiState.Success(mockWeather),
                onForecastClick = {},
                onSearchClick = {},
                onRetry = {},
            )
        }
    }
}

@Preview(name = "Search Screen", showBackground = true)
@Composable
private fun SearchScreenPreview() {
    Android_UTH_02_Weather_Viewing_App_Group6Theme {
        SearchScreen(
            contentPadding = PaddingValues(16.dp),
            onCitySelected = {},
        )
    }
}

