package com.example.android_uth_02_weather_viewing_app_group6

import com.example.android_uth_02_weather_viewing_app_group6.data.location.LocationTracker
import com.example.android_uth_02_weather_viewing_app_group6.data.remote.api.GeocodingApiService
import com.example.android_uth_02_weather_viewing_app_group6.data.remote.api.WeatherApiService
import com.example.android_uth_02_weather_viewing_app_group6.data.remote.model.CurrentDto
import com.example.android_uth_02_weather_viewing_app_group6.data.remote.model.GeocodingResponse
import com.example.android_uth_02_weather_viewing_app_group6.data.remote.model.GeocodingResult
import com.example.android_uth_02_weather_viewing_app_group6.data.remote.model.OpenMeteoWeatherResponse
import com.example.android_uth_02_weather_viewing_app_group6.data.repository.WeatherRepository
import com.example.android_uth_02_weather_viewing_app_group6.ui.viewmodel.WeatherUiState
import com.example.android_uth_02_weather_viewing_app_group6.ui.viewmodel.WeatherViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import retrofit2.Response

@OptIn(ExperimentalCoroutinesApi::class)
class LocationAndSearchUnitTest {

    private val testDispatcher = StandardTestDispatcher()

    private val mockWeatherResponse = OpenMeteoWeatherResponse(
        latitude = 10.76,
        longitude = 106.66,
        timezone = "Asia/Ho_Chi_Minh",
        current = CurrentDto(
            time = "2026-08-24T10:00",
            temperature = 30.0,
            humidity = 70,
            apparentTemperature = 32.0,
            weatherCode = 0,
            windSpeed = 3.5,
            windDirection = 180,
            pressure = 1012.0,
        ),
        currentUnits = null,
        daily = null,
    )

    private val fakeWeatherApi = object : WeatherApiService {
        override suspend fun getCurrentWeather(
            latitude: Double,
            longitude: Double,
            current: String,
            daily: String,
            forecastDays: Int,
            timezone: String,
        ): Response<OpenMeteoWeatherResponse> {
            return Response.success(mockWeatherResponse)
        }
    }

    private val fakeGeocodingApi = object : GeocodingApiService {
        override suspend fun searchCity(
            name: String,
            count: Int,
            language: String,
            format: String,
        ): Response<GeocodingResponse> {
            return if (name == "UnknownCity") {
                Response.success(GeocodingResponse(emptyList()))
            } else {
                Response.success(
                    GeocodingResponse(
                        listOf(
                            GeocodingResult(
                                name = name,
                                latitude = 10.76,
                                longitude = 106.66,
                                country = "VN",
                                admin1 = null,
                            )
                        )
                    )
                )
            }
        }
    }

    @Before
    fun setUp() {
        Dispatchers.setMain(testDispatcher)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun testWeatherRepository_getWeatherByCoordinates_success() = runTest {
        val repository = WeatherRepository(fakeWeatherApi, fakeGeocodingApi)
        val result = repository.getWeatherByCoordinates(10.76, 106.66, "Vị trí GPS của tôi")

        assertTrue(result.isSuccess)
        val weather = result.getOrNull()
        assertEquals("Vị trí GPS của tôi", weather?.cityName)
        assertEquals(30.0, weather?.temperatureC ?: 0.0, 0.01)
    }

    @Test
    fun testWeatherRepository_unknownCity_returnsFailure() = runTest {
        val repository = WeatherRepository(fakeWeatherApi, fakeGeocodingApi)
        val result = repository.getCurrentWeather("UnknownCity")

        assertTrue(result.isFailure)
        assertTrue(result.exceptionOrNull() is IllegalArgumentException)
    }

    @Test
    fun testWeatherViewModel_blankCityName_returnsErrorState() = runTest {
        val repository = WeatherRepository(fakeWeatherApi, fakeGeocodingApi)
        val viewModel = WeatherViewModel(repository)

        viewModel.loadCurrentWeather("   ")
        val state = viewModel.uiState.value
        assertTrue(state is WeatherUiState.Error)
        assertEquals("Vui lòng nhập tên thành phố.", (state as WeatherUiState.Error).message)
    }

    @Test
    fun testWeatherViewModel_fetchLocationWeather_handlesNullLocation() = runTest {
        val repository = WeatherRepository(fakeWeatherApi, fakeGeocodingApi)
        val viewModel = WeatherViewModel(repository)

        val nullLocationTracker = object : LocationTracker {
            override suspend fun getCurrentLocation() = null
        }

        var resultSuccess = false
        var resultErrorMsg: String? = null

        viewModel.fetchLocationWeather(nullLocationTracker) { success, msg ->
            resultSuccess = success
            resultErrorMsg = msg
        }
        testDispatcher.scheduler.advanceUntilIdle()

        assertEquals(false, resultSuccess)
        assertTrue(resultErrorMsg?.contains("Không thể lấy vị trí GPS") == true)
    }

    @Test
    fun testWeatherViewModel_unitFormatting() = runTest {
        val repository = WeatherRepository(fakeWeatherApi, fakeGeocodingApi)
        val viewModel = WeatherViewModel(repository)

        // Default C
        assertEquals("30°C", viewModel.formatTemperature(30.0))
        assertEquals("3.5 m/s", viewModel.formatWindSpeed(3.5))

        // Toggle to F
        viewModel.toggleTemperatureUnit()
        assertFalse(viewModel.isCelsius.value)
        assertEquals("86°F", viewModel.formatTemperature(30.0))

        // Toggle to km/h
        viewModel.updateWindUnit("km/h")
        assertEquals("km/h", viewModel.windUnit.value)
        assertEquals("12.6 km/h", viewModel.formatWindSpeed(3.5))
    }
}
