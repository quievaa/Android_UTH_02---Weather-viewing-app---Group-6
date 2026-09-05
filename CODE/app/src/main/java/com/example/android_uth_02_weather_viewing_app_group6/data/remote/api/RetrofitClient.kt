package com.example.android_uth_02_weather_viewing_app_group6.data.remote.api

import okhttp3.Dns
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.net.Inet4Address
import java.net.InetAddress
import java.util.concurrent.TimeUnit

object RetrofitClient {

    private val loggingInterceptor = HttpLoggingInterceptor().apply {
        level = HttpLoggingInterceptor.Level.BASIC
    }

    // Strict IPv4 DNS resolver to avoid IPv6 unreachable routing errors [2400:...]:443 on emulators/networks
    private val ipv4FirstDns = object : Dns {
        override fun lookup(hostname: String): List<InetAddress> {
            return try {
                val addresses = InetAddress.getAllByName(hostname).toList()
                val ipv4 = addresses.filterIsInstance<Inet4Address>()
                if (ipv4.isNotEmpty()) ipv4 else addresses
            } catch (e: Exception) {
                try {
                    val addresses = Dns.SYSTEM.lookup(hostname)
                    val ipv4 = addresses.filterIsInstance<Inet4Address>()
                    if (ipv4.isNotEmpty()) ipv4 else addresses
                } catch (e2: Exception) {
                    Dns.SYSTEM.lookup(hostname)
                }
            }
        }
    }

    val okHttpClient: OkHttpClient = OkHttpClient.Builder()
        .dns(ipv4FirstDns)
        .retryOnConnectionFailure(true)
        .addInterceptor(loggingInterceptor)
        .connectTimeout(15, TimeUnit.SECONDS)
        .readTimeout(15, TimeUnit.SECONDS)
        .writeTimeout(15, TimeUnit.SECONDS)
        .build()

    // 1. Open-Meteo
    val weatherApi: WeatherApiService by lazy {
        Retrofit.Builder()
            .baseUrl("https://api.open-meteo.com/")
            .client(okHttpClient)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(WeatherApiService::class.java)
    }

    // Geocoding for Open-Meteo
    val geocodingApi: GeocodingApiService by lazy {
        Retrofit.Builder()
            .baseUrl("https://geocoding-api.open-meteo.com/")
            .client(okHttpClient)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(GeocodingApiService::class.java)
    }

    // 2. OpenWeatherMap
    val openWeatherApi: OpenWeatherMapApiService by lazy {
        Retrofit.Builder()
            .baseUrl("https://api.openweathermap.org/")
            .client(okHttpClient)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(OpenWeatherMapApiService::class.java)
    }

    // 3. WeatherAPI.com
    val weatherApiDotCom: WeatherApiDotComService by lazy {
        Retrofit.Builder()
            .baseUrl("https://api.weatherapi.com/")
            .client(okHttpClient)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(WeatherApiDotComService::class.java)
    }
}
