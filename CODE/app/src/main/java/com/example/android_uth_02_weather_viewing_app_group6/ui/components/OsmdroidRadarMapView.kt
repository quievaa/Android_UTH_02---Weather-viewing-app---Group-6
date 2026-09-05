package com.example.android_uth_02_weather_viewing_app_group6.ui.components

import android.content.Context
import android.graphics.Color as AndroidColor
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.viewinterop.AndroidView
import com.example.android_uth_02_weather_viewing_app_group6.ui.model.RadarLayer
import org.osmdroid.config.Configuration
import org.osmdroid.tileprovider.MapTileProviderBasic
import org.osmdroid.tileprovider.tilesource.OnlineTileSourceBase
import org.osmdroid.tileprovider.tilesource.TileSourceFactory
import org.osmdroid.tileprovider.tilesource.XYTileSource
import org.osmdroid.util.GeoPoint
import org.osmdroid.util.MapTileIndex
import org.osmdroid.views.CustomZoomButtonsController
import org.osmdroid.views.MapView
import org.osmdroid.views.overlay.Marker
import org.osmdroid.views.overlay.TilesOverlay
import java.io.File

val OPENWEATHER_BASE_MAP_SOURCE = XYTileSource(
    "OSMHot",
    0, 19, 256, ".png",
    arrayOf(
        "https://a.tile.openstreetmap.fr/hot/",
        "https://b.tile.openstreetmap.fr/hot/",
        "https://c.tile.openstreetmap.fr/hot/"
    ),
    "© OpenStreetMap contributors"
)

@Composable
fun OsmdroidRadarMapView(
    latitude: Double,
    longitude: Double,
    cityName: String,
    selectedLayer: RadarLayer,
    zoomLevel: Float,
    apiKey: String = "8aa0ba046904f17e164ed03a89638b37",
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    var mapViewRef by remember { mutableStateOf<MapView?>(null) }
    var weatherOverlayRef by remember { mutableStateOf<TilesOverlay?>(null) }
    var cityMarkerRef by remember { mutableStateOf<Marker?>(null) }

    fun ensureOsmdroidConfig(ctx: Context) {
        val basePath = File(ctx.cacheDir, "osmdroid")
        val tileCache = File(basePath, "tiles")
        tileCache.mkdirs()

        val prefs = ctx.getSharedPreferences("${ctx.packageName}_osmdroid", Context.MODE_PRIVATE)
        Configuration.getInstance().apply {
            load(ctx, prefs)
            osmdroidBasePath = basePath
            osmdroidTileCache = tileCache
            userAgentValue = ctx.packageName
            userAgentHttpHeader = "User-Agent"
        }
    }

    fun createWeatherTileSource(layer: RadarLayer, currentApiKey: String): OnlineTileSourceBase {
        val layerCode = when (layer) {
            RadarLayer.PRECIPITATION -> "precipitation_new"
            RadarLayer.CLOUDS -> "clouds_new"
            RadarLayer.WIND -> "wind_new"
            RadarLayer.TEMPERATURE -> "temp_new"
        }

        return object : OnlineTileSourceBase(
            "OWM-$layerCode",
            0, 18, 256, ".png",
            arrayOf("https://tile.openweathermap.org/map/$layerCode/")
        ) {
            override fun getTileURLString(pMapTileIndex: Long): String {
                val z = MapTileIndex.getZoom(pMapTileIndex)
                val x = MapTileIndex.getX(pMapTileIndex)
                val y = MapTileIndex.getY(pMapTileIndex)
                return "https://tile.openweathermap.org/map/$layerCode/$z/$x/$y.png?appid=$currentApiKey"
            }
        }
    }

    LaunchedEffect(latitude, longitude, zoomLevel) {
        val map = mapViewRef
        if (map != null) {
            val geoPoint = GeoPoint(latitude, longitude)
            map.controller.animateTo(geoPoint, zoomLevel.toDouble().coerceIn(4.0, 18.0), 800L)

            val marker = cityMarkerRef
            if (marker != null) {
                marker.position = geoPoint
                marker.title = cityName
                marker.snippet = "Tọa độ: %.4f, %.4f".format(latitude, longitude)
                marker.showInfoWindow()
            }
        }
    }

    LaunchedEffect(selectedLayer, apiKey) {
        val map = mapViewRef
        if (map != null) {
            val oldOverlay = weatherOverlayRef
            if (oldOverlay != null) {
                try {
                    map.overlays.remove(oldOverlay)
                    oldOverlay.onDetach(map)
                } catch (_: Exception) {}
            }

            val tileSource = createWeatherTileSource(selectedLayer, apiKey)
            val tileProvider = MapTileProviderBasic(context, tileSource).apply {
                setTileRequestCompleteHandler(map.tileRequestCompleteHandler)
            }
            val newOverlay = TilesOverlay(tileProvider, context).apply {
                loadingBackgroundColor = AndroidColor.TRANSPARENT
                loadingLineColor = AndroidColor.TRANSPARENT
            }

            map.overlays.add(0, newOverlay)
            weatherOverlayRef = newOverlay
            map.invalidate()
        }
    }

    Box(modifier = modifier.fillMaxSize()) {
        AndroidView(
            modifier = Modifier.fillMaxSize(),
            factory = { ctx ->
                ensureOsmdroidConfig(ctx)

                MapView(ctx).apply {
                    setUseDataConnection(true)
                    setTileSource(OPENWEATHER_BASE_MAP_SOURCE)
                    setMultiTouchControls(true)
                    isTilesScaledToDpi = true
                    zoomController.setVisibility(CustomZoomButtonsController.Visibility.NEVER)
                    minZoomLevel = 3.0
                    maxZoomLevel = 19.0

                    val centerPoint = GeoPoint(latitude, longitude)
                    controller.setZoom(zoomLevel.toDouble().coerceIn(4.0, 18.0))
                    controller.setCenter(centerPoint)

                    val tileSource = createWeatherTileSource(selectedLayer, apiKey)
                    val currentMap = this
                    val tileProvider = MapTileProviderBasic(ctx, tileSource).apply {
                        setTileRequestCompleteHandler(currentMap.tileRequestCompleteHandler)
                    }
                    val weatherOverlay = TilesOverlay(tileProvider, ctx).apply {
                        loadingBackgroundColor = AndroidColor.TRANSPARENT
                        loadingLineColor = AndroidColor.TRANSPARENT
                    }
                    overlays.add(weatherOverlay)
                    weatherOverlayRef = weatherOverlay

                    val marker = Marker(this).apply {
                        position = centerPoint
                        title = cityName
                        snippet = "Tọa độ: %.4f, %.4f".format(latitude, longitude)
                        setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_BOTTOM)
                        showInfoWindow()
                    }
                    overlays.add(marker)
                    cityMarkerRef = marker

                    onResume()
                    mapViewRef = this
                }
            },
            update = { map ->
                mapViewRef = map
            }
        )
    }

    DisposableEffect(Unit) {
        onDispose {
            try {
                val map = mapViewRef
                if (map != null) {
                    weatherOverlayRef?.onDetach(map)
                    map.onPause()
                    map.onDetach()
                }
            } catch (_: Exception) {}
            mapViewRef = null
            weatherOverlayRef = null
            cityMarkerRef = null
        }
    }
}
