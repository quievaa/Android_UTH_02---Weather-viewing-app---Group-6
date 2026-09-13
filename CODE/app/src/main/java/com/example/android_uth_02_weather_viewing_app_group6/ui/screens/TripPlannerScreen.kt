package com.example.android_uth_02_weather_viewing_app_group6.ui.screens

import androidx.compose.animation.AnimatedVisibility
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
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.DirectionsCar
import androidx.compose.material.icons.filled.Navigation
import androidx.compose.material.icons.filled.SwapVert
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material.icons.outlined.Air
import androidx.compose.material.icons.outlined.NearMe
import androidx.compose.material.icons.outlined.Schedule
import androidx.compose.material.icons.outlined.Visibility
import androidx.compose.material.icons.outlined.WaterDrop
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.android_uth_02_weather_viewing_app_group6.ui.components.GlassCard
import com.example.android_uth_02_weather_viewing_app_group6.ui.components.Weather3DBackground
import com.example.android_uth_02_weather_viewing_app_group6.ui.components.WeatherIcon
import com.example.android_uth_02_weather_viewing_app_group6.ui.model.RouteWaypoint
import com.example.android_uth_02_weather_viewing_app_group6.ui.model.TripRoute
import com.example.android_uth_02_weather_viewing_app_group6.ui.model.WeatherCondition
import com.example.android_uth_02_weather_viewing_app_group6.ui.viewmodel.WeatherViewModel

@Composable
fun TripPlannerScreen(
    contentPadding: PaddingValues,
    viewModel: WeatherViewModel
) {
    val currentTrip by viewModel.currentTrip.collectAsState()
    val isNavigating by viewModel.isNavigating.collectAsState()
    val activeWaypointIndex by viewModel.activeWaypointIndex.collectAsState()
    val roadAlertsEnabled by viewModel.roadAlertsEnabled.collectAsState()
    var showRoutePicker by remember { mutableStateOf(false) }

    val firstWaypoint = currentTrip.waypoints.firstOrNull()
    val tripCondition = firstWaypoint?.condition ?: WeatherCondition.PARTLY_CLOUDY
    val tripTemp = firstWaypoint?.temp?.toDouble() ?: 28.0

    Box(
        modifier = Modifier.fillMaxSize()
    ) {
        // Dynamic 3D Atmosphere Background for Trip
        Weather3DBackground(
            condition = tripCondition,
            isNight = false,
            windSpeedMps = 4.0,
            temperatureC = tripTemp
        )

        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(
                start = 16.dp,
                end = 16.dp,
                top = contentPadding.calculateTopPadding() + 8.dp,
                bottom = contentPadding.calculateBottomPadding() + 140.dp
            ),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Origin & Destination Card
            item {
                OriginDestinationCard(
                    trip = currentTrip,
                    onSwap = { viewModel.swapOriginDestination() },
                    onPickRoute = { showRoutePicker = true }
                )
            }

            // Severe Road Warning Banner
            if (currentTrip.hasSevereWarning && roadAlertsEnabled) {
                item {
                    RoadWarningCard(
                        title = currentTrip.warningTitle,
                        description = currentTrip.warningDesc
                    )
                }
            }

            // Route Waypoints with Timeline
            item {
                Text(
                    text = "Lộ trình & Điểm dừng thời tiết",
                    color = Color(0xCCFFFFFF),
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Medium,
                    modifier = Modifier.padding(start = 4.dp, top = 4.dp)
                )
            }

            itemsIndexed(
                items = currentTrip.waypoints,
                key = { index: Int, waypoint: RouteWaypoint -> "${waypoint.locationName}_$index" }
            ) { index: Int, waypoint: RouteWaypoint ->
                WaypointTimelineItem(
                    waypoint = waypoint,
                    isFirst = index == 0,
                    isLast = index == currentTrip.waypoints.lastIndex,
                    isActive = isNavigating && activeWaypointIndex == index
                )
            }
        }

        // Bottom CTA Button: "Bắt đầu hành trình"
        Box(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .fillMaxWidth()
                .padding(start = 20.dp, end = 20.dp, bottom = 80.dp)
        ) {
            Button(
                onClick = {
                    if (isNavigating) viewModel.stopJourney() else viewModel.startJourney()
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp)
                    .shadow(16.dp, RoundedCornerShape(28.dp))
                    .testTag("start_journey_button"),
                shape = RoundedCornerShape(28.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = if (isNavigating) Color(0xFFEF4444) else Color.White,
                    contentColor = if (isNavigating) Color.White else Color(0xFF0F172A)
                )
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Center
                ) {
                    Icon(
                        imageVector = if (isNavigating) Icons.Default.Navigation else Icons.Outlined.NearMe,
                        contentDescription = null,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(10.dp))
                    Text(
                        text = if (isNavigating) "Đang dẫn đường • Dừng lại" else "Bắt đầu hành trình",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }

        // Route Presets Picker Dialog
        if (showRoutePicker) {
            RoutePickerDialog(
                currentTrip = currentTrip,
                availableTrips = viewModel.availableTrips,
                onDismiss = { showRoutePicker = false },
                onSelect = {
                    viewModel.selectTrip(it)
                    showRoutePicker = false
                }
            )
        }
    }
}

@Composable
private fun OriginDestinationCard(
    trip: TripRoute,
    onSwap: () -> Unit,
    onPickRoute: () -> Unit
) {
    GlassCard(
        modifier = Modifier
            .fillMaxWidth()
            .testTag("route_input_card"),
        cornerRadius = 24.dp,
        backgroundColor = Color(0x331E293B),
        borderColor = Color(0x3394A3B8)
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Route line icon on left
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier.padding(top = 8.dp, bottom = 8.dp, end = 12.dp)
                ) {
                    // Origin Blue Ring Dot
                    Box(
                        modifier = Modifier
                            .size(14.dp)
                            .border(3.dp, Color(0xFF0284C7), CircleShape)
                    )

                    // Vertical connecting dotted line
                    Canvas(
                        modifier = Modifier
                            .width(2.dp)
                            .height(44.dp)
                    ) {
                        drawLine(
                            color = Color(0xFF0284C7),
                            start = Offset(size.width / 2, 0f),
                            end = Offset(size.width / 2, size.height),
                            strokeWidth = 2.dp.toPx()
                        )
                    }

                    // Destination Blue Dot
                    Box(
                        modifier = Modifier
                            .size(14.dp)
                            .background(Color(0xFF0284C7), CircleShape)
                    )
                }

                // Origin and Destination text inputs
                Column(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    // ORIGIN
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(12.dp))
                            .background(Color(0x260F172A))
                            .clickable { onPickRoute() }
                            .padding(horizontal = 12.dp, vertical = 8.dp)
                    ) {
                        Text(
                            text = "ĐIỂM KHỞI HÀNH (ORIGIN)",
                            color = Color(0x9994A3B8),
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 0.5.sp
                        )
                        Text(
                            text = trip.origin,
                            color = Color.White,
                            fontSize = 16.sp,
                            fontWeight = FontWeight.SemiBold
                        )
                    }

                    // DESTINATION
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(12.dp))
                            .background(Color(0x260F172A))
                            .clickable { onPickRoute() }
                            .padding(horizontal = 12.dp, vertical = 8.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(
                                text = "ĐIỂM ĐẾN (DESTINATION)",
                                color = Color(0x9994A3B8),
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                letterSpacing = 0.5.sp
                            )
                            Text(
                                text = trip.destination,
                                color = Color.White,
                                fontSize = 16.sp,
                                fontWeight = FontWeight.SemiBold
                            )
                        }

                        // Swap Button
                        Box(
                            modifier = Modifier
                                .size(36.dp)
                                .clip(CircleShape)
                                .background(Color(0x4D334155))
                                .clickable { onSwap() }
                                .testTag("swap_route_button"),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.SwapVert,
                                contentDescription = "Đổi vị trí",
                                tint = Color.White,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Footer row: Depart: Now | Duration
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 4.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Outlined.Schedule,
                        contentDescription = null,
                        tint = Color(0xCCFFFFFF),
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "Khởi hành: ${trip.departureTime}",
                        color = Color(0xCCFFFFFF),
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Medium
                    )
                }

                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Filled.DirectionsCar,
                        contentDescription = null,
                        tint = Color(0xCCFFFFFF),
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "${trip.durationText} • ${trip.distanceKm} km",
                        color = Color(0xCCFFFFFF),
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Medium
                    )
                }
            }
        }
    }
}

