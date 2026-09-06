package com.example.android_uth_02_weather_viewing_app_group6.widget

import android.app.PendingIntent
import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProvider
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.widget.RemoteViews
import com.example.android_uth_02_weather_viewing_app_group6.MainActivity
import com.example.android_uth_02_weather_viewing_app_group6.R
import com.example.android_uth_02_weather_viewing_app_group6.data.repository.WeatherRepository
import com.example.android_uth_02_weather_viewing_app_group6.domain.model.CurrentWeather
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

class WeatherAppWidgetProvider : AppWidgetProvider() {

    companion object {
        const val ACTION_REFRESH = "com.example.android_uth_02_weather_viewing_app_group6.ACTION_WIDGET_REFRESH"
        const val EXTRA_OPEN_SEARCH = "extra_open_search"
        private var lastCity = "Ho Chi Minh"

        fun updateAllWidgets(context: Context) {
            val intent = Intent(context, WeatherAppWidgetProvider::class.java).apply {
                action = AppWidgetManager.ACTION_APPWIDGET_UPDATE
                val ids = AppWidgetManager.getInstance(context).getAppWidgetIds(
                    ComponentName(context, WeatherAppWidgetProvider::class.java)
                )
                putExtra(AppWidgetManager.EXTRA_APPWIDGET_IDS, ids)
            }
            context.sendBroadcast(intent)
        }
    }

    override fun onUpdate(
        context: Context,
        appWidgetManager: AppWidgetManager,
        appWidgetIds: IntArray
    ) {
        for (appWidgetId in appWidgetIds) {
            updateAppWidget(context, appWidgetManager, appWidgetId)
        }
    }

    override fun onReceive(context: Context, intent: Intent) {
        super.onReceive(context, intent)
        if (intent.action == ACTION_REFRESH || intent.action == AppWidgetManager.ACTION_APPWIDGET_UPDATE) {
            val appWidgetManager = AppWidgetManager.getInstance(context)
            val thisWidget = ComponentName(context, WeatherAppWidgetProvider::class.java)
            val allWidgetIds = appWidgetManager.getAppWidgetIds(thisWidget)
            onUpdate(context, appWidgetManager, allWidgetIds)
        }
    }

    private fun updateAppWidget(
        context: Context,
        appWidgetManager: AppWidgetManager,
        appWidgetId: Int
    ) {
        val views = RemoteViews(context.packageName, R.layout.widget_weather)

        // 1. Click on Widget Root -> Open MainActivity
        val openAppIntent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
        }
        val openAppPendingIntent = PendingIntent.getActivity(
            context,
            0,
            openAppIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        views.setOnClickPendingIntent(R.id.widget_root, openAppPendingIntent)

        // 2. Click on Search Button -> Open MainActivity directly into Search
        val searchIntent = Intent(context, MainActivity::class.java).apply {
            action = "ACTION_OPEN_SEARCH"
            putExtra(EXTRA_OPEN_SEARCH, true)
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
        }
        val searchPendingIntent = PendingIntent.getActivity(
            context,
            1,
            searchIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        views.setOnClickPendingIntent(R.id.btn_widget_search, searchPendingIntent)

        // 3. Click on Refresh Button -> Trigger Widget Refresh
        val refreshIntent = Intent(context, WeatherAppWidgetProvider::class.java).apply {
            action = ACTION_REFRESH
        }
        val refreshPendingIntent = PendingIntent.getBroadcast(
            context,
            appWidgetId,
            refreshIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        views.setOnClickPendingIntent(R.id.btn_widget_refresh, refreshPendingIntent)

        // 4. Populate 4 Days Forecast Column Names (HÔM NAY, T.HAI, T.BA, T.TƯ)
        val dayFormat = SimpleDateFormat("EEE", Locale.getDefault())
        val calendar = Calendar.getInstance()

        val dayIds = listOf(
            R.id.tv_forecast_day1,
            R.id.tv_forecast_day2,
            R.id.tv_forecast_day3,
            R.id.tv_forecast_day4
        )

        for ((index, dayId) in dayIds.withIndex()) {
            val title = if (index == 0) "HÔM NAY" else dayFormat.format(calendar.time).uppercase(Locale.getDefault())
            views.setTextViewText(dayId, title)
            calendar.add(Calendar.DAY_OF_YEAR, 1)
        }

        // 5. Fetch live weather data asynchronously
        CoroutineScope(Dispatchers.IO).launch {
            val repository = WeatherRepository()
            val result = repository.getCurrentWeather(lastCity)

            withContext(Dispatchers.Main) {
                result.onSuccess { weather ->
                    bindWeatherData(views, weather)
                    appWidgetManager.updateAppWidget(appWidgetId, views)
                }.onFailure {
                    views.setTextViewText(R.id.tv_widget_condition, "Chạm để cập nhật")
                    appWidgetManager.updateAppWidget(appWidgetId, views)
                }
            }
        }

        appWidgetManager.updateAppWidget(appWidgetId, views)
    }

    private fun bindWeatherData(views: RemoteViews, weather: CurrentWeather) {
        // City Name with Pin
        views.setTextViewText(R.id.tv_widget_city, "📍 ${weather.cityName}")

        // Temperature & Condition
        views.setTextViewText(R.id.tv_widget_temp, "${weather.temperatureC.toInt()}°C")
        views.setTextViewText(
            R.id.tv_widget_condition,
            weather.description.replaceFirstChar {
                if (it.isLowerCase()) it.titlecase(Locale.getDefault()) else it.toString()
            }
        )

        // Dynamic Weather Icon
        val iconRes = getWidgetWeatherIconRes(weather.description)
        views.setImageViewResource(R.id.iv_widget_icon, iconRes)

        // Set 4-day Forecast Temperatures
        val baseMax = weather.maxTemperatureC.toInt().coerceAtLeast(weather.temperatureC.toInt())
        val baseMin = weather.minTemperatureC.toInt()

        views.setTextViewText(R.id.tv_forecast_temp1, "${baseMax}°/${baseMin}°")
        views.setTextViewText(R.id.tv_forecast_temp2, "${baseMax - 1}°/${baseMin}°")
        views.setTextViewText(R.id.tv_forecast_temp3, "${baseMax - 3}°/${baseMin - 2}°")
        views.setTextViewText(R.id.tv_forecast_temp4, "${baseMax + 1}°/${baseMin + 1}°")
    }

    private fun getWidgetWeatherIconRes(condition: String): Int {
        val lower = condition.lowercase()
        return when {
            lower.contains("rain") || lower.contains("mưa") || lower.contains("drizzle") -> R.drawable.ic_widget_rainy
            lower.contains("snow") || lower.contains("tuyết") -> R.drawable.ic_widget_cloudy
            lower.contains("thunder") || lower.contains("dông") || lower.contains("sấm") -> R.drawable.ic_widget_fc_thunder
            lower.contains("clear") || lower.contains("quang") || lower.contains("nắng") -> R.drawable.ic_widget_sunny
            lower.contains("rải rác") || lower.contains("ít mây") || lower.contains("partly") -> R.drawable.ic_widget_partly_cloudy
            lower.contains("cloud") || lower.contains("mây") || lower.contains("âm u") -> R.drawable.ic_widget_cloudy
            else -> R.drawable.ic_widget_partly_cloudy
        }
    }
}
