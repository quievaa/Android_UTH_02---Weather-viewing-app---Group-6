package com.example.android_uth_02_weather_viewing_app_group6

import com.example.android_uth_02_weather_viewing_app_group6.data.remote.api.GeminiApiService
import com.example.android_uth_02_weather_viewing_app_group6.data.remote.api.GeminiCandidate
import com.example.android_uth_02_weather_viewing_app_group6.data.remote.api.GeminiCandidateContent
import com.example.android_uth_02_weather_viewing_app_group6.data.remote.api.GeminiPart
import com.example.android_uth_02_weather_viewing_app_group6.data.remote.api.GeminiRequest
import com.example.android_uth_02_weather_viewing_app_group6.data.remote.api.GeminiResponse
import com.example.android_uth_02_weather_viewing_app_group6.domain.ai.AiWeatherAssistantEngine
import com.example.android_uth_02_weather_viewing_app_group6.domain.ai.VietnameseTextNormalizer
import com.example.android_uth_02_weather_viewing_app_group6.domain.ai.WeatherIntent
import com.example.android_uth_02_weather_viewing_app_group6.domain.ai.WeatherNlpClassifier
import com.example.android_uth_02_weather_viewing_app_group6.domain.model.CurrentWeather
import com.example.android_uth_02_weather_viewing_app_group6.ui.model.HourlyForecast
import com.example.android_uth_02_weather_viewing_app_group6.ui.model.WeatherCondition
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import retrofit2.Response


class WeatherAiTrainingAndNlpUnitTest {

    private lateinit var classifier: WeatherNlpClassifier
    private lateinit var engine: AiWeatherAssistantEngine

    @Before
    fun setUp() {
        classifier = WeatherNlpClassifier()
        engine = AiWeatherAssistantEngine(customKnowledgeRepo = null)
    }

    @Test
    fun testVietnameseTextNormalizer_removeAccents() {
        val accented = "Hôm nay trời có mưa không?"
        val unaccented = VietnameseTextNormalizer.removeAccents(accented)
        assertEquals("Hom nay troi co mua khong?", unaccented)

        val vietnameseChars = "Đường Ung Văn Khiêm ngập úng"
        val cleaned = VietnameseTextNormalizer.clean(vietnameseChars)
        val result = VietnameseTextNormalizer.removeAccents(cleaned)
        assertEquals("duong ung van khiem ngap ung", result)
    }

    @Test
    fun testVietnameseTextNormalizer_slangExpansion() {
        val textWithSlang = "troi mua ko di dc hnay uth cs1"
        val expanded = VietnameseTextNormalizer.expandSlangAndAbbreviations(textWithSlang)

        assertTrue("Phải mở rộng 'ko' thành 'không'", expanded.contains("không"))
        assertTrue("Phải mở rộng 'dc' thành 'được'", expanded.contains("được"))
        assertTrue("Phải mở rộng 'hnay' thành 'hôm nay'", expanded.contains("hôm nay"))
        assertTrue("Phải mở rộng 'cs1' thành 'cơ sở 1'", expanded.contains("cơ sở 1"))
    }

    @Test
    fun testVietnameseTextNormalizer_extractNgrams() {
        val text = "hôm nay có mưa"
        val ngrams = VietnameseTextNormalizer.extractNgrams(text)

        assertTrue(ngrams.contains("hôm"))
        assertTrue(ngrams.contains("mưa"))
        assertTrue(ngrams.contains("có mưa"))
        assertTrue(ngrams.contains("co mua"))
        assertTrue(ngrams.contains("hom nay"))
    }

    @Test
    fun testNlpClassifier_rainAndUmbrellaIntent() {
        val res1 = classifier.classify("Hôm nay có mưa không bot?")
        assertEquals(WeatherIntent.RAIN_AND_UMBRELLA, res1.intent)

        val res2 = classifier.classify("co can mang ao mua ko")
        assertEquals(WeatherIntent.RAIN_AND_UMBRELLA, res2.intent)

        val res3 = classifier.classify("chiều nay trời có mưa dông không")
        assertEquals(WeatherIntent.RAIN_AND_UMBRELLA, res3.intent)
    }