@Composable
private fun RoadWarningCard(
    title: String,
    description: String
) {
    GlassCard(
        modifier = Modifier
            .fillMaxWidth()
            .testTag("road_warning_banner"),
        cornerRadius = 20.dp,
        backgroundColor = Color(0x4D7F1D1D),
        borderColor = Color(0x80EF4444)
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.Top
        ) {
            Icon(
                imageVector = Icons.Default.Warning,
                contentDescription = "Warning",
                tint = Color(0xFFEF4444),
                modifier = Modifier.size(24.dp)
            )

            Spacer(modifier = Modifier.width(12.dp))

            Column {
                Text(
                    text = title,
                    color = Color(0xFFEF4444),
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Bold
                )

                Spacer(modifier = Modifier.height(4.dp))

                Text(
                    text = description,
                    color = Color(0xFFFCA5A5),
                    fontSize = 13.sp,
                    lineHeight = 18.sp
                )
            }
        }
    }
}

@Composable
private fun WaypointTimelineItem(
    waypoint: RouteWaypoint,
    isFirst: Boolean,
    isLast: Boolean,
    isActive: Boolean
) {
    var expanded by remember { mutableStateOf(waypoint.isWarning) }

    Row(
        modifier = Modifier.fillMaxWidth()
    ) {
        // Timeline Dot & Vertical Line
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.width(32.dp)
        ) {
            val nodeColor = when {
                waypoint.isWarning -> Color(0xFFEF4444)
                isActive -> Color(0xFF38BDF8)
                else -> Color.White
            }

            Box(
                modifier = Modifier
                    .padding(top = 18.dp)
                    .size(12.dp)
                    .clip(CircleShape)
                    .background(nodeColor)
                    .border(2.dp, if (isActive) Color(0xFF38BDF8) else Color(0x66FFFFFF), CircleShape)
            )

            if (!isLast) {
                Canvas(
                    modifier = Modifier
                        .width(2.dp)
                        .height(90.dp)
                ) {
                    drawLine(
                        color = Color(0x3394A3B8),
                        start = Offset(size.width / 2, 0f),
                        end = Offset(size.width / 2, size.height),
                        strokeWidth = 2.dp.toPx()
                    )
                }
            }
        }

        Spacer(modifier = Modifier.width(8.dp))

        // Waypoint Card
        val cardBg = when {
            waypoint.isWarning -> Color(0x4D3F121C)
            isActive -> Color(0x330284C7)
            else -> Color(0x331E293B)
        }
        val cardBorder = when {
            waypoint.isWarning -> Color(0x66EF4444)
            isActive -> Color(0x6638BDF8)
            else -> Color(0x2694A3B8)
        }

        GlassCard(
            modifier = Modifier
                .weight(1f)
                .clickable { expanded = !expanded }
                .testTag("waypoint_card_${waypoint.locationName.lowercase().replace(" ", "_")}"),
            cornerRadius = 18.dp,
            backgroundColor = cardBg,
            borderColor = cardBorder
        ) {
            Column(
                modifier = Modifier.padding(14.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = "${waypoint.time} • ${waypoint.type}",
                            color = if (waypoint.isWarning) Color(0xFFFCA5A5) else Color(0x9994A3B8),
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Medium
                        )
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = waypoint.locationName,
                            color = Color.White,
                            fontSize = 17.sp,
                            fontWeight = FontWeight.SemiBold
                        )
                    }

                    Row(verticalAlignment = Alignment.CenterVertically) {
                        WeatherIcon(
                            condition = waypoint.condition,
                            size = 28.dp
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "${waypoint.temp}°",
                            color = Color.White,
                            fontSize = 24.sp,
                            fontWeight = FontWeight.Light
                        )
                    }
                }

                // Expanded Weather Metrics for the Waypoint
                AnimatedVisibility(visible = expanded) {
                    Column(
                        modifier = Modifier.padding(top = 10.dp)
                    ) {
                        if (waypoint.warningDesc != null) {
                            Text(
                                text = waypoint.warningDesc,
                                color = Color(0xFFFCA5A5),
                                fontSize = 12.sp,
                                modifier = Modifier.padding(bottom = 8.dp)
                            )
                        }

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            // Wind
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = Icons.Outlined.Air,
                                    contentDescription = null,
                                    tint = Color(0x9994A3B8),
                                    modifier = Modifier.size(14.dp)
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(
                                    text = "Gió: ${waypoint.windSpeed}",
                                    color = Color(0xCCFFFFFF),
                                    fontSize = 11.sp
                                )
                            }

                            // Visibility
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = Icons.Outlined.Visibility,
                                    contentDescription = null,
                                    tint = Color(0x9994A3B8),
                                    modifier = Modifier.size(14.dp)
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(
                                    text = "Tầm nhìn: ${waypoint.visibility}",
                                    color = Color(0xCCFFFFFF),
                                    fontSize = 11.sp
                                )
                            }

                            // Rain
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = Icons.Outlined.WaterDrop,
                                    contentDescription = null,
                                    tint = Color(0x9994A3B8),
                                    modifier = Modifier.size(14.dp)
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(
                                    text = waypoint.rainAmount,
                                    color = Color(0xCCFFFFFF),
                                    fontSize = 11.sp
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun RoutePickerDialog(
    currentTrip: TripRoute,
    availableTrips: List<TripRoute>,
    onDismiss: () -> Unit,
    onSelect: (TripRoute) -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text("Chọn tuyến đường", fontWeight = FontWeight.Bold)
        },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                availableTrips.forEach { trip ->
                    val isSelected = trip.id == currentTrip.id
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(12.dp))
                            .background(if (isSelected) Color(0x220284C7) else Color(0x0D000000))
                            .clickable { onSelect(trip) }
                            .padding(12.dp)
                    ) {
                        Text(
                            text = "${trip.origin}  ➔  ${trip.destination}",
                            fontWeight = FontWeight.Bold,
                            color = if (isSelected) Color(0xFF0284C7) else Color.Unspecified
                        )
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = "${trip.durationText} • ${trip.distanceKm} km",
                            fontSize = 12.sp,
                            color = Color.Gray
                        )
                        if (trip.hasSevereWarning) {
                            Text(
                                text = "⚠️ ${trip.warningTitle}",
                                fontSize = 11.sp,
                                color = Color(0xFFEF4444),
                                fontWeight = FontWeight.Medium
                            )
                        }
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
