package com.example.android_uth_02_weather_viewing_app_group6

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.android_uth_02_weather_viewing_app_group6.ui.components.ErrorScreen
import com.example.android_uth_02_weather_viewing_app_group6.ui.components.LoadingScreen
import com.example.android_uth_02_weather_viewing_app_group6.ui.setting.SettingScreen
import com.example.android_uth_02_weather_viewing_app_group6.ui.splash.SplashScreen
import com.example.android_uth_02_weather_viewing_app_group6.ui.theme.Android_UTH_02_Weather_Viewing_App_Group6Theme
import kotlin.time.Duration.Companion.milliseconds

enum class Screen {
    Splash, Home, Settings, Loading, Error
}

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            Android_UTH_02_Weather_Viewing_App_Group6Theme {
                var currentScreen by remember { mutableStateOf(Screen.Splash) }
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    when (currentScreen) {
                        Screen.Splash -> SplashScreen { currentScreen = Screen.Home }
                        Screen.Home -> HomeScreen(
                            onNavigateToSettings = { currentScreen = Screen.Settings },
                            onShowLoading = { currentScreen = Screen.Loading }
                        )
                        Screen.Settings -> SettingScreen(onBackClick = { currentScreen = Screen.Home })
                        Screen.Loading -> {
                            LoadingScreen()
                            LaunchedEffect(Unit) {
                                kotlinx.coroutines.delay(1500.milliseconds)
                                currentScreen = Screen.Home
                            }
                        }
                        Screen.Error -> ErrorScreen(
                            error = "Lỗi kết nối máy chủ.",
                            onRetry = { currentScreen = Screen.Home }
                        )
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(onNavigateToSettings: () -> Unit, onShowLoading: () -> Unit) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Dự báo thời tiết") },
                actions = {
                    IconButton(onClick = onNavigateToSettings) {
                        Icon(Icons.Default.Settings, contentDescription = "Cài đặt")
                    }
                }
            )
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding),
            contentAlignment = Alignment.Center,
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(text = "Weather App - Group 6", style = MaterialTheme.typography.headlineMedium)
                Spacer(modifier = Modifier.height(24.dp))
                Button(onClick = onShowLoading) { Text("Giả lập tải dữ liệu") }
                Spacer(modifier = Modifier.height(8.dp))
                OutlinedButton(onClick = onNavigateToSettings) { Text("Cài đặt ứng dụng") }
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun HomeScreenPreview() {
    Android_UTH_02_Weather_Viewing_App_Group6Theme {
        HomeScreen(
            onNavigateToSettings = {},
            onShowLoading = {},
        )
    }
}
