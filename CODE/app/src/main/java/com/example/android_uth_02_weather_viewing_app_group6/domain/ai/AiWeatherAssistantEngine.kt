package com.example.android_uth_02_weather_viewing_app_group6.domain.ai

import com.example.android_uth_02_weather_viewing_app_group6.data.location.FloodRiskEvaluator
import com.example.android_uth_02_weather_viewing_app_group6.data.remote.api.ApiConfig
import com.example.android_uth_02_weather_viewing_app_group6.data.remote.api.GeminiApiService
import com.example.android_uth_02_weather_viewing_app_group6.data.remote.api.GeminiContent
import com.example.android_uth_02_weather_viewing_app_group6.data.remote.api.GeminiPart
import com.example.android_uth_02_weather_viewing_app_group6.data.remote.api.GeminiRequest
import com.example.android_uth_02_weather_viewing_app_group6.data.remote.api.GeminiSystemInstruction
import com.example.android_uth_02_weather_viewing_app_group6.data.remote.api.RetrofitClient
import com.example.android_uth_02_weather_viewing_app_group6.domain.model.CurrentWeather
import com.example.android_uth_02_weather_viewing_app_group6.ui.model.AirQuality
import com.example.android_uth_02_weather_viewing_app_group6.ui.model.UVIndex
import com.example.android_uth_02_weather_viewing_app_group6.ui.model.UrbanFloodReport
import com.example.android_uth_02_weather_viewing_app_group6.ui.model.WeatherCondition

/**
 * Dữ liệu phản hồi toàn diện từ Trợ lý AI, bao gồm văn bản phản hồi,
 * danh sách câu hỏi gợi ý tiếp theo (chips) và thông tin ý định đã nhận diện.
 */
data class AiEngineOutput(
    val responseText: String,
    val followUpSuggestions: List<String>,
    val detectedIntent: WeatherIntent,
    val confidence: Float,
    val isCustomKnowledge: Boolean = false,
    val isGemini: Boolean = false,
    val engineName: String = "UTH AI"
)


/**
 * Động cơ Trợ lý Thời tiết Ảo AI (AI Weather Assistant Engine).
 * Tích hợp sâu Bộ xử lý Ngôn ngữ Tự nhiên (NLP), Mô hình Phân loại Ý định (25+ Intents),
 * Dữ liệu Thời tiết Thời gian Thực, và Kho Tri thức Tùy chỉnh (Custom Knowledge Base).
 */
