package com.example.android_uth_02_weather_viewing_app_group6

import com.example.android_uth_02_weather_viewing_app_group6.data.location.LocationTracker
import com.example.android_uth_02_weather_viewing_app_group6.data.remote.api.GeocodingApiService
import com.example.android_uth_02_weather_viewing_app_group6.data.remote.api.WeatherApiService
import com.example.android_uth_02_weather_viewing_app_group6.data.remote.model.CurrentDto
import com.example.android_uth_02_weather_viewing_app_group6.data.remote.model.GeocodingResponse
import com.example.android_uth_02_weather_viewing_app_group6.data.remote.model.GeocodingResult
import com.example.android_uth_02_weather_viewing_app_group6.data.remote.model.OpenMeteoWeatherResponse
import com.example.android_uth_02_weather_viewing_app_group6.data.repository.WeatherRepository
import com.example.android_uth_02_weather_viewing_app_group6.domain.model.CurrentWeather
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
            hourly: String,
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
        com.example.android_uth_02_weather_viewing_app_group6.domain.ai.AiWeatherAssistantEngine.globalEnableOnlineGemini = false
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
        com.example.android_uth_02_weather_viewing_app_group6.domain.ai.AiWeatherAssistantEngine.globalEnableOnlineGemini = true
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
    fun testWeatherViewModel_loadCurrentWeather_success() = runTest {
        val repository = WeatherRepository(fakeWeatherApi, fakeGeocodingApi)
        val viewModel = WeatherViewModel(repository)

        viewModel.loadCurrentWeather("Ho Chi Minh")
        testDispatcher.scheduler.advanceUntilIdle()

        val state = viewModel.uiState.value
        assertTrue(state is WeatherUiState.Success)
        assertEquals("Ho Chi Minh", (state as WeatherUiState.Success).weather.cityName)
    }

    @Test
    fun testWeatherViewModel_retry_updatesState() = runTest {
        val repository = WeatherRepository(fakeWeatherApi, fakeGeocodingApi)
        val viewModel = WeatherViewModel(repository)

        // First load
        viewModel.loadCurrentWeather("Ha Noi")
        testDispatcher.scheduler.advanceUntilIdle()

        // Trigger retry
        viewModel.retry()
        testDispatcher.scheduler.advanceUntilIdle()

        val state = viewModel.uiState.value
        assertTrue(state is WeatherUiState.Success)
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

    @Test
    fun testFloodRiskEvaluator_hcmSevereRain_triggersSevereAlert() {
        val severeWeather = CurrentWeather(
            cityName = "TP. Hồ Chí Minh",
            temperatureC = 26.0,
            feelsLikeC = 27.0,
            minTemperatureC = 24.0,
            maxTemperatureC = 28.0,
            description = "Mưa dông bão sấm sét",
            weatherMain = "Thunderstorm",
            humidityPercent = 95,
            pressureHpa = 1005.0,
            windSpeedMps = 8.5,
            windDirectionDeg = 180,
            latitude = 10.82,
            longitude = 106.63,
            iconCode = "11d"
        )

        val report = com.example.android_uth_02_weather_viewing_app_group6.data.location.FloodRiskEvaluator.evaluate(
            severeWeather,
            "TP. Hồ Chí Minh"
        )

        assertTrue(report.hasAlert)
        assertEquals(com.example.android_uth_02_weather_viewing_app_group6.ui.model.FloodAlertLevel.SEVERE, report.level)
        assertTrue(report.highRiskStreets.any { it.streetName.contains("Ung Văn Khiêm") })
        assertTrue(report.highRiskStreets.any { it.isNearUthCampus })
    }

    @Test
    fun testFloodRiskEvaluator_sunnyWeather_returnsSafe() {
        val sunnyWeather = CurrentWeather(
            cityName = "Đà Lạt",
            temperatureC = 22.0,
            feelsLikeC = 22.0,
            minTemperatureC = 16.0,
            maxTemperatureC = 24.0,
            description = "Trời quang nắng ấm",
            weatherMain = "Clear",
            humidityPercent = 60,
            pressureHpa = 1015.0,
            windSpeedMps = 2.0,
            windDirectionDeg = 90,
            latitude = 11.94,
            longitude = 108.45,
            iconCode = "01d"
        )

        val report = com.example.android_uth_02_weather_viewing_app_group6.data.location.FloodRiskEvaluator.evaluate(
            sunnyWeather,
            "Đà Lạt"
        )

        assertFalse(report.hasAlert)
        assertEquals(com.example.android_uth_02_weather_viewing_app_group6.ui.model.FloodAlertLevel.SAFE, report.level)
        assertTrue(report.highRiskStreets.isEmpty())
    }

    @Test
    fun testTripRoutingEngine_uthCampuses_resolvesCoordinates() = runTest {
        val fakeOsrmApi = object : com.example.android_uth_02_weather_viewing_app_group6.data.remote.api.OsrmApiService {
            override suspend fun getDrivingRoute(
                coordinates: String,
                overview: String,
                geometries: String,
                steps: Boolean
            ): retrofit2.Response<com.example.android_uth_02_weather_viewing_app_group6.data.remote.model.OsrmRouteResponse> {
                return retrofit2.Response.success(
                    com.example.android_uth_02_weather_viewing_app_group6.data.remote.model.OsrmRouteResponse(
                        code = "Ok",
                        routes = listOf(
                            com.example.android_uth_02_weather_viewing_app_group6.data.remote.model.OsrmRoute(
                                distance = 14500.0,
                                duration = 1800.0,
                                geometry = com.example.android_uth_02_weather_viewing_app_group6.data.remote.model.OsrmGeometry(
                                    coordinates = listOf(
                                        listOf(106.7135, 10.8037),
                                        listOf(106.6346, 10.8690)
                                    ),
                                    type = "LineString"
                                ),
                                legs = null
                            )
                        ),
                        waypoints = null
                    )
                )
            }
        }

        val engine = com.example.android_uth_02_weather_viewing_app_group6.data.location.TripRoutingEngine(
            osrmApi = fakeOsrmApi,
            geocodingApi = fakeGeocodingApi
        )

        val cs1 = engine.resolveCoordinates("UTH Cơ sở 1")
        val cs2 = engine.resolveCoordinates("UTH Cơ sở 2")
        val cs3 = engine.resolveCoordinates("UTH Cơ sở 3")

        assertEquals(10.8037, cs1?.latitude ?: 0.0, 0.001)
        assertEquals(10.8690, cs2?.latitude ?: 0.0, 0.001)
        assertEquals(10.7745, cs3?.latitude ?: 0.0, 0.001)

        val routeResult = engine.calculateRoute(
            originName = "UTH Cơ sở 1",
            destinationName = "UTH Cơ sở 2",
            vehicleType = "Xe máy"
        )
        assertTrue(routeResult.isSuccess)
        val route = routeResult.getOrNull()
        assertEquals(15, route?.distanceKm) // 14.5km rounded to 15km
        assertEquals("Xe máy", route?.vehicleType)
        assertTrue(route?.drivingAdvice?.contains("Xe máy") == true)
        assertTrue(route?.waypoints?.isNotEmpty() == true)
    }

    @Test
    fun testTripRoutingEngine_vehicleSpecificAdvice_changesByVehicle() {
        val engine = com.example.android_uth_02_weather_viewing_app_group6.data.location.TripRoutingEngine(
            osrmApi = object : com.example.android_uth_02_weather_viewing_app_group6.data.remote.api.OsrmApiService {
                override suspend fun getDrivingRoute(coordinates: String, overview: String, geometries: String, steps: Boolean) =
                    retrofit2.Response.success(com.example.android_uth_02_weather_viewing_app_group6.data.remote.model.OsrmRouteResponse("Ok", null, null))
            },
            geocodingApi = fakeGeocodingApi
        )

        val bikeAdvice = engine.buildDrivingAdvice("Xe máy", hasSevereWarning = true, distanceKm = 100, durationText = "2 giờ")
        val carAdvice = engine.buildDrivingAdvice("Ô tô", hasSevereWarning = true, distanceKm = 100, durationText = "2 giờ")

        assertTrue(bikeAdvice.contains("áo mưa bộ") || bikeAdvice.contains("Xe máy"))
        assertTrue(carAdvice.contains("trượt nước") || carAdvice.contains("aquaplaning") || carAdvice.contains("Ô tô"))
    }

    @Test
    fun testTripRoutingEngine_myLocation_resolvesSuccessfully() = runTest {
        val fakeOsrmApi = object : com.example.android_uth_02_weather_viewing_app_group6.data.remote.api.OsrmApiService {
            override suspend fun getDrivingRoute(coordinates: String, overview: String, geometries: String, steps: Boolean) =
                retrofit2.Response.success(
                    com.example.android_uth_02_weather_viewing_app_group6.data.remote.model.OsrmRouteResponse(
                        code = "Ok",
                        routes = listOf(
                            com.example.android_uth_02_weather_viewing_app_group6.data.remote.model.OsrmRoute(
                                distance = 300000.0,
                                duration = 21600.0,
                                geometry = com.example.android_uth_02_weather_viewing_app_group6.data.remote.model.OsrmGeometry(
                                    coordinates = listOf(listOf(106.6297, 10.8231), listOf(108.4583, 11.9404)),
                                    type = "LineString"
                                ),
                                legs = null
                            )
                        ),
                        waypoints = null
                    )
                )
        }

        val engine = com.example.android_uth_02_weather_viewing_app_group6.data.location.TripRoutingEngine(
            osrmApi = fakeOsrmApi,
            geocodingApi = fakeGeocodingApi
        )

        // 1. When userLocation is passed explicitly
        val customLoc = com.example.android_uth_02_weather_viewing_app_group6.ui.model.RouteGeoPoint(10.75, 106.66)
        val resolvedWithLoc = engine.resolveCoordinates("Vị trí của tôi", customLoc)
        assertEquals(10.75, resolvedWithLoc?.latitude ?: 0.0, 0.001)

        // 2. When userLocation is null (fallback)
        val resolvedWithoutLoc = engine.resolveCoordinates("Vị trí của tôi", null)
        assertTrue("Should fallback instead of null", resolvedWithoutLoc != null)
        assertEquals(10.8231, resolvedWithoutLoc?.latitude ?: 0.0, 0.001)

        // 3. Full calculateRoute with "Vị trí của tôi" -> "Đà Lạt"
        val routeResult = engine.calculateRoute(
            originName = "Vị trí của tôi",
            destinationName = "Đà Lạt",
            vehicleType = "Ô tô",
            userLocation = customLoc
        )
        assertTrue("Route calculation with 'Vị trí của tôi' must succeed", routeResult.isSuccess)
        val route = routeResult.getOrNull()
        assertEquals("Vị trí của tôi", route?.origin)
        assertEquals("Đà Lạt", route?.destination)
        assertEquals(300, route?.distanceKm)
    }

    @Test
    fun testAiWeatherAssistantEngine_answersIntelligently() {
        val engine = com.example.android_uth_02_weather_viewing_app_group6.domain.ai.AiWeatherAssistantEngine()
        val mockWeather = com.example.android_uth_02_weather_viewing_app_group6.domain.model.CurrentWeather(
            cityName = "TP. Hồ Chí Minh",
            temperatureC = 31.0,
            feelsLikeC = 34.0,
            minTemperatureC = 26.0,
            maxTemperatureC = 33.0,
            description = "Mưa rào rải rác",
            weatherMain = "Rain",
            humidityPercent = 85,
            pressureHpa = 1010.0,
            windSpeedMps = 5.0,
            windDirectionDeg = 180,
            latitude = 10.8231,
            longitude = 106.6297,
            iconCode = "10d",
            hourlyForecast = emptyList(),
            dailyForecast = emptyList()
        )

        // 1. Rain question
        val rainAnswer = engine.generateResponse("Hôm nay có mưa không?", mockWeather, null)
        assertTrue(rainAnswer.contains("mưa", ignoreCase = true))

        // 2. Clothing advice
        val outfitAnswer = engine.generateResponse("Nên mặc gì ra ngoài?", mockWeather, null)
        assertTrue(outfitAnswer.contains("trang phục", ignoreCase = true) || outfitAnswer.contains("mặc", ignoreCase = true))

        // 3. Campus & flood check
        val floodAnswer = engine.generateResponse("Tuyến đường cơ sở UTH Bình Thạnh có bị ngập không?", mockWeather, null)
        assertTrue(floodAnswer.contains("UTH", ignoreCase = true) && floodAnswer.contains("ngập", ignoreCase = true))

        // 4. Outdoor sports
        val sportAnswer = engine.generateResponse("Có nên tập thể thao ngoài trời không?", mockWeather, null)
        assertTrue(sportAnswer.contains("thể thao", ignoreCase = true) || sportAnswer.contains("ngoài trời", ignoreCase = true))
    }

    @Test
    fun testWeatherViewModel_aiChatInteraction() = runTest {
        val repository = WeatherRepository(fakeWeatherApi, fakeGeocodingApi)
        val viewModel = WeatherViewModel(repository)

        assertEquals(1, viewModel.chatMessages.value.size) // Welcome message
        viewModel.sendChatMessage("Hôm nay nên mặc gì?")

        // Initially thinking should be true, then complete after scheduler advances
        assertEquals(2, viewModel.chatMessages.value.size) // Welcome + User query
        testDispatcher.scheduler.advanceUntilIdle()

        // After completion: Welcome + User query + AI reply
        assertEquals(3, viewModel.chatMessages.value.size)
        assertFalse(viewModel.isAiThinking.value)
        assertFalse(viewModel.chatMessages.value.last().isUser)

        // Test clearChat
        viewModel.clearChat()
        assertEquals(1, viewModel.chatMessages.value.size)
    }
}