    @Test
    fun testNlpClassifier_rainStopTimeIntent() {
        val res = classifier.classify("khi nào hết mưa vậy bạn")
        assertEquals(WeatherIntent.RAIN_STOP_TIME, res.intent)

        val res2 = classifier.classify("mấy giờ tạnh mưa")
        assertEquals(WeatherIntent.RAIN_STOP_TIME, res2.intent)
    }

    @Test
    fun testNlpClassifier_uthFloodIntent() {
        val res1 = classifier.classify("Đường Ung Văn Khiêm CS1 có ngập không?")
        assertEquals(WeatherIntent.UTH_FLOOD_AND_TRAFFIC, res1.intent)
        assertEquals("Cơ sở 1 (Bình Thạnh - Ung Văn Khiêm)", res1.entities["campus"])

        val res2 = classifier.classify("to ky quan 12 co ngap lut khong")
        assertEquals(WeatherIntent.UTH_FLOOD_AND_TRAFFIC, res2.intent)
        assertEquals("Cơ sở 2 (Quận 12 - Tô Ký)", res2.entities["campus"])
    }

    @Test
    fun testNlpClassifier_laundryAndCarWashIntents() {
        val resLaundry = classifier.classify("trời này phơi đồ được không bot")
        assertEquals(WeatherIntent.LAUNDRY_DRYING, resLaundry.intent)

        val resCarWash = classifier.classify("hôm nay có nên rửa xe máy không")
        assertEquals(WeatherIntent.CAR_WASH, resCarWash.intent)
    }

    @Test
    fun testNlpClassifier_runningAndHealthIntents() {
        val resRunning = classifier.classify("chiều nay chạy bộ ngoài trời được không")
        assertEquals(WeatherIntent.OUTDOOR_ACTIVITIES, resRunning.intent)

        val resHealth = classifier.classify("thời tiết này có dễ bị cảm cúm đau đầu không")
        assertEquals(WeatherIntent.HEALTH_AND_WELLNESS, resHealth.intent)
    }

    @Test
    fun testNlpClassifier_uvAndAqiIntents() {
        val resUv = classifier.classify("chỉ số uv hôm nay có gắt không có cần bôi kem chống nắng")
        assertEquals(WeatherIntent.UV_INDEX_SUN, resUv.intent)

        val resAqi = classifier.classify("chất lượng không khí aqi và bụi mịn hôm nay")
        assertEquals(WeatherIntent.AIR_QUALITY_AQI, resAqi.intent)
    }

    @Test
    fun testNlpClassifier_travelDestinationIntent() {
        val resDalat = classifier.classify("thời tiết đà lạt cuối tuần này thế nào")
        assertEquals(WeatherIntent.TRAVEL_TOURISM, resDalat.intent)
        assertEquals("Đà Lạt", resDalat.entities["destination"])

        val resVungtau = classifier.classify("di vung tau tam bien duoc khong")
        assertEquals(WeatherIntent.TRAVEL_TOURISM, resVungtau.intent)
        assertEquals("Vũng Tàu", resVungtau.entities["destination"])
    }

    @Test
    fun testNlpClassifier_scienceAndProverbIntents() {
        val resScience = classifier.classify("tại sao trước khi mưa trời lại oi bức ngột ngạt")
        assertEquals(WeatherIntent.METEOROLOGY_SCIENCE, resScience.intent)

        val resProverb = classifier.classify("chuồn chuồn bay thấp thì mưa")
        assertEquals(WeatherIntent.FOLK_PROVERBS, resProverb.intent)
    }

    @Test
    fun testNlpClassifier_petCareIntent() {
        val resPet = classifier.classify("dắt chó đi dạo trời này có bị bỏng chân cún không")
        assertEquals(WeatherIntent.PET_CARE, resPet.intent)
    }