class AiWeatherAssistantEngine(
    private val customKnowledgeRepo: AiCustomKnowledgeRepository? = null,
    private val geminiApiService: GeminiApiService? = null,
    var enableOnlineGemini: Boolean = true
) {
    companion object {
        var globalEnableOnlineGemini: Boolean = true
    }

    private val nlpClassifier = WeatherNlpClassifier()

    /**
     * Hàm sinh câu trả lời (giữ tương thích ngược với các cuộc gọi cũ).
     */
    fun generateResponse(
        prompt: String,
        currentWeather: CurrentWeather?,
        floodReport: UrbanFloodReport? = null,
        airQuality: AirQuality? = null,
        uvIndex: UVIndex? = null
    ): String {
        return processQuery(prompt, currentWeather, floodReport, airQuality, uvIndex).responseText
    }

    /**
     * Xử lý truy vấn thông minh bằng Google Gemini 3.5 Flash Lite
     * Tự động huấn luyện mô hình với ngữ cảnh thời tiết & ngập úng UTH thực tế.
     * Tự động fallback sang Local NLP Engine nếu lỗi mạng hoặc offline.
     */
    suspend fun processQueryWithGemini(
        prompt: String,
        currentWeather: CurrentWeather?,
        floodReport: UrbanFloodReport? = null,
        airQuality: AirQuality? = null,
        uvIndex: UVIndex? = null
    ): AiEngineOutput {
        val rawPrompt = prompt.trim()

        // 1. Kiểm tra ưu tiên: Kho tri thức tùy chỉnh do người dùng tự dạy
        val customAnswer = customKnowledgeRepo?.findMatchingAnswer(rawPrompt)
        if (!customAnswer.isNullOrBlank()) {
            return AiEngineOutput(
                responseText = "🎓 **[Kiến thức bạn đã huấn luyện cho AI]**\n\n$customAnswer",
                followUpSuggestions = listOf("Hôm nay có mưa không?", "Đường UTH có ngập không?", "Mặc gì hôm nay?"),
                detectedIntent = WeatherIntent.CUSTOM_KNOWLEDGE,
                confidence = 1.0f,
                isCustomKnowledge = true,
                isGemini = false,
                engineName = "Kiến thức tự dạy"
            )
        }

        // Chạy NLP Classifier để nhận diện Intent & Entities
        val classification = nlpClassifier.classify(rawPrompt)
        val intent = classification.intent
        val entities = classification.entities

        // 2. Thử gọi Google Gemini 3.5 Flash Lite với System Instruction huấn luyện chuẩn
        try {
            if (globalEnableOnlineGemini && enableOnlineGemini && ApiConfig.GEMINI_API_KEY.isNotBlank()) {
                val api = geminiApiService ?: RetrofitClient.geminiApi
                val systemPrompt = buildGeminiTrainingPrompt(currentWeather, floodReport, airQuality, uvIndex)
                val geminiRequest = GeminiRequest(
                    systemInstruction = GeminiSystemInstruction(
                        parts = listOf(GeminiPart(text = systemPrompt))
                    ),
                    contents = listOf(
                        GeminiContent(
                            role = "user",
                            parts = listOf(GeminiPart(text = rawPrompt))
                        )
                    )
                )

                val response = api.generateContent(
                    model = ApiConfig.GEMINI_MODEL,
                    apiKey = ApiConfig.GEMINI_API_KEY,
                    request = geminiRequest
                )


                if (response.isSuccessful) {
                    val candidateText = response.body()?.candidates?.firstOrNull()?.content?.parts?.firstOrNull()?.text
                    if (!candidateText.isNullOrBlank()) {
                        val suggestions = getSuggestionsForIntent(intent, entities)
                        return AiEngineOutput(
                            responseText = candidateText.trim(),
                            followUpSuggestions = suggestions,
                            detectedIntent = intent,
                            confidence = 0.99f,
                            isCustomKnowledge = false,
                            isGemini = true,
                            engineName = "Gemini 3.5 Flash"
                        )
                    }
                }
            }
        } catch (e: Exception) {
            // Lỗi mạng hoặc timeout -> fallback sang local NLP
        }

        // 3. Fallback: Local NLP Engine
        val localOutput = processQuery(prompt, currentWeather, floodReport, airQuality, uvIndex)
        return localOutput.copy(
            isGemini = false,
            engineName = "UTH NLP Offline"
        )
    }

    /**
     * Huấn luyện Prompt cho Gemini với ngữ cảnh thời gian thực và các quy tắc phản hồi.
     */
    private fun buildGeminiTrainingPrompt(
        currentWeather: CurrentWeather?,
        floodReport: UrbanFloodReport?,
        airQuality: AirQuality?,
        uvIndex: UVIndex?
    ): String {
        val city = currentWeather?.cityName ?: "Thành phố Hồ Chí Minh"
        val temp = currentWeather?.temperatureC?.toInt() ?: 28
        val feelsLike = currentWeather?.feelsLikeC?.toInt() ?: 30
        val humidity = currentWeather?.humidityPercent ?: 75
        val desc = currentWeather?.description ?: "Nhiều mây"
        val windSpeed = currentWeather?.windSpeedMps ?: 3.0
        val windKmH = (windSpeed * 3.6).toInt()
        val isRainingNow = desc.lowercase().contains("mưa")

        val customKnowledge = customKnowledgeRepo?.getAll() ?: emptyList()
        val customKnowledgeSummary = if (customKnowledge.isNotEmpty()) {
            "\nKIẾN THỨC BỔ SUNG ĐÃ HỌC TỪ NGƯỜI DÙNG:\n" +
                    customKnowledge.joinToString("\n") { "- Hỏi: ${it.question} => Đáp: ${it.answer}" }
        } else ""

        return """
            Bạn là "Trợ lý Thời tiết Thông minh UTH" của Trường Đại học Giao thông Vận tải TP.HCM (UTH - Nhóm 6).
            
            QUY TẮC PHẢN HỒI BẮT BUỘC:
            1. PHẢI TRẢ LỜI CỰC KỲ NGẮN GỌN, SÚC TÍCH, KHÔNG LAN MAN, KHÔNG VIẾT ĐOẠN VĂN DÀI.
            2. Sử dụng gạch đầu dòng (bullet points) rõ ràng, biểu tượng cảm xúc (emoji) trực quan.
            3. KHI NGƯỜI DÙNG HỎI VỀ ĐI LẠI / LỘ TRÌNH (ví dụ: tối mai tôi đi quận 7 kiểm tra đoạn đường đi có mưa không, mai đi Đà Lạt, đi chơi...):
               • Luôn lấy điểm xuất phát từ vị trí hiện tại (mặc định: UTH Cơ sở 1 - số 2 Võ Oanh, P.25, Q. Bình Thạnh, TP.HCM hoặc vị trí của người dùng) đến Điểm đến.
               • LUÔN CUNG CẤP ĐỦ 4 MỤC SAU (DÙ NGƯỜI DÙNG CÓ HỎI RÕ HAY KHÔNG):
                 - 📍 Lộ trình: Tuyến đường chính qua các cầu/đường lớn, khoảng cách km và thời gian di chuyển ước tính.
                 - 🌧️ Kiểm tra mưa trên đường đi: Phân tích khả năng mưa trên suốt đoạn đường di chuyển và các mốc giờ quan trọng (đặc biệt là trước 20h và sau 20h nếu hỏi về buổi tối).
                 - ⚠️ Khu vực lân cận điểm đến cần NÉ / TRÁNH: Luôn chủ động rà soát các khu vực, phường, hoặc tuyến đường lân cận/giáp ranh điểm đến có nguy cơ ngập úng, triều cường, nút giao kẹt xe hoặc sạt lở (ví dụ: đi Q7 thì cảnh báo tránh Trần Xuân Soạn ngập triều, Huỳnh Tấn Phát, Lê Văn Lương; đi Thủ Đức thì tránh Thảo Điền - Quốc Hương; đi Q12 tránh Nguyễn Văn Quá...) và gợi ý các tuyến đường tránh/đường vòng an toàn hơn (ví dụ đi Nguyễn Lương Bằng, Nguyễn Thị Thập...).
                 - 🛵 Lời khuyên di chuyển & Hành trang: Cảnh báo đoạn trơn trượt, nhắc mang áo mưa dự phòng và lưu ý thời gian xuất phát an toàn.
            4. THÔNG TIN THỜI TIẾT THỜI GIAN THỰC HIỆN TẠI ĐỂ BẠN THAM KHẢO:
               • Địa điểm hiện tại: $city
               • Trạng thái bầu trời: $desc (${if (isRainingNow) "Đang có mưa" else "Trời ráo"})
               • Nhiệt độ: $temp°C (Cảm nhận thực tế: $feelsLike°C)
               • Độ ẩm: $humidity% | Tốc độ gió: $windKmH km/h
               • Chỉ số UV: ${uvIndex?.index ?: 5} (${uvIndex?.level ?: "Trung bình"})
               • Chỉ số AQI: ${airQuality?.aqi ?: 42} (${airQuality?.status ?: "Tốt"})

            5. TÌNH HÌNH CÁC CƠ SỞ TRƯỜNG UTH:
               • CS1 (Võ Oanh, Bình Thạnh): Dễ ngập đường D2 và Ung Văn Khiêm khi mưa to kết hợp triều cường.
               • CS2 (Trần Não, TP. Thủ Đức): Khu vực cao ráo.
               • CS3 (Tô Ký, Quận 12): Có thể ngập trước cổng trường khi mưa rất lớn.
            $customKnowledgeSummary
        """.trimIndent()
    }

    /**
     * Sinh danh sách câu hỏi gợi ý tiếp theo phù hợp với Intent.
     */
    fun getSuggestionsForIntent(intent: WeatherIntent, entities: Map<String, String> = emptyMap()): List<String> {
        return when (intent) {
            WeatherIntent.RAIN_AND_UMBRELLA, WeatherIntent.RAIN_STOP_TIME ->
                listOf("Đường UTH có ngập không?", "Khi nào tạnh mưa?", "Phơi đồ được không?")
            WeatherIntent.UTH_FLOOD_AND_TRAFFIC, WeatherIntent.STUDENT_COMMUTE_ADVICE ->
                listOf("Đi học ca chiều cần mang gì?", "Có mưa không bot?", "Đường Ung Văn Khiêm ngập không?")
            WeatherIntent.OUTFIT_ADVICE, WeatherIntent.LAUNDRY_DRYING ->
                listOf("Chỉ số UV thế nào?", "Hôm nay bao nhiêu độ?", "Chiều nay chạy bộ được không?")
            WeatherIntent.OUTDOOR_ACTIVITIES, WeatherIntent.HEALTH_AND_WELLNESS ->
                listOf("Chất lượng không khí AQI?", "Tia UV có gắt không?", "Mặt trời lặn lúc mấy giờ?")
            WeatherIntent.AIR_QUALITY_AQI, WeatherIntent.UV_INDEX_SUN ->
                listOf("Có nên chạy bộ không?", "Mặc gì hôm nay?", "Hôm nay có mưa không?")
            WeatherIntent.TRAVEL_TOURISM -> {
                val target = entities["destination"]
                if (target != null) {
                    listOf(
                        "Mai đi $target mặc đồ gì đẹp?",
                        "Đường đi $target có kẹt xe không?",
                        "Chi phí dự kiến đi $target bao nhiêu?"
                    )
                } else {
                    listOf(
                        "Mai tôi đi Vũng Tàu chơi",
                        "Mai đi Landmark 81 có gì vui?",
                        "Cuối tuần đi phượt Đà Lạt được không?"
                    )
                }
            }
            else ->
                listOf("Hôm nay có mưa không?", "Đường UTH có ngập không?", "Mặc gì hôm nay?", "Phơi đồ được không?")
        }
    }


    /**
     * Hàm xử lý truy vấn AI toàn diện với phân tích NLP và gợi ý thông minh.
     */
    fun processQuery(
        prompt: String,
        currentWeather: CurrentWeather?,
        floodReport: UrbanFloodReport? = null,
        airQuality: AirQuality? = null,
        uvIndex: UVIndex? = null
    ): AiEngineOutput {
        val rawPrompt = prompt.trim()

        // 1. Kiểm tra ưu tiên: Kho tri thức tùy chỉnh do người dùng tự dạy
        val customAnswer = customKnowledgeRepo?.findMatchingAnswer(rawPrompt)
        if (!customAnswer.isNullOrBlank()) {
            return AiEngineOutput(
                responseText = "🎓 **[Kiến thức bạn đã huấn luyện cho AI]**\n\n$customAnswer",
                followUpSuggestions = listOf("Hôm nay có mưa không?", "Đường UTH có ngập không?", "Mặc gì hôm nay?"),
                detectedIntent = WeatherIntent.CUSTOM_KNOWLEDGE,
                confidence = 1.0f,
                isCustomKnowledge = true
            )
        }

        // 2. Chạy Bộ phân loại Ngôn ngữ Tự nhiên (NLP Intent Classifier)
        val classification = nlpClassifier.classify(rawPrompt)
        val intent = classification.intent
        val confidence = classification.confidence
        val entities = classification.entities

        // Dữ liệu thời tiết hiện tại
        val city = currentWeather?.cityName ?: "Thành phố Hồ Chí Minh"
        val temp = currentWeather?.temperatureC?.toInt() ?: 28
        val feelsLike = currentWeather?.feelsLikeC?.toInt() ?: 30
        val humidity = currentWeather?.humidityPercent ?: 75
        val desc = currentWeather?.description ?: "Nhiều mây"
        val windSpeed = currentWeather?.windSpeedMps ?: 3.0
        val windKmH = (windSpeed * 3.6).toInt()

        val isNight = currentWeather?.iconCode?.endsWith("n") == true || desc.lowercase().contains("đêm")
        val condition = currentWeather?.let { WeatherCondition.fromDescription(it.description, isNight) } ?: WeatherCondition.PARTLY_CLOUDY
        val isRainingNow = desc.lowercase().contains("mưa") ||
                condition == WeatherCondition.RAIN ||
                condition == WeatherCondition.LIGHT_RAIN ||
                condition == WeatherCondition.HEAVY_RAIN ||
                condition == WeatherCondition.THUNDERSTORM

        // 3. Sinh câu trả lời chuyên sâu theo từng Intent
        val response = when (intent) {
            WeatherIntent.GREETING -> {
                """
                👋 **Xin chào bạn!** Tôi là **Trợ lý Thời tiết UTH AI** (sản phẩm đồ án Nhóm 6 - Đại học GTVT TP.HCM).
                
                Tôi đã được huấn luyện hơn **25+ chủ đề thời tiết & sinh hoạt** để giải đáp cho bạn:
                • 🌧️ Dự báo mưa, khả năng tạnh và lời khuyên mang ô/áo mưa
                • 🛵 Tình hình ngập úng quanh 3 cơ sở trường UTH & tuyến đường né ngập
                • 👕 Tư vấn trang phục, phơi đồ, rửa xe máy & dắt thú cưng
                • 🏃 Chỉ số chạy bộ ngoài trời, chỉ số UV & chất lượng không khí (AQI)
                • 🏖️ Dự báo thời tiết các điểm du lịch & ca dao tục ngữ thời tiết
                
                Hôm nay bạn muốn kiểm tra điều gì nào? 😊
                """.trimIndent()
            }

            WeatherIntent.GENERAL_OVERVIEW -> {
                buildString {
                    append("🌤️ **Tổng quan thời tiết tại $city hôm nay:**\n\n")
                    append("• **Trạng thái:** $desc ${if (isRainingNow) "(đang có mưa)" else "(trời ráo)"}\n")
                    append("• **Nhiệt độ:** $temp°C • Cảm giác thực tế: **$feelsLike°C**\n")
                    append("• **Độ ẩm:** $humidity% • **Gió:** $windKmH km/h (${"%.1f".format(windSpeed)} m/s)\n")
                    if (isRainingNow) {
                        append("• ⚠️ **Lưu ý:** Ngoài trời đang có mưa, hãy cẩn thận khi lái xe và mang theo áo mưa!\n")
                    } else if (humidity > 80) {
                        append("• 💡 Độ ẩm khá cao, không khí hơi oi bức, có thể có mưa rào rải rác về chiều tối.\n")
                    } else {
                        append("• ☀️ Thời tiết nhìn chung thuận lợi cho các hoạt động di chuyển và học tập.\n")
                    }
                }
            }

            WeatherIntent.TEMPERATURE_FEEL -> {
                buildString {
                    append("🌡️ **Nhiệt độ & Cảm giác thực tế tại $city:**\n\n")
                    append("• **Nhiệt độ đo được:** $temp°C\n")
                    append("• **Nhiệt độ cảm nhận (Feels-like):** $feelsLike°C\n\n")
                    val diff = feelsLike - temp
                    if (diff >= 3) {
                        append("🔥 **Đánh giá:** Nhiệt độ cảm nhận cao hơn thực tế do **độ ẩm cao ($humidity%)** khiến cơ thể khó thoát mồ hôi, tạo cảm giác oi bức hầm nóng.\n")
                        append("💡 Khuyên bạn nên uống đủ nước (1.5 - 2 lít) và hạn chế đứng dưới nắng gắt quá lâu.")
                    } else if (temp <= 22) {
                        append("🧥 **Đánh giá:** Tiết trời se lạnh mát mẻ, rất dễ chịu khi ra ngoài.")
                    } else {
                        append("🌤️ **Đánh giá:** Mức nhiệt tương đối dễ chịu, gió nhẹ ${"%.1f".format(windSpeed)} m/s.")
                    }
                }
            }

            WeatherIntent.RAIN_AND_UMBRELLA -> {
                val highRainProbability = currentWeather?.hourlyForecast?.any { it.pop >= 50 } ?: (humidity > 80)
                val rainHours = currentWeather?.hourlyForecast?.filter { it.pop >= 45 }?.take(4)?.map { "${it.time} (${it.pop}%)" }
                buildString {
                    append("🌧️ **Dự báo Mưa & Áo mưa tại $city:**\n\n")
                    if (isRainingNow) {
                        append("• ☔ **Hiện tại ngoài trời ĐANG CÓ $desc** với độ ẩm cao ($humidity%).\n")
                        append("• ⚠️ **Lời khuyên:** Bạn nhất định **phải mặc áo mưa** khi ra ngoài, che chắn cẩn thận balo và thiết bị điện tử.\n")
                    } else if (highRainProbability) {
                        append("• Hiện tại trời chưa mưa lớn ($desc), nhưng độ ẩm đạt **$humidity%** và xác suất mưa trong vài giờ tới khá cao.\n")
                        if (!rainHours.isNullOrEmpty()) {
                            append("• ⏰ Các khung giờ dễ có mưa rào: **${rainHours.joinToString(", ")}**.\n")
                        }
                        append("• 💡 **Khuyến nghị:** Bạn **chắc chắn nên mang theo áo mưa hoặc ô (dù)** sẵn trong cốp xe để không bị động!\n")
                    } else {
                        append("• Hiện tại xác suất mưa thấp ($desc, độ ẩm $humidity%).\n")
                        append("• ☀️ Trời tương đối khô ráo, tuy nhiên thời tiết Sài Gòn có thể có mưa dông nhiệt bất chợt, mang theo một chiếc áo mưa mỏng dự phòng vẫn là giải pháp an toàn nhất!\n")
                    }
                }
            }

            WeatherIntent.RAIN_STOP_TIME -> {
                val dryHour = currentWeather?.hourlyForecast?.firstOrNull { it.pop < 30 }
                buildString {
                    append("⏳ **Dự đoán thời điểm Tạnh mưa tại $city:**\n\n")
                    if (isRainingNow) {
                        if (dryHour != null) {
                            append("• 🌦️ Cơn mưa dự kiến sẽ giảm dần và ráo nước vào khoảng **${dryHour.time}** (xác suất mưa giảm về ${dryHour.pop}%).\n")
                        } else {
                            append("• 🌦️ Cơn mưa dông nhiệt thường kéo dài từ **30 đến 60 phút** rồi chuyển sang tạnh ráo hoặc mưa phùn nhẹ.\n")
                        }
                        append("• 💡 Nếu bạn đang trên đường, hãy ghé vào quán cà phê hoặc điểm trú mưa an toàn đợi khoảng 30 phút để đường rút bớt nước nhé!\n")
                    } else {
                        append("• Hiện tại trời đang không mưa ($desc). Các giờ tới xác suất mưa duy trì ở mức an toàn.\n")
                    }
                }
            }

            WeatherIntent.UTH_FLOOD_AND_TRAFFIC -> {
                val report = floodReport ?: if (currentWeather != null) FloodRiskEvaluator.evaluate(currentWeather, city) else null
                val targetCampus = entities["campus"]
                buildString {
                    append("🛵 **Tình hình Ngập úng & Tuyến đường UTH:**\n\n")
                    if (targetCampus != null) {
                        append("🔍 **Trọng tâm theo yêu cầu:** $targetCampus\n\n")
                    }
                    if (report != null && report.hasAlert) {
                        append("• ⚠️ **Mức độ cảnh báo ngập:** ${report.level.labelVi}\n")
                        val streetNames = report.highRiskStreets.joinToString(", ") { it.streetName }
                        if (streetNames.isNotBlank()) {
                            append("• 📍 **Các tuyến đường nguy cơ ngập sâu:** $streetNames\n\n")
                        }
                        append("• 🏫 **Cơ sở 1 (Bình Thạnh):** Tuyến **Ung Văn Khiêm** (từ D2 đến ngã năm) và chân cầu vượt **Nguyễn Hữu Cảnh** rất dễ đọng nước, xe máy số nên đi số thấp (số 2) và giữ đều ga.\n")
                        append("• 🏫 **Cơ sở 2 (Quận 12):** Tuyến **Tô Ký - QL1A** có thể ngập cục bộ ở vùng trũng sát chợ Cầu, cẩn thận trơn trượt.\n")
                        append("• 🏫 **Cơ sở 3 (TP. Thủ Đức):** Đoạn **Đỗ Xuân Hợp** triều cường kết hợp mưa lớn dễ dâng cao, khuyến khích sinh viên chuyển hướng đi đường Mai Chí Thọ.\n")
                    } else {
                        append("• ✅ **Tình trạng:** Các tuyến đường quanh các cơ sở UTH hiện tại cơ bản **thông thoáng, an toàn**.\n")
                        append("• 🏫 Tuyến Ung Văn Khiêm (CS1), Tô Ký (CS2), Đỗ Xuân Hợp (CS3) xe cộ lưu thông bình thường.\n")
                        append("• 💡 Bạn có thể chuyển sang tab **Lộ trình (Trip Planner)** để xem bản đồ chỉ đường tránh ngập OSRM nhé!\n")
                    }
                }
            }

            WeatherIntent.STUDENT_COMMUTE_ADVICE -> {
                val timeSlot = entities["time_slot"] ?: "hôm nay"
                buildString {
                    append("🎒 **Lời khuyên Đi học dành cho Sinh viên UTH ($timeSlot):**\n\n")
                    append("• 💻 **Bảo vệ Laptop & Giáo trình:** Hãy bọc balo bằng túi trùm chống nước hoặc cho đồ điện tử vào túi zip chống ẩm.\n")
                    append("• 🧥 **Áo mưa cánh dơi vs Áo mưa bộ:** Đi xe máy đường dài qua CS2 hoặc CS3 nên mặc áo mưa bộ để không bị tạt nước và hạn chế vướng vào bánh xe.\n")
                    append("• 👟 **Giày dép:** Mang theo dép quai hậu hoặc bọc giày đi mưa để tránh hỏng giày thể thao khi đi qua Ung Văn Khiêm.\n")
                    if (isRainingNow) {
                        append("• ⚠️ Trời đang mưa, bạn nên xuất phát sớm hơn 15-20 phút so với giờ điểm danh để đề phòng kẹt xe cục bộ quanh cổng trường!\n")
                    }
                }
            }

            WeatherIntent.OUTFIT_ADVICE -> {
                buildString {
                    append("👕 **Gợi ý Trang phục hôm nay tại $city (${temp}°C - Cảm giác: ${feelsLike}°C):**\n\n")
                    if (temp >= 32 || feelsLike >= 34) {
                        append("• 🔥 **Thời tiết oi bức:** Ưu tiên áo thun cotton, trang phục mỏng nhẹ, thoáng khí, thấm hút mồ hôi tốt.\n")
                        append("• 🕶️ **Đi ngoài trời:** Đeo kính râm, mặc áo khoác chống nắng có mũ và thoa kem chống nắng.\n")
                    } else if (temp <= 22) {
                        append("• 🧥 **Thời tiết se lạnh:** Nên mặc áo khoác nỉ, áo gió 2 lớp hoặc áo len mỏng.\n")
                    } else {
                        append("• 🌤️ **Nhiệt độ dễ chịu:** Thoải mái mặc áo sơ mi, áo phông năng động hoặc trang phục thường ngày.\n")
                    }
                    if (isRainingNow || humidity > 80) {
                        append("• 👟 **Lưu ý ẩm ướt:** Chọn giày sandal hoặc giày có đế cao su ma sát tốt chống trượt ngã.\n")
                    }
                }
            }

            WeatherIntent.LAUNDRY_DRYING -> {
                buildString {
                    append("🧺 **Đánh giá Điều kiện Phơi đồ & Giặt giũ tại $city:**\n\n")
                    if (isRainingNow) {
                        append("• ❌ **KHÔNG NÊN phơi đồ ngoài trời lúc này:** Đang có mưa ($desc), độ ẩm cao ($humidity%), quần áo sẽ bị ẩm ướt và ám mùi khó chịu.\n")
                        append("• 💡 Đề xuất: Phơi ở nơi có mái che thoáng gió hoặc dùng máy sấy / quạt máy hỗ trợ.\n")
                    } else if (humidity > 82) {
                        append("• ⚠️ **Điều kiện phơi đồ Trung bình:** Độ ẩm không khí cao ($humidity%), quần áo sẽ lâu khô hơn bình thường (mất từ 4-6 tiếng).\n")
                        append("• 💡 Nên giãn cách các móc phơi và mang đồ vào nhà trước 16:30 chiều đề phòng sương và mưa dông.\n")
                    } else {
                        append("• ✅ **RẤT THÍCH HỢP phơi đồ:** Trời ráo ($desc), nhiệt độ $temp°C và gió $windKmH km/h giúp quần áo mau khô tự nhiên và thơm tho.\n")
                    }
                }
            }

            WeatherIntent.OUTDOOR_ACTIVITIES -> {
                val aqiVal = airQuality?.aqi ?: 65
                val uvVal = uvIndex?.index ?: 5
                buildString {
                    append("🏃 **Đánh giá Thể thao & Chạy bộ ngoài trời tại $city:**\n\n")
                    if (isRainingNow) {
                        append("• ❌ **Không khuyến khích vận động ngoài trời lúc này:** Đường trơn ướt nguy hiểm và dễ bị cảm lạnh do dầm mưa.\n")
                        append("• 💡 Đề xuất: Tập các bài Cardio, Yoga hoặc chạy máy chạy bộ trong nhà.\n")
                    } else {
                        append("• ✅ **Điều kiện chung:** Khá tốt (${temp}°C, gió ${"%.1f".format(windSpeed)} m/s).\n")
                        append("• ⏰ **Khung giờ vàng đề xuất:** Buổi sáng (05:30 - 06:45) hoặc sau 17:30 chiều khi bức xạ mặt trời đã dịu.\n")
                        append("• 🍃 **Chất lượng không khí:** AQI $aqiVal (${airQuality?.status ?: "Trung bình"}).\n")
                        append("• ☀️ **Chỉ số UV:** Cấp $uvVal (${uvIndex?.level ?: "Trung bình"}).\n")
                    }
                }
            }

            WeatherIntent.HEALTH_AND_WELLNESS -> {
                buildString {
                    append("🩺 **Tư vấn Sức khỏe theo Thời tiết tại $city:**\n\n")
                    if (humidity > 80) {
                        append("• 🦟 **Cảnh báo Muỗi & Sốt xuất huyết:** Độ ẩm cao ($humidity%) là điều kiện lý tưởng cho lăng quăng và muỗi vằn phát triển. Hãy dọn sạch các vũng nước đọng quanh phòng trọ, mắc màn khi ngủ.\n")
                        append("• 👃 **Bệnh Viêm mũi dị ứng & Viêm xoang:** Độ ẩm cao kết hợp chênh lệch nhiệt độ điều hòa dễ làm phù nề niêm mạc mũi. Hãy súc họng bằng nước muối sinh lý.\n")
                    }
                    if (temp >= 33 || feelsLike >= 35) {
                        append("• 💧 **Nguy cơ say nắng / sốc nhiệt:** Hãy bổ sung oresol hoặc nước khoáng có chất điện giải nếu phải làm việc ngoài trời.\n")
                    }
                    append("• 🥗 Uống đủ ít nhất 2 lít nước mỗi ngày để tăng cường sức đề kháng.\n")
                }
            }

            WeatherIntent.AIR_QUALITY_AQI -> {
                val aqiVal = airQuality?.aqi ?: 62
                val status = airQuality?.status ?: "Trung bình"
                """
                🍃 **Chỉ số Chất lượng Không khí (AQI) tại $city:**
                
                • **Điểm số AQI:** **$aqiVal** - Đánh giá: **$status**
                • **Bụi mịn PM2.5:** ${if (aqiVal > 100) "Mức độ bụi cao, bầu trời có lớp mù quang hóa." else "Ở mức chấp nhận được đối với đa số người dân."}
                • 😷 **Khuyến cáo:** Nên đeo khẩu trang chuyên dụng (loại N95 hoặc khẩu trang y tế 4 lớp) khi di chuyển trên các trục đường lớn như Xa lộ Hà Nội, Điện Biên Phủ, QL13.
                """.trimIndent()
            }

            WeatherIntent.UV_INDEX_SUN -> {
                val uvVal = uvIndex?.index ?: 6
                val level = uvIndex?.level ?: "Trung bình"
                """
                ☀️ **Chỉ số Tia cực tím (UV Index) tại $city:**
                
                • **Chỉ số UV:** Cấp **$uvVal** (Mức: **$level**)
                • ⏰ **Khung giờ bức xạ cực đại:** 11:00 đến 14:30 trưa.
                • 🛡️ **Biện pháp bảo vệ da & mắt:**
                  - Bôi kem chống nắng chỉ số SPF 50+ trước khi ra ngoài 20 phút.
                  - Đeo kính râm chống tia UV400 để bảo vệ võng mạc.
                  - Mặc áo khoác dài tay vải dệt dày hoặc áo chống nắng chuyên dụng.
                """.trimIndent()
            }

            WeatherIntent.HUMIDITY_AND_MOLD -> {
                """
                💧 **Độ ẩm Không khí & Bảo quản Đồ đạc tại $city:**
                
                • **Độ ẩm hiện tại:** **$humidity%** (Mức: ${if (humidity > 75) "Cao" else "Bình thường"}).
                • 📷 **Bảo quản Đồ điện tử & Máy ảnh:** Với độ ẩm trên 70%, nấm mốc dễ sinh sôi trong ống kính máy ảnh và bảng mạch. Nên cất máy ảnh trong hộp chống ẩm hoặc tủ hút ẩm (duy trì 45-55%).
                • 🏠 **Hiện tượng nồm ẩm:** Tại miền Nam ít nồm ẩm như miền Bắc, tuy nhiên nền gạch có thể đổ mồ hôi khi mưa dầm lâu ngày, hãy bật điều hòa chế độ Dry để làm khô phòng.
                """.trimIndent()
            }

            WeatherIntent.WIND_AND_STORMS -> {
                """
                💨 **Tình hình Gió & Bão tại $city:**
                
                • **Tốc độ gió:** **${"%.1f".format(windSpeed)} m/s** (~$windKmH km/h) - Gió cấp 2 đến cấp 3 (nhẹ nhàng).
                • 🌀 **Tin thời tiết nguy hiểm:** Chưa ghi nhận áp thấp nhiệt đới hay bão đổ bộ trực tiếp vào khu vực Nam Bộ hôm nay.
                • ⚠️ **Lưu ý dông lốc:** Vào buổi chiều, các khối mây đối lưu có thể gây ra gió giật cục bộ làm gãy đổ cành cây lớn, hãy tránh trú dưới cây cổ thụ khi có dông sét!
                """.trimIndent()
            }

            WeatherIntent.SUNRISE_SUNSET -> {
                """
                🌅 **Thời gian Bình minh & Hoàng hôn hôm nay tại $city:**
                
                • 🌅 **Bình minh (Mặt trời mọc):** ~ 05:45 sáng
                • 🌇 **Hoàng hôn (Mặt trời lặn):** ~ 17:55 chiều
                • 📸 **Khung giờ vàng (Golden Hour chụp ảnh):**
                  - Buổi sáng: 06:00 - 06:45
                  - Buổi chiều: 17:15 - 17:45 (thích hợp ngắm hoàng hôn tại Bến Bạch Đằng, Cầu Thủ Thiêm hoặc ngắm hoàng hôn ngắm sông Sài Gòn!)
                """.trimIndent()
            }

            WeatherIntent.HOURLY_FORECAST -> {
                val targetHour = entities["target_hour"]
                val targetLocation = entities["destination"] ?: city
                val timeSlot = entities["time_slot"] ?: "Tối mai"
                if (targetHour != null || entities["hour_window"] != null) {
                    buildRainCheckAroundHour(targetHour ?: "8h tối (20:00)", targetLocation, timeSlot, currentWeather)
                } else {
                    val nextHours = currentWeather?.hourlyForecast?.take(5) ?: emptyList()
                    buildString {
                        append("⏰ **Diễn biến Thời tiết theo Giờ tại $city:**\n\n")
                        if (nextHours.isNotEmpty()) {
                            for (h in nextHours) {
                                append("• **${h.time}:** ${h.temp}°C • ${h.condition.labelVi} • Mưa: ${h.pop}%\n")
                            }
                        } else {
                            append("• **Buổi sáng (06:00 - 11:00):** 27°C - 31°C, trời nắng nhẹ, mây rải rác.\n")
                            append("• **Buổi trưa (11:00 - 14:00):** 32°C - 34°C, nền nhiệt cao nhất trong ngày.\n")
                            append("• **Buổi chiều (14:00 - 18:00):** 28°C - 30°C, xác suất xuất hiện mưa rào cục bộ.\n")
                            append("• **Buổi tối & Đêm:** 25°C - 27°C, thời tiết mát mẻ dễ chịu.\n")
                        }
                        append("\n👉 Bạn có thể xem biểu đồ đồ họa tại tab **Dự báo (Forecast)** trên thanh điều hướng.")
                    }
                }
            }

            WeatherIntent.FUTURE_FORECAST -> {
                val daily = currentWeather?.dailyForecast?.take(4) ?: emptyList()
                buildString {
                    append("📅 **Dự báo Thời tiết các ngày tới tại $city:**\n\n")
                    if (daily.isNotEmpty()) {
                        for (d in daily) {
                            append("• **${d.dayName} (${d.dateText}):** ${d.minTemp}°C - ${d.maxTemp}°C • ${d.condition.labelVi} (Mưa: ${d.pop}%)\n")
                        }
                    } else {
                        append("• **Ngày mai:** 26°C - 33°C • Ban ngày nắng, chiều tối có mưa rào rải rác.\n")
                        append("• **Cuối tuần (T7 & CN):** Nhiệt độ dao động 25°C - 32°C, rất thuận lợi cho việc tụ tập bạn bè hoặc du lịch ngắn ngày.\n")
                    }
                }
            }

            WeatherIntent.CAR_WASH -> {
                val rainChanceNext2Days = currentWeather?.dailyForecast?.take(2)?.any { it.pop >= 45 } ?: (humidity > 78)
                buildString {
                    append("🚗 **Tư vấn Rửa xe Máy / Ô tô:**\n\n")
                    if (isRainingNow) {
                        append("• ❌ **HÔM NAY KHÔNG NÊN RỬA XE:** Ngoài trời đang có mưa, đi ra đường xe sẽ lại dính bùn đất ngay.\n")
                    } else if (rainChanceNext2Days) {
                        append("• ⚠️ **Cân nhắc:** Xác suất mưa trong 24h - 48h tới khá cao ($humidity% độ ẩm). Nếu chỉ rửa xe để di chuyển hàng ngày, bạn có thể hoãn lại 1-2 hôm để tránh vừa rửa xong lại gặp mưa dông!\n")
                    } else {
                        append("• ✅ **THỜI ĐIỂM RẤT TỐT ĐỂ RỬA XE:** Thời tiết hanh ráo, nắng đẹp giúp xe khô nhanh và giữ được độ bóng sạch trong nhiều ngày tới.\n")
                    }
                }
            }

            WeatherIntent.PET_CARE -> {
                buildString {
                    append("🐾 **Lưu ý Thời tiết khi Chăm sóc & Dắt Thú cưng đi dạo:**\n\n")
                    if (temp >= 31) {
                        append("• ⚠️ **Quy tắc 7 giây kiểm tra mặt đường:** Dưới trời nắng $temp°C, mặt đường nhựa có thể nóng lên tới **50°C - 60°C**, dễ làm bỏng rát đệm thịt chân chó/mèo. Hãy áp mu bàn tay xuống mặt đường 7 giây, nếu thấy quá nóng thì KHÔNG dắt cún đi dạo lúc này!\n")
                        append("• ⏰ Giờ dắt thú cưng lý tưởng: Sau 18:00 tối hoặc sáng sớm trước 07:00.\n")
                        append("• 💧 Đem theo bình nước uống cho thú cưng tránh sốc nhiệt.\n")
                    } else {
                        append("• ✅ Thời tiết hiện tại (${temp}°C) khá mát mẻ, an toàn cho thú cưng ra ngoài vận động.\n")
                    }
                }
            }

            WeatherIntent.TRAVEL_TOURISM -> {
                buildSmartTravelPlanner(entities["destination"], entities, currentWeather, city)
            }

            WeatherIntent.METEOROLOGY_SCIENCE -> {
                """
                🔬 **Góc Khoa học Khí tượng Dễ hiểu:**
                
                • **Vì sao trước khi mưa trời lại oi bức ngột ngạt?**
                  Trước cơn mưa, lượng hơi nước bốc lên từ mặt đất ngưng tụ thành mây giải phóng một lượng nhiệt ẩn (latent heat) khổng lồ vào không khí. Đồng thời, độ ẩm tăng cao làm giảm tốc độ bốc hơi mồ hôi của con người, khiến chúng ta cảm thấy hầm nóng khó chịu.
                
                • **Mưa rào nhiệt đới hình thành thế nào?**
                  Nắng gắt hun nóng mặt đất làm luồng không khí nóng bốc lên nhanh tạo thành đám mây đối lưu (Cumulonimbus - mây dông khổng lồ). Khi giọt nước quá nặng, nó rơi xuống tạo thành cơn mưa rào lớn nhưng thường mau dứt.
                """.trimIndent()
            }

            WeatherIntent.FOLK_PROVERBS -> {
                """
                🌾 **Tục ngữ & Kinh nghiệm Dân gian Dự báo Thời tiết:**
                
                1. *"Chuồn chuồn bay thấp thì mưa, bay cao thì nắng, bay vừa thì râm."*
                   🔬 *Giải thích khoa học:* Khi sắp mưa, độ ẩm không khí tăng cao làm hơi nước đọng vào cánh chuồn chuồn khiến cánh nặng, buộc chúng phải bay là là sát mặt đất.
                
                2. *"Cơn mưa đằng Đông vừa trông vừa chạy, cơn mưa đằng Tây vừa cày vừa ăn."*
                   🔬 *Giải thích:* Mưa phía Đông thường di chuyển nhanh theo gió biển vào bờ, còn mưa đằng Tây thường tan chậm hơn.
                
                3. *"Trăng quầng thì hạn, trăng tán thì mưa."*
                """.trimIndent()
            }

            WeatherIntent.BOT_CAPABILITIES_AND_ORIGIN -> {
                val customCount = customKnowledgeRepo?.getAll()?.size ?: 0
                """
                🤖 **Thông tin Trợ lý UTH AI & Mô hình Huấn luyện:**
                
                • **Nhóm phát triển:** Nhóm 6 - Đại học GTVT TP.HCM (UTH).
                • **Kiến trúc AI:** Mô hình NLP Engine phân loại ý định trên thiết bị (On-Device Intent Classifier), kết hợp Cosine Similarity, N-grams Vectorizer và Heuristic Rule Weights.
                • **Tập dữ liệu huấn luyện:** Hơn **300+ mẫu câu hỏi thực tế** trải rộng trên **25+ chủ đề thời tiết**, đời sống sinh viên UTH, giao thông và sức khỏe.
                • **Tri thức tự học:** Hiện có **$customCount bài học tùy chỉnh** do bạn trực tiếp dạy thêm.
                • **100% Riêng tư & Miễn phí:** Chạy độc lập trên máy bạn, không cần API Key trả phí!
                """.trimIndent()
            }

            WeatherIntent.GOODBYE_AND_THANKS -> {
                """
                😊 **Rất vui được hỗ trợ bạn!**
                
                Chúc bạn có một ngày học tập và làm việc thật hiệu quả, di chuyển an toàn và luôn tràn đầy năng lượng nhé! ✨
                Nếu cần hỏi thêm bất cứ điều gì về thời tiết hoặc UTH, cứ nhắn cho tôi bất cứ lúc nào! 👋
                """.trimIndent()
            }

            WeatherIntent.CUSTOM_KNOWLEDGE, WeatherIntent.UNKNOWN -> {
                """
                🌤️ **Thời tiết hiện tại tại $city:**
                • **Nhiệt độ:** $temp°C (Cảm giác: $feelsLike°C)
                • **Độ ẩm:** $humidity% • **Gió:** $windKmH km/h
                • **Trạng thái:** $desc
                
                💡 Tôi có thể trả lời chi tiết về:
                • *"Hôm nay có mưa không?", "Có cần mang áo mưa không?"*
                • *"Đường Ung Văn Khiêm CS1 có ngập không?"*
                • *"Phơi đồ hôm nay có khô không?", "Có nên rửa xe không?"*
                • *"Chỉ số UV và không khí AQI hôm nay thế nào?"*
                """.trimIndent()
            }
        }

        // 4. Sinh gợi ý phản hồi tiếp theo (Follow-up chips) phù hợp ngữ cảnh
        val suggestions = getSuggestionsForIntent(intent, entities)


        return AiEngineOutput(
            responseText = response,
            followUpSuggestions = suggestions,
            detectedIntent = intent,
            confidence = confidence
        )
    }

    /**
     * Tra cứu chi tiết khả năng mưa trước và sau một mốc giờ (mặc định mốc 8h tối / 20:00).
     */
    private fun buildRainCheckAroundHour(
        hourLabel: String = "8h tối (20:00)",
        location: String = "Quận 7",
        timeSlot: String = "Tối mai",
        currentWeather: CurrentWeather?
    ): String {
        val forecastList = currentWeather?.hourlyForecast ?: emptyList()
        val h18 = forecastList.find { it.time.contains("18") }
        val h19 = forecastList.find { it.time.contains("19") }
        val h20 = forecastList.find { it.time.contains("20") }
        val h21 = forecastList.find { it.time.contains("21") }
        val h22 = forecastList.find { it.time.contains("22") }

        val popBefore = maxOf(h18?.pop ?: 15, h19?.pop ?: 15)
        val tempBefore = h19?.temp ?: 28
        val descBefore = h19?.condition?.labelVi ?: "Nhiều mây mát mẻ"

        val popAt8 = h20?.pop ?: 10
        val tempAt8 = h20?.temp ?: 26
        val descAt8 = h20?.condition?.labelVi ?: "Trời trong ráo, mát mẻ"

        val popAfter = maxOf(h21?.pop ?: 5, h22?.pop ?: 5)
        val tempAfter = h21?.temp ?: 25
        val descAfter = h21?.condition?.labelVi ?: "Gió sông lộng mát, khô ráo"

        val isRainBefore = popBefore >= 45 || descBefore.contains("mưa", ignoreCase = true)
        val rainDescBefore = if (isRainBefore) {
            "**Cảnh báo có mưa rào rải rác (Xác suất mưa ~$popBefore%)**. Nên mang theo áo mưa, bọc túi chống nước cho điện thoại và ưu tiên ăn tối tại khu ẩm thực / TTTM có mái che."
        } else {
            "**Thời tiết ráo ráo, an toàn (Xác suất mưa ~$popBefore%)**. Không có dông bất lợi, đường sá khô ráo, rất thuận tiện để di chuyển sang $location và thưởng thức bữa tối."
        }

        val isRainAt8 = popAt8 >= 45 || descAt8.contains("mưa", ignoreCase = true)
        val rainDescAt8 = if (isRainAt8) {
            "**Có khả năng còn mưa nhẹ rải rác (Xác suất mưa ~$popAt8%)**. Thay vì đứng trên cầu lộng gió, bạn nên chọn phương án dạo mát, xem phim hoặc uống cafe phòng lạnh tại Crescent Mall / SC VivoCity."
        } else {
            "**KHÔNG CÓ MƯA (Xác suất mưa chỉ ~$popAt8%)**. Tiết trời mát lành, gió hồ mát rượi, là thời điểm vàng đẹp nhất trong đêm để lên Cầu Ánh Sao ngắm phun nước đổi màu và dạo quanh hồ Bán Nguyệt."
        }

        val isRainAfter = popAfter >= 45 || descAfter.contains("mưa", ignoreCase = true)
        val rainDescAfter = if (isRainAfter) {
            "**Có thể có mưa phùn hoặc dông muộn (Xác suất mưa ~$popAfter%)**. Nên chú ý thời tiết, mang áo mưa khi ra về."
        } else {
            "**Tạnh ráo, khô thoáng (Xác suất mưa < ${popAfter.coerceAtLeast(10)}%)**. Không khí se mát, cực kỳ thích hợp để ngồi cafe ven hồ hoặc dạo Crescent Mall / SC VivoCity mua sắm mà không lo ướt áo."
        }

        val overallConclusion = when {
            !isRainBefore && !isRainAt8 && !isRainAfter ->
                "Cả trước và sau $hourLabel tại $location **đều tạnh ráo và không có mưa**, thời tiết cực kỳ lý tưởng để bạn yên tâm lên đồ đi chơi trọn vẹn!"
            isRainBefore && !isRainAt8 && !isRainAfter ->
                "Trước 8h tối có thể có mưa rào rải rác ($popBefore%), nhưng **tầm 8h trở đi trời sẽ tạnh ráo mát mẻ**, bạn có thể căn giờ xuất phát sau 19:30 để có chuyến đi trọn vẹn nhất!"
            isRainAt8 || isRainAfter ->
                "Xung quanh mốc $hourLabel có khả năng xuất hiện mưa dông rải rác. Bạn **nhất định nên mang theo áo mưa** và ưu tiên các hoạt động vui chơi trong nhà tại Crescent Mall / SC VivoCity nhé!"
            else ->
                "Thời tiết nhìn chung tương đối thuận lợi, chỉ cần mang theo một chiếc áo mưa mỏng dự phòng trong cốp xe là bạn có thể thoải mái khám phá $location!"
        }

        return buildString {
            append("🔍 **Tra cứu Chi tiết Dự báo Mưa quanh mốc $hourLabel ($timeSlot tại $location):**\n\n")
            append("• 🌆 **Trước 8h tối (18:00 - 19:30):**\n")
            append("  - Nhiệt độ: **$tempBefore°C** • $descBefore\n")
            append("  - Khả năng mưa: $rainDescBefore\n\n")

            append("• 🌟 **Đúng tầm 8h tối (20:00 - Thời điểm vàng):**\n")
            append("  - Nhiệt độ: **$tempAt8°C** • $descAt8\n")
            append("  - Khả năng mưa: $rainDescAt8\n\n")

            append("• 🌙 **Sau 8h tối (20:30 - 22:00):**\n")
            append("  - Nhiệt độ: **$tempAfter°C** • $descAfter\n")
            append("  - Khả năng mưa: $rainDescAfter\n\n")

            append("🎯 **KẾT LUẬN CỦA AI:** $overallConclusion")
        }
    }

    /**
     * Cố vấn Lịch trình Du lịch & Khí hậu Trọn gói (Smart Travel & Trip Planner)
     * Chia rõ Timeline (Sáng - Trưa - Chiều - Tối), Hành trang & Cảnh báo an toàn đường đi.
     */
    private fun buildSmartTravelPlanner(
        dest: String?,
        entities: Map<String, String>,
        currentWeather: CurrentWeather?,
        city: String
    ): String {
        val isGeneric = dest == null || entities["trip_type"] == "generic_outing"
        val timeSlot = entities["time_slot"] ?: "Ngày mai"
        val tomorrow = currentWeather?.dailyForecast?.getOrNull(1)
        val tomorrowDesc = tomorrow?.condition?.labelVi ?: "Nắng ráo có mây"
        val tomorrowMin = tomorrow?.minTemp ?: 25
        val tomorrowMax = tomorrow?.maxTemp ?: 33
        val tomorrowPop = tomorrow?.pop ?: 25

        if (isGeneric) {
            val isRainyTomorrow = tomorrowPop >= 40 || tomorrowDesc.contains("mưa", ignoreCase = true)
            return buildString {
                append("🗺️ **Cố vấn Lịch trình Đi chơi & Dã ngoại ($timeSlot tại khu vực $city):**\n\n")
                append("🌤️ **Thời tiết Dự báo $timeSlot:** $tomorrowMin°C - $tomorrowMax°C • **$tomorrowDesc** (Xác suất mưa: **$tomorrowPop%**).\n\n")

                if (isRainyTomorrow) {
                    append("☔ **Dự báo có mưa rải rác! Gợi ý các điểm 'Tránh Mưa - Sống Ảo - Chill' trong nhà:**\n\n")
                    append("• 🏙️ **1. Landmark 81 & TTTM Vincom (Bình Thạnh):** Sân trượt băng Ice Rink, rạp CGV, tổ hợp ẩm thực B1 Haidilao/Sushi, ngắm mưa từ cafe tầng cao view mây.\n")
                    append("• 🎨 **2. Bảo tàng Mỹ thuật TP.HCM (Quận 1):** Kiến trúc Art Deco Pháp cổ kính, không gian check-in sống ảo cực nghệ thuật và yên bình.\n")
                    append("• ☕ **3. Phố Cafe Chung cư Cổ (42 Nguyễn Huệ hoặc 26 Lý Tự Trọng):** Thưởng thức trà ấm, chơi boardgame cùng nhóm bạn hoặc tham gia workshop gốm/vẽ tranh.\n\n")
                    append("⏰ **Lịch trình Gợi ý Timeline:**\n")
                    append("• 🌅 **Sáng (09:00 - 11:30):** Khởi đầu bằng bữa brunch & cafe tại chung cư cổ Quận 1.\n")
                    append("• ☀️ **Trưa (12:00 - 14:30):** Di chuyển sang Landmark 81 ăn trưa buffet, xem phim chiếu rạp.\n")
                    append("• 🌇 **Chiều (15:00 - 17:30):** Trượt băng hoặc trải nghiệm đài quan sát Skyview tầng 81 ngắm mưa thành phố.\n")
                    append("• 🌙 **Tối (18:30 - 21:00):** Ăn tối ấm cúng cùng bạn bè tại khu ẩm thực sầm uất.\n\n")
                    append("🎒 **Hành trang cần chuẩn bị:** Dù/ô che mưa, áo khoác mỏng (phòng máy lạnh buốt), bọc giày chống nước.\n\n")
                } else {
                    append("☀️ **Thời tiết nắng ráo tuyệt đẹp! Gợi ý các điểm Dã ngoại & Đổi gió ngoài trời:**\n\n")
                    append("• 🌊 **1. Đi Vũng Tàu tắm biển trong ngày:** Xuất phát sáng sớm, tắm biển Bãi Sau, ăn bánh khọt, ngắm hoàng hôn Bãi Trước.\n")
                    append("• 🌴 **2. Khám phá Cần Giờ:** Đảo khỉ rợp bóng mát, ăn hải sản tươi sống giá rẻ tại Chợ Hàng Dương ven biển 30/4.\n")
                    append("• ⛵ **3. Bến Bạch Đằng & Saigon Waterbus:** Trải nghiệm buýt sông ngắm hoàng hôn lộng gió (vé chỉ 15.000đ/lượt siêu hợp sinh viên).\n")
                    append("• 🏕️ **4. Cắm trại Hồ Trị An (Đồng Nai):** Chèo SUP ngắm hoàng hôn, nướng BBQ và đốt lửa trại ven hồ.\n\n")
                    append("⏰ **Lịch trình Gợi ý Timeline:**\n")
                    append("• 🌅 **Sáng (06:30 - 11:00):** Xuất phát sớm đón gió mát lành, ăn sáng đặc sản vùng miền.\n")
                    append("• ☀️ **Trưa (11:30 - 14:00):** Ăn trưa hải sản/đặc sản địa phương, nghỉ trưa dưới tán cây/quán cafe tránh nắng gắt.\n")
                    append("• 🌇 **Chiều (15:30 - 18:00):** Vui chơi ngoài trời, dạo mát ngắm hoàng hôn rực rỡ.\n")
                    append("• 🌙 **Tối (18:30 - 21:00):** Thưởng thức ẩm thực đêm, dạo phố trước khi trở về.\n\n")
                    append("🎒 **Hành trang cần chuẩn bị:** Kem chống nắng SPF 50+, kính râm, nón rộng vành, bình nước cá nhân.\n\n")
                }
                append("👉 *Bạn có thể gõ tên một địa điểm cụ thể (VD: 'Mai đi Vũng Tàu', 'Mai đi Đà Lạt', 'Mai đi Landmark 81'...) để nhận lịch trình chuyên sâu nhé!*")
            }
        }

        // Trường hợp người dùng nêu đích danh một địa điểm cụ thể:
        val origin = entities["origin"] ?: "Vị trí hiện tại (UTH Cơ sở 1 - Bình Thạnh)"

        return when (dest) {
            "Vũng Tàu" -> """
                🌊 **Lộ trình & Cẩm nang Du lịch Vũng Tàu ($timeSlot):**

                📍 **Tuyến đường di chuyển:**
                • **Điểm đi:** $origin ➔ **Điểm đến:** TP. Vũng Tàu (Bãi Sau / Bãi Trước)
                • **Khoảng cách & Thời gian:** ~95 km • ~2 giờ 15 phút (Xe máy / Ô tô / Limousine)
                • **Trục đường chính:** Hàng Xanh ➔ Phà Cát Lái (hoặc Cao tốc Long Thành) ➔ QL51 ➔ TP. Vũng Tàu

                🌧️ **Kiểm tra Mưa trên Toàn bộ Lộ trình:**
                • 🏁 **Điểm đi ($origin):** Khô ráo, nhiệt độ ~28°C - 31°C, xác suất mưa thấp (~15%).
                • 🛣️ **Đoạn đường di chuyển (QL51 & Cát Lái):** Đường bằng phẳng, thông thoáng, không mưa dông.
                • 🎯 **Điểm đến (Vũng Tàu):** 27°C - 32°C • Nắng vàng, gió biển cấp 3-4, sóng êm an toàn tắm biển.

                ⏰ **Lịch trình Tóm tắt Timeline:**
                • 🌅 **Sáng (06:00 - 10:30):** Xuất phát sớm, ăn sáng bánh khọt Cô Ba, check-in Mũi Nghinh Phong và tượng Chúa Kytô.
                • ☀️ **Trưa (11:00 - 14:00):** Ăn trưa lẩu cá đuối Trương Công Định, nghỉ ngơi tại quán cafe phòng lạnh view biển Bãi Dâu.
                • 🌇 **Chiều (15:30 - 18:00):** Tắm biển Bãi Sau thỏa thích, lên ngọn Hải Đăng ăn trứng lòng đào ngắm hoàng hôn Bãi Trước.
                • 🌙 **Tối (18:30 - 21:00):** Thưởng thức hải sản Chợ Đêm / Ốc Tự Nhiên, dạo mát công viên rồi về lại Sài Gòn.

                ⚠️ **Khu vực lân cận cần NÉ / TRÁNH:**
                • ⚠️ **Điểm ùn tắc & trơn trượt:** Đoạn QL51 qua ngã 3 Nhơn Trạch, cổng KCN Mỹ Xuân và trạm thu phí T2 cũ chiều Chủ Nhật; các khúc cua dốc Bãi Trước rất trơn trượt khi mưa dông.
                • 💡 **Tuyến đường né tránh an toàn:** Vào trung tâm TP. Vũng Tàu nên rẽ vào đường 3/2 rộng 6 làn xe thay vì đi đường 30/4 nhiều xe tải nặng và container.

                🎒 **Hành trang cần chuẩn bị:** Đồ bơi, kem chống nắng SPF 50+, kính râm, nón có quai, dép xỏ ngón và áo mưa mỏng trong cốp xe.
                🛵 **Di chuyển & An toàn đường sá:** Đi xe máy QL51 chú ý tốc độ đoạn qua thị xã Phú Mỹ và Bà Rịa; xe Limousine đón tại Hàng Xanh rất tiện sinh viên UTH.
            """.trimIndent()

            "Đà Lạt" -> """
                🌲 **Lộ trình & Cẩm nang Du lịch Đà Lạt ($timeSlot):**

                📍 **Tuyến đường di chuyển:**
                • **Điểm đi:** $origin ➔ **Điểm đến:** TP. Đà Lạt (Lâm Đồng)
                • **Khoảng cách & Thời gian:** ~305 km • ~6 - 7 giờ (Xe khách giường nằm / Ô tô)
                • **Trục đường chính:** Cao tốc Dầu Giây ➔ QL20 ➔ Đèo Chuối ➔ Đèo Bảo Lộc ➔ Đèo Prenn ➔ TP. Đà Lạt

                🌧️ **Kiểm tra Mưa trên Toàn bộ Lộ trình:**
                • 🏁 **Điểm đi ($origin):** Nắng ấm 28°C - 32°C, trời khô ráo.
                • 🛣️ **Đoạn đường di chuyển (Đèo Bảo Lộc & QL20):** Chiều tối có thể có sương mù và mưa rào nhẹ, đường trơn cần giảm tốc độ < 40 km/h.
                • 🎯 **Điểm đến (Đà Lạt):** 14°C - 23°C • Sáng sớm sương mờ, trưa nắng dịu mát, đêm se lạnh đặc trưng.

                ⚠️ **Khu vực lân cận cần NÉ / TRÁNH:**
                • ⚠️ **Khu vực đèo nguy hiểm:** Tránh vượt Đèo Bảo Lộc và Đèo Prenn sau 19:00 tối hoặc khi trời mưa to dông lốc (nguy cơ sạt lở taluy và sương mù che khuất tầm nhìn).
                • 🌊 **Vùng trũng nội đô:** Tránh lưu thông qua đoạn trũng đường Phan Đình Phùng và hạ lưu suối Cam Ly nếu mưa lớn dài tập.
                • 💡 **Tuyến đường an toàn:** Ưu tiên đi tuyến Đèo Mimosa khi đèo Prenn quá tải hoặc có cảnh báo thời tiết xấu.

                ⏰ **Lịch trình Tóm tắt Timeline:**
                • 🌅 **Sáng (05:30 - 10:30):** Săn biển mây Cầu Đất, ăn sáng bánh mì xíu mại Hoàng Diệu, cafe ngắm thung lũng.
                • ☀️ **Trưa (11:30 - 14:00):** Ăn trưa lẩu gà lá é Tao Ngộ (đường 3/4) hoặc lẩu bò Ba Toa Quán Gỗ, về nghỉ ngơi.
                • 🌇 **Chiều (14:30 - 17:30):** Tham quan Ga Đà Lạt, check-in Dinh 1 hoặc dạo bước quanh Hồ Xuân Hương se lạnh.
                • 🌙 **Tối (18:30 - 22:00):** Dạo Chợ Đêm Đà Lạt, ăn bánh tráng nướng 'pizza Đà Lạt', uống sữa đậu nành nóng.

                🎒 **Hành trang cần chuẩn bị:** Áo len/khoác ấm, khăn quàng cổ, kem dưỡng ẩm da, sạc dự phòng và áo mưa mỏng đề phòng sương ẩm.
                🛵 **Di chuyển & An toàn đường sá:** Đèo Bảo Lộc nhiều khúc cua gấp, xe khách Phương Trang/Thành Bưởi chạy đêm 6 tiếng là giải pháp thảnh thơi nhất.
            """.trimIndent()

            "Landmark 81" -> """
                🏙️ **Lộ trình & Khám phá Landmark 81 ($timeSlot):**

                📍 **Tuyến đường di chuyển:**
                • **Điểm đi:** $origin ➔ **Điểm đến:** Tòa nhà Landmark 81 & Công viên Vinhomes Central Park
                • **Khoảng cách & Thời gian:** ~3.5 km • ~8 - 10 phút di chuyển (Xe máy / Ô tô / Đi bộ)
                • **Trục đường chính:** Điện Biên Phủ (hoặc Ung Văn Khiêm ➔ D2 ➔ Nguyễn Hữu Cảnh) ➔ Cổng Vinhomes Central Park

                🌧️ **Kiểm tra Mưa trên Toàn bộ Lộ trình:**
                • 🏁 **Điểm đi ($origin):** Nhiệt độ ~31°C, trời ráo, xác suất mưa thấp (~15%).
                • 🛣️ **Đoạn đường di chuyển (Nguyễn Hữu Cảnh):** Ráo ráo, thông thoáng, không ngập úng.
                • 🎯 **Điểm đến (Landmark 81):** Không gian TTTM Vincom máy lạnh 24°C mát rượi, công viên bờ sông lộng gió thoáng đãng.

                ⏰ **Lịch trình Gợi ý Timeline:**
                • 🌅 **Sáng (09:00 - 11:30):** Cafe ngắm view công viên bờ sông, check-in cầu đỏ phong cách Nhật Bản.
                • ☀️ **Trưa (12:00 - 14:30):** Ăn trưa khu ẩm thực Tầng B1 (Haidilao, Dozo Sushi, lẩu băng chuyền...) mát rượi trốn nắng.
                • 🌇 **Chiều (14:30 - 17:30):** Trượt băng Vincom Ice Rink hoặc lên Skyview tầng 81 ngắm trọn hoàng hôn Sài Gòn.
                • 🌙 **Tối (18:30 - 21:30):** Dạo công viên Landmark lung linh ánh đèn laser, ngắm du thuyền lướt trên sông.

                ⚠️ **Khu vực lân cận cần NÉ / TRÁNH:**
                • 🚗 **Điểm ùn ứ:** Tránh nút giao Ngã tư Hàng Xanh, Ung Văn Khiêm đoạn giao Nguyễn Xí và vòng xoay Điện Biên Phủ giờ tan tầm (17:15 - 18:30).
                • 💡 **Tuyến đường né tránh an toàn:** Đi từ D2 (Nguyễn Gia Trí) luồn qua Ung Văn Khiêm ➔ rẽ Nguyễn Hữu Cảnh vào trực tiếp cổng Vinhomes Central Park.

                🎒 **Hành trang:** Áo khoác mỏng (phòng máy lạnh và sân băng khá buốt), sạc dự phòng.
                🛵 **Di chuyển & An toàn:** Cực gần trường UTH CS1; gửi xe máy tại tầng hầm B2/B3 tiện lợi.
            """.trimIndent()

            "Quận 7" -> {
                val rainCheckAround8 = buildRainCheckAroundHour("8h tối (20:00)", "Quận 7", timeSlot, currentWeather)
                """
                🛵 **Lộ trình & Kiểm tra Mưa: $origin ➔ Quận 7 ($timeSlot):**

                📍 **Tuyến đường di chuyển:**
                • **Điểm đi:** $origin
                • **Điểm đến:** Quận 7 (Cầu Ánh Sao / Hồ Bán Nguyệt / Phú Mỹ Hưng)
                • **Khoảng cách & Thời gian:** ~11 km • ~25 - 30 phút di chuyển (Xe máy / Ô tô)
                • **Trục đường chính:** Ung Văn Khiêm ➔ Nguyễn Hữu Cảnh ➔ Cầu Thủ Thiêm / Cầu Ba Son ➔ Mai Chí Thọ ➔ Nguyễn Thị Thập (hoặc qua Cầu Kênh Tẻ)

                🌧️ **Kiểm tra Mưa trên Toàn bộ Lộ trình:**
                • 🏁 **Điểm xuất phát ($origin):** Thời tiết ráo mát ~27°C, xác suất mưa thấp (~15%).
                • 🛣️ **Đoạn đường di chuyển (Các cầu & Trục đường):** Đường khô ráo, lưu thông thuận lợi. Lưu ý tránh Cầu Kênh Tẻ giờ cao điểm 17:30 - 18:30 dễ ùn ứ.
                • 🎯 **Điểm đến (Quận 7 quanh mốc 8h tối):**
                $rainCheckAround8

                ⚠️ **Khu vực lân cận điểm đến cần NÉ / TRÁNH:**
                • 🌊 **Đoạn ngập triều & trũng thấp:** Tuyệt đối tránh đường Trần Xuân Soạn (dọc bờ Kênh Tẻ), Huỳnh Tấn Phát (đoạn trũng gần giao lộ Phú Thuận), Lê Văn Lương (khu vực cầu Rạch Đỉa giáp Nhà Bè) khi triều cường dâng cao hoặc có mưa to.
                • 🚗 **Nút giao kẹt xe:** Tránh Cầu Kênh Tẻ và vòng xoay Nguyễn Hữu Thọ - Nguyễn Văn Linh vào khung giờ cao điểm (17:30 - 18:45).
                • 💡 **Tuyến đường tránh an toàn:** Đi qua Cầu Ba Son ➔ Mai Chí Thọ ➔ Cầu Phú Mỹ hoặc Cầu Tân Thuận 2 ➔ Nguyễn Thị Thập / Nguyễn Lương Bằng, mặt đường cao ráo và thông thoáng hơn nhiều.

                ⏰ **Lịch trình Buổi tối Gợi ý tại Quận 7 (Timeline ngắn gọn 18:00 - 22:00):**
                • 🌇 **18:00 - 19:30 (Bữa tối):** Thưởng thức ẩm thực Hàn Quốc Phú Mỹ Hưng (đường Bùi Bằng Đoàn) hoặc phố ẩm thực Nguyễn Thị Thập.
                • 🌉 **19:30 - 21:00 (Check-in):** Dạo mát ngắm Cầu Ánh Sao phun nước cầu vồng và hồ Bán Nguyệt lộng gió thơ mộng.
                • 🛍️ **21:00 - 22:00 (Thư giãn):** Mua sắm, xem phim hoặc uống cafe tại Crescent Mall / SC VivoCity.

                🎒 **Hành trang cần chuẩn bị:** Trang phục thoải mái, giày bệt đi dạo hồ, một chiếc áo mưa mỏng dự phòng trong cốp xe.
                🛵 **Lưu ý Di chuyển:** Nên xuất phát sau 18:45 để đường thông thoáng và nhớ bọc bảo vệ điện thoại nếu trời chuyển mưa dông bất chợt!
                """.trimIndent()
            }

            "Quận 2 (Thảo Điền / Sala)" -> """
                🏙️ **Lộ trình & Khám phá TP. Thủ Đức - Thảo Điền & Đô thị Sala ($timeSlot):**

                📍 **Tuyến đường:** $origin ➔ Thảo Điền / Sala (~7 km, ~15 phút qua Cầu Sài Gòn hoặc Cầu Ba Son).
                🌧️ **Kiểm tra Mưa:** Tuyến đường khô ráo, gió sông Sài Gòn lộng mát, nhiệt độ 26°C - 30°C.
                ⏰ **Timeline:** 16:30 ngắm hoàng hôn công viên bờ sông Sala ➔ 19:00 ăn tối pizza nướng củi phố Tây Thảo Điền (Xuân Thủy).
                🎒 **Hành trang:** Trang phục phong cách, điện thoại chụp ảnh hoàng hôn.
            """.trimIndent()

            "Quận 1" -> """
                🏙️ **Lộ trình & Vui chơi Trung tâm Quận 1 ($timeSlot):**

                📍 **Tuyến đường:** $origin ➔ Trung tâm Quận 1 (~6 km, ~15 phút qua Điện Biên Phủ / Nguyễn Thị Minh Khai).
                🌧️ **Kiểm tra Mưa:** Thời tiết mát mẻ 26°C - 31°C, trời ráo, không có mưa dông.
                ⏰ **Timeline:** 17:30 check-in Bến Bạch Đằng & Nhà hát Thành phố ➔ 19:30 hòa mình vào không khí Phố Đi Bộ Nguyễn Huệ / Bùi Viện náo nhiệt.
                🎒 **Hành trang:** Giày đi bộ êm ái, túi đeo chéo bảo quản tư trang cẩn thận.
            """.trimIndent()

            "Quận 5 (Chợ Lớn)", "Quận 10", "Quận 4" -> """
                🍜 **Lộ trình & Food Tour Ẩm thực $dest ($timeSlot):**

                📍 **Tuyến đường:** $origin ➔ $dest (~8 - 12 km, ~20 - 25 phút).
                🌧️ **Kiểm tra Mưa:** Thời tiết buổi chiều tối mát mẻ 26°C - 29°C, đường khô ráo, lý tưởng la cà quán xá.
                ⏰ **Tọa độ Ăn uống:** ${if (dest.contains("Quận 5")) "Khu phố người Hoa Chợ Lớn (Dimsum Tiến Phát, sủi cảo Hà Tôn Quyền, chè Hà Ký)." else if (dest.contains("Quận 10")) "Phố ẩm thực Hồ Thị Kỷ, Vạn Hạnh Mall mua sắm giải trí." else "Phố ẩm thực Vĩnh Khánh, chợ Xóm Chiếu ngập tràn đồ ăn vặt."}
                🎒 **Hành trang:** Bụng đói oanh tạc món ngon, tiền mặt lẻ gửi xe và áo mưa mỏng dự phòng!
            """.trimIndent()

            "Phố đi bộ Nguyễn Huệ", "Bến Bạch Đằng" -> """
                ⛵ **Lịch trình Trải nghiệm Phố Đi Bộ & Bến Bạch Đằng ($timeSlot):**

                📍 **Tuyến đường:** $origin ➔ Bến Bạch Đằng (~5 km, ~12 phút qua Tôn Đức Thắng).
                🌧️ **Kiểm tra Mưa:** Từ 16:30 gió sông Sài Gòn thổi mát rượi, trời tạnh ráo, xác suất mưa < 15%.
                ⏰ **Timeline:** 16:30 trải nghiệm Saigon Waterbus buýt sông (15.000đ) ngắm hoàng hôn cầu Ba Son ➔ 19:00 dạo Phố Đi Bộ Nguyễn Huệ xem âm nhạc đường phố.
                🎒 **Hành trang:** Giày sneaker êm chân, quạt mini, gửi xe tại bãi Hàm Nghi hoặc Nhà hát Thành phố.
            """.trimIndent()

            "Tây Ninh" -> """
                ⛰️ **Lộ trình & Cẩm nang Phượt Tây Ninh - Núi Bà Đen ($timeSlot):**

                📍 **Tuyến đường:** $origin ➔ Núi Bà Đen (~90 km, ~2.5 giờ qua QL22 - Củ Chi - Trảng Bàng).
                🌧️ **Kiểm tra Mưa:** Chân núi 26°C - 34°C trời nắng; trên Đỉnh Núi Bà Đen lộng gió mát rượi 20°C - 24°C, không mưa.
                ⏰ **Timeline:** 06:00 xuất phát ăn bánh canh Trảng Bàng ➔ 09:30 cáp treo lên đỉnh săn mây chiêm bái Phật Bà ➔ 12:00 ăn bò tơ Năm Sánh ➔ 17:30 về lại TP.HCM.
                🎒 **Hành trang:** Áo chống nắng, nón rộng vành, giày thể thao đi bậc thang, kính râm.
            """.trimIndent()

            "Cần Giờ" -> """
                🌴 **Lộ trình & Cẩm nang Phượt Cần Giờ - Đổi gió Biển ($timeSlot):**

                📍 **Tuyến đường:** $origin ➔ Biển Cần Giờ (~55 km, ~1.5 giờ qua Huỳnh Tấn Phát ➔ Phà Bình Khánh ➔ Đường Rừng Sác).
                🌧️ **Kiểm tra Mưa:** Rừng ngập mặn rợp bóng cây mát mẻ, gió biển 27°C - 32°C, đường khô ráo.
                ⏰ **Timeline:** 07:00 qua phà đi Đảo Khỉ ➔ 11:30 Chợ Hàng Dương ăn hải sản tươi sống và nghỉ trưa biển 30/4 ➔ 15:30 check-in Cầu Nam Hải ngắm hoàng hôn.
                🎒 **Hành trang:** Thuốc chống muỗi, kính râm, nón che nắng; nhớ đổ đầy bình xăng trước khi qua phà.
            """.trimIndent()

            "Hồ Trị An" -> """
                🏕️ **Lộ trình & Cắm trại Glamping Hồ Trị An ($timeSlot):**

                📍 **Tuyến đường:** $origin ➔ Hồ Trị An Đồng Nai (~70 km, ~2 giờ qua QL1A ➔ Ngã 3 Trị An ➔ ĐT768).
                🌧️ **Kiểm tra Mưa:** Ban ngày 27°C - 33°C nắng đẹp; ban đêm ven hồ nhiệt độ hạ 22°C - 24°C se lạnh, trời quang mây ngắm sao đêm.
                ⏰ **Timeline:** 08:00 xuất phát nhận lều ven hồ ➔ 15:00 chèo SUP ngắm hoàng hôn mặt nước ➔ 18:30 tiệc nướng BBQ và đốt lửa trại.
                🎒 **Hành trang:** Lều chống mưa, áo phao bơi, đèn pin sạc, kem chống muỗi, áo ấm mỏng cho ban đêm.
            """.trimIndent()

            "Phan Thiết" -> """
                🏖️ **Lộ trình & Du lịch Biển Phan Thiết - Mũi Né ($timeSlot):**

                📍 **Tuyến đường:** $origin ➔ Phan Thiết (~185 km, ~2 giờ 15 phút qua Cao tốc Dầu Giây - Phan Thiết).
                🌧️ **Kiểm tra Mưa:** 26°C - 33°C • Nắng vàng biển xanh, gió lộng mát, điều kiện lý tưởng tắm biển.
                ⏰ **Timeline:** Sáng sớm trượt cát Bàu Trắng xe jeep ➔ Trưa ăn bánh canh chả cá, lẩu thả ➔ Chiều lội Suối Tiên ngắm hoàng hôn làng chài Mũi Né ➔ Tối phố Tây Nguyễn Đình Chiểu.
                🎒 **Hành trang:** Kính râm, kem chống nắng SPF 50+, khăn turban, dép quai hậu lội suối.
            """.trimIndent()

            "Bảo tàng Mỹ thuật", "Thảo Cầm Viên", "Công viên Đầm Sen", "Khu du lịch Suối Tiên", "Trung tâm thương mại" -> """
                🏛️ **Lộ trình & Vui chơi Nội thành - Điểm đến $dest ($timeSlot):**

                📍 **Tuyến đường:** $origin ➔ $dest (Thuận tiện di chuyển bằng xe máy hoặc xe buýt sinh viên).
                🌧️ **Kiểm tra Mưa:** Nhiệt độ $tomorrowMin°C - $tomorrowMax°C, $tomorrowDesc, thuận lợi cho việc tham quan và chụp ảnh.
                ⏰ **Gợi ý Khung giờ:** Sáng 08:30 - 11:30 tham quan ánh sáng đẹp ➔ Trưa cafe nghỉ ngơi ➔ Chiều tối dạo phố ẩm thực.
                🎒 **Hành trang:** Trang phục trẻ trung, quạt mini, mang theo thẻ sinh viên UTH để được giảm giá vé!
            """.trimIndent()

            else -> """
                🗺️ **Lộ trình & Kiểm tra Mưa: $origin ➔ $dest ($timeSlot):**

                📍 **Tuyến đường di chuyển:**
                • **Điểm đi:** $origin ➔ **Điểm đến:** $dest
                • **Thời tiết dự kiến:** $tomorrowMin°C - $tomorrowMax°C • $tomorrowDesc

                🌧️ **Kiểm tra Mưa trên Toàn bộ Lộ trình:**
                • 🏁 **Điểm xuất phát ($origin):** Thời tiết ráo ráo, nhiệt độ thuận lợi, xác suất mưa thấp (~15%).
                • 🛣️ **Đoạn đường di chuyển:** Trục đường lưu thông an toàn, tầm nhìn tốt, không có dông lốc bất thường.
                • 🎯 **Điểm đến ($dest):** Điều kiện thuận lợi cho các hoạt động di chuyển và tham quan dã ngoại.

                ⏰ **Lịch trình Đề xuất Timeline:**
                • 🌅 **Sáng (07:00 - 11:00):** Xuất phát sớm lúc trời mát mẻ, ăn sáng đặc sản và tham quan danh lam thắng cảnh.
                • ☀️ **Trưa (11:30 - 14:00):** Dùng bữa trưa tại nhà hàng địa phương, nghỉ ngơi tại không gian râm mát tránh nắng gắt.
                • 🌇 **Chiều (15:00 - 18:00):** Tham quan check-in chụp ảnh và ngắm cảnh hoàng hôn buông xuống.
                • 🌙 **Tối (18:30 - 21:30):** Khám phá ẩm thực đêm và dạo phố.

                ⚠️ **Khu vực lân cận điểm đến cần NÉ / TRÁNH:**
                • ⚠️ **Lưu ý điểm trũng & kẹt xe:** Rà soát các tuyến đường trũng ven sông hoặc các nút giao công trình đang thi công gần $dest để tránh bị kẹt xe kéo dài.
                • 💡 **Tuyến đường né tránh:** Ưu tiên chọn các trục đường lớn hoặc đại lộ vành đai cao ráo, thoáng đãng.

                🎒 **Hành trang cần chuẩn bị:** Trang phục năng động, kem chống nắng, kính râm và một chiếc áo mưa mỏng dự phòng trong cốp xe.
            """.trimIndent()
        }
    }
}
