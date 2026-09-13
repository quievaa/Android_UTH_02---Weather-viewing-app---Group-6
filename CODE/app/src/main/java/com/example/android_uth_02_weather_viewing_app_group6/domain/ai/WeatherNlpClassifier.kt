package com.example.android_uth_02_weather_viewing_app_group6.domain.ai

import kotlin.math.sqrt

// Kết quả phân loại ý định từ câu hỏi người dùng.
data class ClassificationResult(
    val intent: WeatherIntent,
    val confidence: Float,
    val entities: Map<String, String> = emptyMap(),
    val matchedReason: String = ""
)

// Phân loại Ngôn ngữ Tự nhiên & Ý định.
class WeatherNlpClassifier {

    // Tiền xử lý tập dữ liệu huấn luyện thành vector N-grams cho từng Intent
    private val intentNgramVectors: Map<WeatherIntent, Map<String, Float>> by lazy {
        buildIntentVectors()
    }

    private fun buildIntentVectors(): Map<WeatherIntent, Map<String, Float>> {
        val result = mutableMapOf<WeatherIntent, Map<String, Float>>()

        for ((intent, samples) in WeatherIntentCorpus.trainingData) {
            val freqMap = mutableMapOf<String, Float>()
            for (sample in samples) {
                val ngrams = VietnameseTextNormalizer.extractNgrams(sample)
                for (ng in ngrams) {
                    freqMap[ng] = (freqMap[ng] ?: 0f) + 1f
                }
            }
            result[intent] = freqMap
        }
        return result
    }

    //Phân loại câu hỏi của người dùng thành Intent phù hợp nhất.
    fun classify(query: String): ClassificationResult {
        val raw = query.trim()
        if (raw.isBlank()) {
            return ClassificationResult(WeatherIntent.UNKNOWN, 0f)
        }

        val cleaned = VietnameseTextNormalizer.clean(raw)
        val unaccented = VietnameseTextNormalizer.removeAccents(cleaned)
        val queryNgrams = VietnameseTextNormalizer.extractNgrams(raw)
        val entities = extractEntities(unaccented, raw)

        //Kiểm tra khớp chính xác hoặc chứa toàn bộ mẫu câu huấn luyện
        for ((intent, samples) in WeatherIntentCorpus.trainingData) {
            for (sample in samples) {
                val sampleClean = VietnameseTextNormalizer.clean(sample)
                val sampleUnaccented = VietnameseTextNormalizer.removeAccents(sampleClean)
                if (unaccented == sampleUnaccented || unaccented.contains(sampleUnaccented) || cleaned.contains(sampleClean)) {
                    return ClassificationResult(
                        intent = intent,
                        confidence = 0.98f,
                        entities = entities,
                        matchedReason = "Trùng khớp mẫu câu huấn luyện: '$sample'"
                    )
                }
            }
        }

        //Tính điểm tương đồng Vector N-grams (Cosine Similarity) kết hợp High-Signal Keywords
        var bestIntent = WeatherIntent.GENERAL_OVERVIEW
        var maxScore = 0f
        var bestReason = ""

        //Điểm thưởng đặc biệt cho chủ đề Du lịch / Đi chơi vs Ngập úng
        val hasFloodKeyword = unaccented.contains("ngap") || unaccented.contains("trieu cuong")
        val hasTravelEntity = (entities.containsKey("destination") || entities.containsKey("trip_type")) && !hasFloodKeyword
        val hasTripKeyword = !hasFloodKeyword && (unaccented.contains("di choi") || unaccented.contains("di phuot") || 
                unaccented.contains("da ngoai") || unaccented.contains("lich trinh") || 
                unaccented.contains("cam trai") || unaccented.contains("check in") ||
                unaccented.contains("doan duong") || unaccented.contains("duong di") ||
                unaccented.contains("lo trinh") || unaccented.contains("di toi") ||
                unaccented.contains("di den") || unaccented.contains("tren duong"))

        for ((intent, intentVector) in intentNgramVectors) {
            val cosineSim = computeCosineSimilarity(queryNgrams, intentVector)

            // Điểm thưởng nếu chứa từ khóa tín hiệu cao (High-Signal Keyword Boost)
            var boost = 0f
            val keywords = WeatherIntentCorpus.highSignalKeywords[intent] ?: emptyList()
            for (kw in keywords) {
                if (unaccented.contains(kw) || cleaned.contains(kw)) {
                    boost += 0.35f
                }
            }

            // Thưởng thêm nếu hỏi về ngập úng UTH
            if (intent == WeatherIntent.UTH_FLOOD_AND_TRAFFIC && (hasFloodKeyword || entities.containsKey("campus"))) {
                boost += 0.85f
            }

            // Thưởng thêm nếu hỏi về đi chơi / du lịch
            if (intent == WeatherIntent.TRAVEL_TOURISM && (hasTravelEntity || hasTripKeyword)) {
                boost += 0.65f
            }

            val totalScore = cosineSim + boost

            if (totalScore > maxScore) {
                maxScore = totalScore
                bestIntent = intent
                bestReason = "Cosine Similarity (${"%.2f".format(cosineSim)}) + Boost (${"%.2f".format(boost)})"
            }
        }

        //Quy chuẩn ngưỡng tin cậy (Confidence Threshold)
        val finalConfidence = (maxScore).coerceIn(0f, 1f)
        if (finalConfidence < 0.25f) {
            return ClassificationResult(
                intent = WeatherIntent.GENERAL_OVERVIEW,
                confidence = 0.30f,
                entities = entities,
                matchedReason = "Dưới ngưỡng tin cậy tối thiểu, chuyển sang Tổng quan thời tiết"
            )
        }

        return ClassificationResult(
            intent = bestIntent,
            confidence = finalConfidence,
            entities = entities,
            matchedReason = bestReason
        )
    }