    @Test
    fun testAiWeatherAssistantEngine_generateRichOutput() {
        val fakeWeather = CurrentWeather(
            cityName = "TP. Hồ Chí Minh",
            temperatureC = 31.0,
            feelsLikeC = 35.0,
            minTemperatureC = 25.0,
            maxTemperatureC = 33.0,
            description = "Nhiều mây, có mưa rào rải rác",
            weatherMain = "Rain",
            humidityPercent = 82,
            pressureHpa = 1008.0,
            windSpeedMps = 4.2,
            windDirectionDeg = 210,
            latitude = 10.82,
            longitude = 106.63,
            iconCode = "10d",
            hourlyForecast = listOf(
                HourlyForecast(time = "14:00", temp = 31, condition = WeatherCondition.RAIN, pop = 75),
                HourlyForecast(time = "15:00", temp = 30, condition = WeatherCondition.RAIN, pop = 60),
                HourlyForecast(time = "16:00", temp = 29, condition = WeatherCondition.CLOUDY, pop = 25)
            )
        )

        val output = engine.processQuery(
            prompt = "Chiều nay có cần mang áo mưa không?",
            currentWeather = fakeWeather
        )

        assertNotNull(output)
        assertEquals(WeatherIntent.RAIN_AND_UMBRELLA, output.detectedIntent)
        assertTrue("Phản hồi phải chứa thông tin TP. Hồ Chí Minh", output.responseText.contains("TP. Hồ Chí Minh"))
        assertTrue("Phản hồi phải chứa phân tích mưa", output.responseText.contains("mưa"))
        assertTrue("Phải có danh sách gợi ý câu hỏi tiếp theo", output.followUpSuggestions.isNotEmpty())
    }

    @Test
    fun testSmartTravelPlanner_specificDestinationVungTau() {
        val query = "mai tôi đi vũng tàu chơi"
        val classification = classifier.classify(query)

        assertEquals(WeatherIntent.TRAVEL_TOURISM, classification.intent)
        assertEquals("Vũng Tàu", classification.entities["destination"])

        val output = engine.processQuery(
            prompt = query,
            currentWeather = null
        )

        val text = output.responseText
        assertTrue("Phản hồi phải chứa cẩm nang Vũng Tàu", text.contains("Vũng Tàu"))
        assertTrue("Phải có lịch trình Sáng", text.contains("Sáng"))
        assertTrue("Phải có lịch trình Trưa", text.contains("Trưa"))
        assertTrue("Phải có lịch trình Chiều", text.contains("Chiều"))
        assertTrue("Phải có lịch trình Tối", text.contains("Tối"))
        assertTrue("Phải có mục Hành trang cần chuẩn bị", text.contains("Hành trang cần chuẩn bị"))
        assertTrue("Phải có mục Di chuyển & An toàn", text.contains("Di chuyển & An toàn"))
    }

    @Test
    fun testSmartTravelPlanner_innerCityLandmark81() {
        val query = "mai đi landmark 81 chơi"
        val classification = classifier.classify(query)

        assertEquals(WeatherIntent.TRAVEL_TOURISM, classification.intent)
        assertEquals("Landmark 81", classification.entities["destination"])

        val output = engine.processQuery(
            prompt = query,
            currentWeather = null
        )

        val text = output.responseText
        assertTrue("Phản hồi phải chứa thông tin Landmark 81", text.contains("Landmark 81"))
        assertTrue("Phải có gợi ý ẩm thực / vui chơi", text.contains("ẩm thực") || text.contains("Vincom"))
        assertTrue("Phải có lịch trình các buổi", text.contains("Sáng") && text.contains("Chiều"))
    }

    @Test
    fun testSmartTravelPlanner_genericOutingQuery() {
        val query = "mai rảnh nên đi đâu chơi"
        val classification = classifier.classify(query)

        assertEquals(WeatherIntent.TRAVEL_TOURISM, classification.intent)

        val output = engine.processQuery(
            prompt = query,
            currentWeather = null
        )

        val text = output.responseText
        assertTrue("Phản hồi phải đưa ra gợi ý lịch trình đi chơi", text.contains("Lịch trình") || text.contains("Đi chơi"))
        assertTrue("Phải có phân tích thời tiết dự báo", text.contains("Thời tiết Dự báo") || text.contains("Dự báo"))
        assertTrue("Phải có gợi ý các điểm đến", text.contains("Gợi ý") || text.contains("Điểm"))
    }

