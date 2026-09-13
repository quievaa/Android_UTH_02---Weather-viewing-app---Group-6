package com.example.android_uth_02_weather_viewing_app_group6.ui.components

import android.content.Context
import android.graphics.Color as AndroidColor
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.CenterFocusStrong
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import com.example.android_uth_02_weather_viewing_app_group6.ui.model.TripRoute
import org.osmdroid.config.Configuration
import org.osmdroid.tileprovider.tilesource.TileSourceFactory
import org.osmdroid.tileprovider.tilesource.XYTileSource
import org.osmdroid.util.BoundingBox
import org.osmdroid.util.GeoPoint
import org.osmdroid.views.CustomZoomButtonsController
import org.osmdroid.views.MapView
import org.osmdroid.views.overlay.Marker
import org.osmdroid.views.overlay.Polyline
import java.io.File
import java.util.concurrent.atomic.AtomicReference

val CARTO_VOYAGER_TILE_SOURCE = XYTileSource(
    "CartoVoyager",
    0, 20, 256, ".png",
    arrayOf(
        "https://a.basemaps.cartocdn.com/rastertiles/voyager/",
        "https://b.basemaps.cartocdn.com/rastertiles/voyager/",
        "https://c.basemaps.cartocdn.com/rastertiles/voyager/",
        "https://d.basemaps.cartocdn.com/rastertiles/voyager/"
    ),
    "© OpenStreetMap contributors © CARTO"
)

/**
 * Bản đồ hiển thị tuyến đường di chuyển thực tế (OSRM Polyline) trên OpenStreetMap (Osmdroid).
 * 100% mượt mà, không giật lag, không blocking UI thread, an toàn lifecycle.
 */
