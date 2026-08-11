package com.example.android_uth_02_weather_viewing_app_group6.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Thermostat
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.android_uth_02_weather_viewing_app_group6.ui.components.SectionTitle
import com.example.android_uth_02_weather_viewing_app_group6.ui.components.SettingRow

@Composable
fun SettingsScreen(contentPadding: PaddingValues) {
    var useCelsius by rememberSaveable { mutableStateOf(true) }
    var notificationsEnabled by rememberSaveable { mutableStateOf(false) }

    LazyColumn(
        contentPadding = contentPadding,
        verticalArrangement = Arrangement.spacedBy(14.dp),
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp),
    ) {
        item {
            Spacer(modifier = Modifier.height(4.dp))
            SectionTitle("Preferences")
        }
        item {
            SettingRow(
                icon = Icons.Default.Thermostat,
                title = "Temperature unit",
                subtitle = if (useCelsius) "Celsius (C)" else "Fahrenheit (F)",
                checked = useCelsius,
                onCheckedChange = { useCelsius = it },
            )
        }
        item {
            SettingRow(
                icon = Icons.Default.Refresh,
                title = "Weather alerts",
                subtitle = "Receive daily update reminders",
                checked = notificationsEnabled,
                onCheckedChange = { notificationsEnabled = it },
            )
        }
    }
}
