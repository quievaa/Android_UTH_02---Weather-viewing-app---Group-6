package com.example.android_uth_02_weather_viewing_app_group6.ui.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import com.example.android_uth_02_weather_viewing_app_group6.ui.navigation.WeatherScreen

@Composable
fun BottomFloatingNavBar(
    selectedScreen: WeatherScreen,
    onScreenSelected: (WeatherScreen) -> Unit,
    modifier: Modifier = Modifier
) {
    val navTabs = remember {
        listOf(
            WeatherScreen.Home,
            WeatherScreen.Radar,
            WeatherScreen.TripPlanner,
            WeatherScreen.Forecast,
            WeatherScreen.Favorite,
            WeatherScreen.Settings
        )
    }

    Box(
        modifier = modifier
            .fillMaxWidth()
            .navigationBarsPadding()
            .padding(horizontal = 16.dp, vertical = 10.dp),
        contentAlignment = Alignment.Center
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(62.dp)
                .shadow(20.dp, RoundedCornerShape(32.dp), spotColor = Color(0x80000000))
                .clip(RoundedCornerShape(32.dp))
                .background(Color(0xD90F172A))
                .border(1.dp, Color(0x3394A3B8), RoundedCornerShape(32.dp))
                .padding(horizontal = 8.dp),
            horizontalArrangement = Arrangement.SpaceAround,
            verticalAlignment = Alignment.CenterVertically
        ) {
            navTabs.forEach { screen ->
                NavBarItem(
                    icon = screen.icon,
                    label = screen.title,
                    isSelected = selectedScreen == screen,
                    onClick = { onScreenSelected(screen) },
                    testTag = screen.testTag
                )
            }
        }
    }
}

@Composable
private fun NavBarItem(
    icon: ImageVector,
    label: String,
    isSelected: Boolean,
    onClick: () -> Unit,
    testTag: String
) {
    val interactionSource = remember { MutableInteractionSource() }
    val tintColor by animateColorAsState(
        targetValue = if (isSelected) Color.White else Color(0xFF94A3B8),
        animationSpec = tween(200),
        label = "nav_tint"
    )

    val bgColor by animateColorAsState(
        targetValue = if (isSelected) Color(0x3338BDF8) else Color.Transparent,
        animationSpec = tween(200),
        label = "nav_bg"
    )

    Box(
        modifier = Modifier
            .size(44.dp)
            .clip(CircleShape)
            .background(bgColor)
            .clickable(
                interactionSource = interactionSource,
                indication = null
            ) { onClick() }
            .testTag(testTag),
        contentAlignment = Alignment.Center
    ) {
        Icon(
            imageVector = icon,
            contentDescription = label,
            tint = tintColor,
            modifier = Modifier.size(22.dp)
        )
    }
}