    // Tính toán Cosine Similarity giữa câu hỏi và vector huấn luyện của Intent.
    private fun computeCosineSimilarity(queryNgrams: Set<String>, intentVector: Map<String, Float>): Float {
        var dotProduct = 0f
        for (ng in queryNgrams) {
            val weight = intentVector[ng] ?: 0f
            dotProduct += weight
        }

        if (dotProduct == 0f) return 0f

        val queryMagnitude = sqrt(queryNgrams.size.toDouble()).toFloat()
        var intentSqSum = 0f
        for (w in intentVector.values) {
            intentSqSum += w * w
        }
        val intentMagnitude = sqrt(intentSqSum.toDouble()).toFloat()

        if (queryMagnitude == 0f || intentMagnitude == 0f) return 0f
        return dotProduct / (queryMagnitude * intentMagnitude)
    }

    // Trích xuất các thực thể quan trọng trong câu (Cơ sở UTH, Tỉnh thành, Điểm vui chơi, Thời gian, Hoạt động).
    private fun extractEntities(unaccented: String, raw: String): Map<String, String> {
        val entities = mutableMapOf<String, String>()

        // 1. Cơ sở UTH
        when {
            unaccented.contains("cs1") || unaccented.contains("co so 1") || unaccented.contains("ung van khiem") || unaccented.contains("binh thanh") -> {
                entities["campus"] = "Cơ sở 1 (Bình Thạnh - Ung Văn Khiêm)"
            }
            unaccented.contains("cs2") || unaccented.contains("co so 2") || unaccented.contains("to ky") || unaccented.contains("quan 12") -> {
                entities["campus"] = "Cơ sở 2 (Quận 12 - Tô Ký)"
            }
            unaccented.contains("cs3") || unaccented.contains("co so 3") || unaccented.contains("do xuan hop") || unaccented.contains("thu duc") -> {
                entities["campus"] = "Cơ sở 3 (TP. Thủ Đức - Đỗ Xuân Hợp)"
            }
        }

        // 2. Điểm đến du lịch ngoại tỉnh & Các quận huyện / Điểm vui chơi TP.HCM
        when {
            // Quận 7 và các tọa độ nổi tiếng
            unaccented.contains("quan 7") || unaccented.contains("q7") || unaccented.contains("phu my hung") ||
            unaccented.contains("cau anh sao") || unaccented.contains("ho ban nguyet") ||
            unaccented.contains("crescent mall") || unaccented.contains("sc vivocity") || unaccented.contains("vivocity") ->
                entities["destination"] = "Quận 7"

            // Các quận khác tại TP.HCM
            unaccented.contains("quan 1") || unaccented.contains("q1") || unaccented.contains("ben thanh") || unaccented.contains("bui vien") ->
                entities["destination"] = "Quận 1"
            unaccented.contains("quan 2") || unaccented.contains("q2") || unaccented.contains("thao dien") || unaccented.contains("sala") ->
                entities["destination"] = "Quận 2 (Thảo Điền / Sala)"
            unaccented.contains("quan 4") || unaccented.contains("q4") || unaccented.contains("vinh khanh") ->
                entities["destination"] = "Quận 4"
            unaccented.contains("quan 5") || unaccented.contains("q5") || unaccented.contains("cho lon") ->
                entities["destination"] = "Quận 5 (Chợ Lớn)"
            unaccented.contains("quan 10") || unaccented.contains("q10") || unaccented.contains("ho thi ky") || unaccented.contains("van hanh mall") ->
                entities["destination"] = "Quận 10"
            unaccented.contains("binh thanh") || unaccented.contains("thanh da") ->
                entities["destination"] = "Bình Thạnh"
            unaccented.contains("tp thu duc") || unaccented.contains("lang dai hoc") ->
                entities["destination"] = "TP. Thủ Đức"
            unaccented.contains("go vap") || unaccented.contains("phan van tri") ->
                entities["destination"] = "Gò Vấp"
            unaccented.contains("phu nhuan") || unaccented.contains("phan xich long") ->
                entities["destination"] = "Phú Nhuận"
            unaccented.contains("cu chi") || unaccented.contains("dia dao") ->
                entities["destination"] = "Củ Chi"

            // Điểm nội thành Sài Gòn
            unaccented.contains("landmark 81") || unaccented.contains("landmark") || unaccented.contains("vinhomes central park") ->
                entities["destination"] = "Landmark 81"
            unaccented.contains("pho di bo nguyen hue") || unaccented.contains("pho di bo") ->
                entities["destination"] = "Phố đi bộ Nguyễn Huệ"
            unaccented.contains("ben bach dang") || unaccented.contains("saigon waterbus") || unaccented.contains("waterbus") ->
                entities["destination"] = "Bến Bạch Đằng"
            unaccented.contains("thao cam vien") || unaccented.contains("so thu") ->
                entities["destination"] = "Thảo Cầm Viên"
            unaccented.contains("ho con rua") ->
                entities["destination"] = "Hồ Con Rùa"
            unaccented.contains("nha tho duc ba") || unaccented.contains("buu dien thanh pho") ->
                entities["destination"] = "Nhà thờ Đức Bà"
            unaccented.contains("bao tang my thuat") || unaccented.contains("bao tang") ->
                entities["destination"] = "Bảo tàng Mỹ thuật"
            unaccented.contains("dam sen") ->
                entities["destination"] = "Công viên Đầm Sen"
            unaccented.contains("suoi tien") ->
                entities["destination"] = "Khu du lịch Suối Tiên"
            unaccented.contains("thiso mall") || unaccented.contains("takashimaya") || unaccented.contains("aeon mall") ->
                entities["destination"] = "Trung tâm thương mại"

            // Điểm dã ngoại lân cận & Ngoại tỉnh
            unaccented.contains("can gio") || unaccented.contains("dao khi") ->
                entities["destination"] = "Cần Giờ"
            unaccented.contains("ho tri an") || unaccented.contains("tri an") ->
                entities["destination"] = "Hồ Trị An"
            unaccented.contains("tay ninh") || unaccented.contains("nui ba den") ->
                entities["destination"] = "Tây Ninh"
            unaccented.contains("suoi mo") ->
                entities["destination"] = "Suối Mơ"
            unaccented.contains("da lat") ->
                entities["destination"] = "Đà Lạt"
            unaccented.contains("vung tau") ->
                entities["destination"] = "Vũng Tàu"
            unaccented.contains("ha noi") ->
                entities["destination"] = "Hà Nội"
            unaccented.contains("da nang") ->
                entities["destination"] = "Đà Nẵng"
            unaccented.contains("phu quoc") ->
                entities["destination"] = "Phú Quốc"
            unaccented.contains("nha trang") ->
                entities["destination"] = "Nha Trang"
            unaccented.contains("sa pa") || unaccented.contains("sapa") ->
                entities["destination"] = "Sa Pa"
            unaccented.contains("can tho") ->
                entities["destination"] = "Cần Thơ"
            unaccented.contains("quy nhon") ->
                entities["destination"] = "Quy Nhơn"
            unaccented.contains("phan thiet") || unaccented.contains("mui ne") ->
                entities["destination"] = "Phan Thiết"
            unaccented.contains("ben tre") ->
                entities["destination"] = "Bến Tre"
        }

        // 3. Nhận diện câu hỏi đi chơi chung chung (chưa rõ điểm đến)
        if (!entities.containsKey("destination")) {
            if (unaccented.contains("di choi") || unaccented.contains("di dau choi") || 
                unaccented.contains("ranh di dau") || unaccented.contains("choi o dau") || 
                unaccented.contains("di dau vui") || unaccented.contains("tron nang") || 
                unaccented.contains("tron mua") || unaccented.contains("da ngoai")) {
                entities["trip_type"] = "generic_outing"
            }
        }

        // 4. Khung thời gian
        val lowerRaw = raw.lowercase()
        when {
            unaccented.contains("toi mai") || lowerRaw.contains("tối mai") -> entities["time_slot"] = "Tối mai"
            unaccented.contains("chieu mai") || lowerRaw.contains("chiều mai") -> entities["time_slot"] = "Chiều mai"
            unaccented.contains("sang mai") || lowerRaw.contains("sáng mai") -> entities["time_slot"] = "Sáng mai"
            unaccented.contains("trua mai") || lowerRaw.contains("trưa mai") -> entities["time_slot"] = "Trưa mai"
            unaccented.contains("toi nay") || lowerRaw.contains("tối nay") -> entities["time_slot"] = "Tối nay"
            unaccented.contains("chieu nay") || lowerRaw.contains("chiều nay") -> entities["time_slot"] = "Chiều nay"
            unaccented.contains("sang nay") || lowerRaw.contains("sáng nay") -> entities["time_slot"] = "Sáng nay"
            unaccented.contains("trua nay") || lowerRaw.contains("trưa nay") -> entities["time_slot"] = "Trưa nay"
            lowerRaw.contains("tối") || unaccented.contains("buoi toi") || unaccented.contains("chieu toi") || unaccented.contains("ve toi") -> entities["time_slot"] = "Buổi tối"
            lowerRaw.contains("sáng") || unaccented.contains("buoi sang") || unaccented.contains("sang som") -> entities["time_slot"] = "Buổi sáng"
            lowerRaw.contains("trưa") || unaccented.contains("buoi trua") || unaccented.contains("ban trua") -> entities["time_slot"] = "Buổi trưa"
            lowerRaw.contains("chiều") || unaccented.contains("buoi chieu") || unaccented.contains("xe chieu") -> entities["time_slot"] = "Buổi chiều"
            lowerRaw.contains("đêm") || unaccented.contains("ban dem") || unaccented.contains("khuya") -> entities["time_slot"] = "Ban đêm"
            unaccented.contains("ngay mai") || unaccented.contains("mai") -> entities["time_slot"] = "Ngày mai"
            unaccented.contains("cuoi tuan") || unaccented.contains("thu bay") || unaccented.contains("chu nhat") -> entities["time_slot"] = "Cuối tuần"
        }

        // 5. Mốc giờ cụ thể (Target Hour) & Khung giờ trước/sau
        when {
            unaccented.contains("8h") || unaccented.contains("8 gio") || unaccented.contains("20h") || 
            unaccented.contains("20:00") || unaccented.contains("tam gio") -> {
                entities["target_hour"] = "20:00 (8h tối)"
            }
            unaccented.contains("7h") || unaccented.contains("7 gio") || unaccented.contains("19h") -> {
                entities["target_hour"] = "19:00 (7h tối)"
            }
            unaccented.contains("9h") || unaccented.contains("9 gio") || unaccented.contains("21h") -> {
                entities["target_hour"] = "21:00 (9h tối)"
            }
        }

        if (unaccented.contains("truoc va sau") || unaccented.contains("truoc sau") || 
            unaccented.contains("xung quanh") || unaccented.contains("quanh") || 
            unaccented.contains("tam") || unaccented.contains("khoang")) {
            entities["hour_window"] = "before_and_after"
        }

        if (unaccented.contains("co mua khong") || unaccented.contains("co mua ko") || 
            unaccented.contains("mua khong") || unaccented.contains("mua ko") || 
            unaccented.contains("troi mua") || unaccented.contains("bi mua")) {
            entities["check_rain"] = "true"
        }

        // 6. Nhận diện kiểm tra đoạn đường / lộ trình di chuyển
        if (unaccented.contains("doan duong") || unaccented.contains("duong di") || 
            unaccented.contains("lo trinh") || unaccented.contains("tuyen duong") || 
            unaccented.contains("tren duong") || unaccented.contains("khoang cach") ||
            unaccented.contains("di tu") || unaccented.contains("tu dia diem") || unaccented.contains("check doan duong")) {
            entities["route_check"] = "true"
        }

        // 7. Nhận diện điểm xuất phát (Origin)
        when {
            unaccented.contains("tu cs1") || unaccented.contains("tu co so 1") -> entities["origin"] = "UTH Cơ sở 1 (Bình Thạnh)"
            unaccented.contains("tu cs2") || unaccented.contains("tu co so 2") -> entities["origin"] = "UTH Cơ sở 2 (Quận 12)"
            unaccented.contains("tu cs3") || unaccented.contains("tu co so 3") -> entities["origin"] = "UTH Cơ sở 3 (TP. Thủ Đức)"
            unaccented.contains("tu quan 1") || unaccented.contains("tu q1") -> entities["origin"] = "Quận 1"
            unaccented.contains("tu dia diem hien tai") || unaccented.contains("tu vi tri hien tai") || unaccented.contains("tu day") -> entities["origin"] = "Vị trí hiện tại (UTH Bình Thạnh, TP.HCM)"
            else -> entities["origin"] = "Vị trí hiện tại (UTH Cơ sở 1 - Bình Thạnh)"
        }

        // 8. Nhận diện yêu cầu trả lời ngắn gọn
        if (unaccented.contains("ngan gon") || unaccented.contains("ngan thoi") || unaccented.contains("ngan ti") || unaccented.contains("tom tat")) {
            entities["concise"] = "true"
        }

        // 9. Nhận diện kiểm tra khu vực lân cận để né/tránh
        if (unaccented.contains("lan can") || unaccented.contains("khu vuc lan can") || 
            unaccented.contains("tranh") || unaccented.contains("ne") || 
            unaccented.contains("duong tranh") || unaccented.contains("tranh ngap") || unaccented.contains("ne ngap")) {
            entities["avoid_adjacent"] = "true"
        }

        return entities

    }
}