    @Test
    fun testSmartTravelPlanner_district7TomorrowEvening() {
        val query = "tối mai tôi đi quận 7"
        val classification = classifier.classify(query)

        assertEquals(WeatherIntent.TRAVEL_TOURISM, classification.intent)
        assertEquals("Quận 7", classification.entities["destination"])
        assertEquals("Tối mai", classification.entities["time_slot"])

        val output = engine.processQuery(
            prompt = query,
            currentWeather = null
        )

        val text = output.responseText
        assertTrue("Phản hồi phải chứa thông tin Quận 7", text.contains("Quận 7"))
        assertTrue("Phải có Cầu Ánh Sao hoặc Hồ Bán Nguyệt", text.contains("Cầu Ánh Sao") || text.contains("Hồ Bán Nguyệt"))
        assertTrue("Phải có gợi ý ẩm thực Phú Mỹ Hưng", text.contains("Phú Mỹ Hưng") || text.contains("ẩm thực"))
        assertTrue("Phải có cảnh báo giao thông", text.contains("Cầu Kênh Tẻ") || text.contains("Giao thông"))
        assertTrue("Phải có tra cứu trước và sau 8h", text.contains("Trước 8h tối") && text.contains("Sau 8h tối"))
    }

    @Test
    fun testSmartTravelPlanner_district7BeforeAndAfter8Pm() {
        val query = "tối mai tôi đi quận 7 bạn tra trước và sau 8h có mưa không"
        val classification = classifier.classify(query)

        assertEquals(WeatherIntent.TRAVEL_TOURISM, classification.intent)
        assertEquals("Quận 7", classification.entities["destination"])
        assertEquals("Tối mai", classification.entities["time_slot"])
        assertEquals("20:00 (8h tối)", classification.entities["target_hour"])
        assertEquals("before_and_after", classification.entities["hour_window"])

        val output = engine.processQuery(
            prompt = query,
            currentWeather = null
        )

        val text = output.responseText
        assertTrue("Phản hồi phải chứa thông tin Quận 7", text.contains("Quận 7"))
        assertTrue("Phải tra cứu Trước 8h tối", text.contains("Trước 8h tối"))
        assertTrue("Phải tra cứu Tầm 8h tối", text.contains("8h tối"))
        assertTrue("Phải tra cứu Sau 8h tối", text.contains("Sau 8h tối"))
        assertTrue("Phải có kết luận không mưa", text.contains("KHÔNG CÓ MƯA") || text.contains("không có mưa"))
        assertTrue("Phải có Cầu Ánh Sao hoặc Hồ Bán Nguyệt", text.contains("Cầu Ánh Sao") || text.contains("Hồ Bán Nguyệt"))
    }

    @Test
    fun testSmartTravelPlanner_conciseRouteAndEnRouteRainCheck() {
        val query = "tối mai tôi đi quận 7 kiểm tra đoạn đường đi có mưa không"
        val classification = classifier.classify(query)

        assertEquals(WeatherIntent.TRAVEL_TOURISM, classification.intent)
        assertEquals("Quận 7", classification.entities["destination"])
        assertEquals("true", classification.entities["route_check"])

        val output = engine.processQuery(
            prompt = query,
            currentWeather = null
        )

        val text = output.responseText
        assertTrue("Phản hồi phải chứa tiêu đề Lộ trình & Kiểm tra Mưa", text.contains("Lộ trình & Kiểm tra Mưa"))
        assertTrue("Phải chứa Tuyến đường di chuyển", text.contains("Tuyến đường di chuyển"))
        assertTrue("Phải chứa khoảng cách hoặc thời gian", text.contains("Khoảng cách") || text.contains("km"))
        assertTrue("Phải chứa Kiểm tra Mưa trên Toàn bộ Lộ trình", text.contains("Kiểm tra Mưa trên Toàn bộ Lộ trình"))
        assertTrue("Phải kiểm tra Điểm xuất phát", text.contains("Điểm xuất phát") || text.contains("Điểm đi"))
        assertTrue("Phải kiểm tra Đoạn đường di chuyển", text.contains("Đoạn đường di chuyển"))
        assertTrue("Phải kiểm tra Điểm đến", text.contains("Điểm đến"))
        assertTrue("Phải có Cầu Ánh Sao hoặc Hồ Bán Nguyệt", text.contains("Cầu Ánh Sao") || text.contains("Hồ Bán Nguyệt"))
    }

