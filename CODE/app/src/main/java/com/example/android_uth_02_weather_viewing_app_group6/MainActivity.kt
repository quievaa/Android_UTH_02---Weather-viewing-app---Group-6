package com.example.android_uth_02_weather_viewing_app_group6

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import com.example.android_uth_02_weather_viewing_app_group6.ui.navigation.WeatherApp
import com.example.android_uth_02_weather_viewing_app_group6.ui.theme.Android_UTH_02_Weather_Viewing_App_Group6Theme
// 1. Import thư viện cấu hình Osmdroid
import org.osmdroid.config.Configuration

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val basePath = java.io.File(cacheDir, "osmdroid")
        val tileCache = java.io.File(basePath, "tiles")
        try {
            val flagFile = java.io.File(basePath, "v2_cache_cleared")
            if (!flagFile.exists()) {
                tileCache.deleteRecursively()
                tileCache.mkdirs()
                flagFile.createNewFile()
            }
        } catch (_: Exception) {}
        tileCache.mkdirs()

        Configuration.getInstance().apply {
            load(applicationContext, getSharedPreferences("osmdroid", MODE_PRIVATE))
            osmdroidBasePath = basePath
            osmdroidTileCache = tileCache
            userAgentValue = "UTH_Weather_Viewing_App_Group6/1.0 (contact: student@uth.edu.vn; Android Client; UTH Ho Chi Minh City)"
            userAgentHttpHeader = "User-Agent"
        }

        enableEdgeToEdge()
        setContent {
            Android_UTH_02_Weather_Viewing_App_Group6Theme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background,
                ) {
                    WeatherApp()
                }
            }
        }
    }
}