@Composable
fun TripRouteMapView(
    trip: TripRoute,
    modifier: Modifier = Modifier
) {
    val mapRef = remember { AtomicReference<MapView?>(null) }

    fun applyTripOverlays(map: MapView, currentTrip: TripRoute) {
        try {
            map.overlays.clear()

            val geoPoints = if (currentTrip.pathPoints.isNotEmpty()) {
                currentTrip.pathPoints.map { GeoPoint(it.latitude, it.longitude) }
            } else {
                currentTrip.waypoints.filter { it.latitude != 0.0 && it.longitude != 0.0 }
                    .map { GeoPoint(it.latitude, it.longitude) }
            }

            // 1. Vẽ đường Polyline OSRM
            if (geoPoints.size >= 2) {
                val polyline = Polyline(map).apply {
                    setPoints(geoPoints)
                    outlinePaint.color = AndroidColor.parseColor("#0284C7") // Cyan / Ocean Blue
                    outlinePaint.strokeWidth = 10f
                    outlinePaint.isAntiAlias = true
                }
                map.overlays.add(polyline)

                // Zoom vừa vặn toàn bộ lộ trình an toàn sau khi map đã layout xong (width, height > 0)
                if (map.width > 0 && map.height > 0) {
                    var minLat = 90.0
                    var maxLat = -90.0
                    var minLon = 180.0
                    var maxLon = -180.0

                    geoPoints.forEach { pt ->
                        if (pt.latitude < minLat) minLat = pt.latitude
                        if (pt.latitude > maxLat) maxLat = pt.latitude
                        if (pt.longitude < minLon) minLon = pt.longitude
                        if (pt.longitude > maxLon) maxLon = pt.longitude
                    }

                    if (maxLat >= minLat && maxLon >= minLon) {
                        val latSpan = (maxLat - minLat).coerceAtLeast(0.02)
                        val lonSpan = (maxLon - minLon).coerceAtLeast(0.02)
                        val boundingBox = BoundingBox(
                            (maxLat + latSpan * 0.15).coerceIn(-85.0, 85.0),
                            (maxLon + lonSpan * 0.15).coerceIn(-180.0, 180.0),
                            (minLat - latSpan * 0.15).coerceIn(-85.0, 85.0),
                            (minLon - lonSpan * 0.15).coerceIn(-180.0, 180.0)
                        )
                        map.zoomToBoundingBox(boundingBox, false, 40)
                    }
                } else {
                    map.controller.setCenter(geoPoints.first())
                    map.controller.setZoom(10.0)
                }
            } else if (geoPoints.isNotEmpty()) {
                map.controller.setCenter(geoPoints.first())
                map.controller.setZoom(11.0)
            }

            // 2. Thêm Marker cho các trạm dừng
            currentTrip.waypoints.forEach { wp ->
                if (wp.latitude != 0.0 && wp.longitude != 0.0) {
                    val marker = Marker(map).apply {
                        position = GeoPoint(wp.latitude, wp.longitude)
                        title = wp.locationName
                        snippet = "${wp.type}: ${wp.temp}°C • ${wp.condition.labelVi} (${wp.time})"
                        setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_BOTTOM)
                    }
                    map.overlays.add(marker)
                }
            }

            map.invalidate()
        } catch (_: Exception) {
            // Không để lỗi vẽ bản đồ ảnh hưởng tới UI thread
        }
    }

    Box(
        modifier = modifier
            .clip(RoundedCornerShape(20.dp))
            .border(1.dp, Color(0x3338BDF8), RoundedCornerShape(20.dp))
    ) {
        AndroidView(
            modifier = Modifier.fillMaxSize(),
            factory = { ctx ->
                try {
                    val basePath = File(ctx.cacheDir, "osmdroid")
                    val tileCache = File(basePath, "tiles")
                    tileCache.mkdirs()
                    Configuration.getInstance().apply {
                        load(ctx, ctx.getSharedPreferences("${ctx.packageName}_osmdroid", Context.MODE_PRIVATE))
                        osmdroidBasePath = basePath
                        osmdroidTileCache = tileCache
                        userAgentValue = "UTH_Weather_Viewing_App_Group6/1.0 (contact: student@uth.edu.vn; Android Client; UTH Ho Chi Minh City)"
                        userAgentHttpHeader = "User-Agent"
                    }
                } catch (_: Exception) {}

                MapView(ctx).apply {
                    setUseDataConnection(true)
                    setTileSource(CARTO_VOYAGER_TILE_SOURCE)
                    setMultiTouchControls(true)
                    zoomController.setVisibility(CustomZoomButtonsController.Visibility.NEVER)
                    isTilesScaledToDpi = true
                    minZoomLevel = 4.0
                    maxZoomLevel = 19.0

                    val initialCenter = if (trip.pathPoints.isNotEmpty()) {
                        GeoPoint(trip.pathPoints.first().latitude, trip.pathPoints.first().longitude)
                    } else if (trip.waypoints.isNotEmpty() && trip.waypoints.first().latitude != 0.0) {
                        GeoPoint(trip.waypoints.first().latitude, trip.waypoints.first().longitude)
                    } else {
                        GeoPoint(10.7769, 106.7009)
                    }
                    controller.setZoom(10.0)
                    controller.setCenter(initialCenter)

                    onResume()
                    mapRef.set(this)

                    post {
                        if (isAttachedToWindow) {
                            applyTripOverlays(this, trip)
                        }
                    }
                }
            },
            update = { map ->
                mapRef.set(map)
                map.post {
                    if (map.isAttachedToWindow) {
                        applyTripOverlays(map, trip)
                    }
                }
            }
        )

        DisposableEffect(Unit) {
            onDispose {
                try {
                    val map = mapRef.getAndSet(null)
                    map?.onPause()
                    map?.onDetach()
                } catch (_: Exception) {}
            }
        }

        // Badge góc trên: OSRM Route Map
        Box(
            modifier = Modifier
                .align(Alignment.TopStart)
                .padding(12.dp)
                .clip(RoundedCornerShape(12.dp))
                .background(Color(0xCC0F172A))
                .border(1.dp, Color(0x440284C7), RoundedCornerShape(12.dp))
                .padding(horizontal = 10.dp, vertical = 6.dp)
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(8.dp)
                        .clip(CircleShape)
                        .background(Color(0xFF38BDF8))
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = "Bản đồ tuyến đường OSRM",
                    color = Color.White,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.SemiBold
                )
            }
        }

        // Các nút điều khiển Zoom / Recenter ở góc phải
        Box(
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(12.dp)
                .clip(RoundedCornerShape(14.dp))
                .background(Color(0xCC0F172A))
                .border(1.dp, Color(0x3394A3B8), RoundedCornerShape(14.dp))
        ) {
            Row(modifier = Modifier.padding(2.dp)) {
                IconButton(
                    onClick = {
                        mapRef.get()?.controller?.zoomIn()
                    },
                    modifier = Modifier.size(34.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Add,
                        contentDescription = "Zoom In",
                        tint = Color.White,
                        modifier = Modifier.size(18.dp)
                    )
                }

                IconButton(
                    onClick = {
                        mapRef.get()?.controller?.zoomOut()
                    },
                    modifier = Modifier.size(34.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Remove,
                        contentDescription = "Zoom Out",
                        tint = Color.White,
                        modifier = Modifier.size(18.dp)
                    )
                }

                IconButton(
                    onClick = {
                        mapRef.get()?.let { applyTripOverlays(it, trip) }
                    },
                    modifier = Modifier.size(34.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.CenterFocusStrong,
                        contentDescription = "Recenter",
                        tint = Color(0xFF38BDF8),
                        modifier = Modifier.size(18.dp)
                    )
                }
            }
        }
    }
}