    @Test
    fun testSmartTravelPlanner_routeDalatRainCheck() {
        val query = "mai tôi đi đà lạt đường đi có mưa không"
        val classification = classifier.classify(query)

        assertEquals(WeatherIntent.TRAVEL_TOURISM, classification.intent)
        assertEquals("Đà Lạt", classification.entities["destination"])
        assertEquals("true", classification.entities["route_check"])

        val output = engine.processQuery(
            prompt = query,
            currentWeather = null
        )

        val text = output.responseText
        assertTrue("Phải chứa thông tin Đà Lạt", text.contains("Đà Lạt"))
        assertTrue("Phải chứa Đèo Bảo Lộc", text.contains("Đèo Bảo Lộc"))
        assertTrue("Phải chứa Lộ trình", text.contains("Lộ trình"))
        assertTrue("Phải kiểm tra mưa trên lộ trình", text.contains("Kiểm tra Mưa"))
    }

    @Test
    fun testProcessQueryWithGemini_onlineSuccess() = runTest {
        val fakeGeminiApi = object : GeminiApiService {
            override suspend fun generateContent(
                model: String,
                apiKey: String,
                request: GeminiRequest
            ): Response<GeminiResponse> {
                val fakeReply = GeminiResponse(
                    candidates = listOf(
                        GeminiCandidate(
                            content = GeminiCandidateContent(
                                parts = listOf(
                                    GeminiPart(
                                        text = "* **Lộ trình:** UTH CS1 -> Q7\n* **Mưa:** Trước 20h ráo, sau 20h mưa rải rác.\n* **Lời khuyên:** Mang áo mưa."
                                    )
                                )
                            )
                        )
                    )
                )
                return Response.success(fakeReply)
            }
        }

        val geminiEngine = AiWeatherAssistantEngine(
            customKnowledgeRepo = null,
            geminiApiService = fakeGeminiApi,
            enableOnlineGemini = true
        )

        val result = geminiEngine.processQueryWithGemini(
            prompt = "tối mai tôi đi quận 7 kiểm tra đoạn đường đi có mưa không",
            currentWeather = null
        )

        assertTrue("Phải kích hoạt Gemini thành công", result.isGemini)
        assertEquals("Gemini 3.5 Flash", result.engineName)
        assertTrue(result.responseText.contains("Lộ trình"))
        assertTrue(result.responseText.contains("Trước 20h"))
    }

    @Test
    fun testProcessQueryWithGemini_networkError_fallbacksToLocalNlp() = runTest {
        val failingGeminiApi = object : GeminiApiService {
            override suspend fun generateContent(
                model: String,
                apiKey: String,
                request: GeminiRequest
            ): Response<GeminiResponse> {
                throw java.io.IOException("Không có kết nối mạng mô phỏng")
            }
        }

        val geminiEngine = AiWeatherAssistantEngine(
            customKnowledgeRepo = null,
            geminiApiService = failingGeminiApi,
            enableOnlineGemini = true
        )

        val result = geminiEngine.processQueryWithGemini(
            prompt = "tối mai tôi đi quận 7 kiểm tra đoạn đường đi có mưa không",
            currentWeather = null
        )

        assertFalse("Khi lỗi mạng phải fallback về Offline", result.isGemini)
        assertEquals("UTH NLP Offline", result.engineName)
        assertTrue("Nội dung fallback vẫn phải có lộ trình quận 7", result.responseText.contains("Quận 7"))
    }

    @Test
    fun testAvoidAdjacentAreasExtractionAndResponse() {
        val query = "tối mai tôi đi quận 7 kiểm tra đoạn đường đi có mưa không và các khu vực lân cận cần tránh"
        val classification = classifier.classify(query)

        assertEquals(WeatherIntent.TRAVEL_TOURISM, classification.intent)
        assertEquals("Quận 7", classification.entities["destination"])
        assertEquals("true", classification.entities["avoid_adjacent"])

        val output = engine.processQuery(
            prompt = query,
            currentWeather = null
        )

        val text = output.responseText
        assertTrue("Phải có tiêu đề khu vực lân cận cần NÉ / TRÁNH", text.contains("Khu vực lân cận") && text.contains("NÉ / TRÁNH"))
        assertTrue("Phải cảnh báo đường Trần Xuân Soạn", text.contains("Trần Xuân Soạn"))
        assertTrue("Phải cảnh báo đường Huỳnh Tấn Phát", text.contains("Huỳnh Tấn Phát"))
        assertTrue("Phải có tuyến đường tránh an toàn", text.contains("Tuyến đường tránh") || text.contains("an toàn"))
    }
}

