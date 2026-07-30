package com.example.android_uth_02_weather_viewing_app_group6

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.android_uth_02_weather_viewing_app_group6.ui.theme.Android_UTH_02_Weather_Viewing_App_Group6Theme
import com.example.android_uth_02_weather_viewing_app_group6.ui.components.ErrorScreen
import com.example.android_uth_02_weather_viewing_app_group6.ui.components.LoadingScreen
import com.example.android_uth_02_weather_viewing_app_group6.ui.setting.SettingScreen
import com.example.android_uth_02_weather_viewing_app_group6.ui.splash.SplashScreen

/**
 * Định nghĩa các màn hình trong ứng dụng
 */
enum class Screen {
    Splash, Home, Settings, Loading, Error
}

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            Android_UTH_02_Weather_Viewing_App_Group6Theme {
                // Trạng thái màn hình hiện tại
                var currentScreen by remember { mutableStateOf(Screen.Splash) }

                // Luồng điều hướng chính
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    when (currentScreen) {
                        Screen.Splash -> {
                            SplashScreen(onNextScreen = {
                                currentScreen = Screen.Home
                            })
                        }
                        Screen.Home -> {
                            HomeScreen(
                                onNavigateToSettings = { currentScreen = Screen.Settings },
                                onShowLoading = { currentScreen = Screen.Loading }
                            )
                        }
                        Screen.Settings -> {
                            SettingScreen(
                                onBackClick = { currentScreen = Screen.Home }
                            )
                        }
                        Screen.Loading -> {
                            LoadingScreen()
                            // Giả lập tải xong sau 2 giây để quay lại Home
                            LaunchedEffect(Unit) {
                                kotlinx.coroutines.delay(1000)
                                currentScreen = Screen.Home
                            }
                        }
                        Screen.Error -> {
                            ErrorScreen(
                                error = "Không thể kết nối tới máy chủ thời tiết.",
                                onRetry = { currentScreen = Screen.Home }
                            )
                        }
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    onNavigateToSettings: () -> Unit,
    onShowLoading: () -> Unit
) {
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
            contentAlignment = Alignment.Center
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(
                    text = "Weather App - Group 6",
                    style = MaterialTheme.typography.headlineMedium
                )
                Spacer(modifier = Modifier.height(24.dp))
                Button(onClick = onShowLoading) {
                    Text("Giả lập tải dữ liệu")
                }
                Spacer(modifier = Modifier.height(8.dp))
                OutlinedButton(onClick = onNavigateToSettings) {
                    Text("Cài đặt ứng dụng")
                }
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun HomeScreenPreview() {
    Android_UTH_02_Weather_Viewing_App_Group6Theme {
        HomeScreen(onNavigateToSettings = {}, onShowLoading = {})
    }
}
