package com.example.android_uth_02_weather_viewing_app_group6.ui.preview

import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.Preview
import com.example.android_uth_02_weather_viewing_app_group6.ui.navigation.MainWeatherScaffold
import com.example.android_uth_02_weather_viewing_app_group6.ui.navigation.WeatherScreen
import com.example.android_uth_02_weather_viewing_app_group6.ui.theme.Android_UTH_02_Weather_Viewing_App_Group6Theme

@Preview(showBackground = true)
@Composable
private fun WeatherAppPreview() {
    Android_UTH_02_Weather_Viewing_App_Group6Theme {
        MainWeatherScaffold(
            currentScreen = WeatherScreen.Home,
            onScreenSelected = {},
        )
    }
}
