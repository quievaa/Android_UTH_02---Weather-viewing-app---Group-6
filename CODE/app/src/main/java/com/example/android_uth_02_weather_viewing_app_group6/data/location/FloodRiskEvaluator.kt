package com.example.android_uth_02_weather_viewing_app_group6.data.location

import com.example.android_uth_02_weather_viewing_app_group6.domain.model.CurrentWeather
import com.example.android_uth_02_weather_viewing_app_group6.ui.model.FloodAlertLevel
import com.example.android_uth_02_weather_viewing_app_group6.ui.model.FloodBlackspot
import com.example.android_uth_02_weather_viewing_app_group6.ui.model.UrbanFloodReport
import com.example.android_uth_02_weather_viewing_app_group6.ui.model.WeatherCondition
import java.util.Calendar

/**
 * Bộ đánh giá rủi ro ngập úng đô thị & thủy văn
 * Phục vụ đồ án chuyên ngành Giao thông vận tải - Đại học Giao thông Vận tải TP.HCM (UTH)
 */
object FloodRiskEvaluator {

    // Danh mục các điểm đen ngập úng thực tế tại TP.HCM
    val HCM_FLOOD_SPOTS = listOf(
        FloodBlackspot(
            id = "hcm_ung_van_khiem",
            streetName = "Đường Ung Văn Khiêm (đoạn từ D2 đến ngã năm)",
            district = "Phường 25, Quận Bình Thạnh",
            city = "TP. Hồ Chí Minh",
            latitude = 10.8038,
            longitude = 106.7175,
            expectedDepthCm = "30 - 50 cm",
            vehicleAdvice = "Xe máy và ô tô con gầm thấp tuyệt đối không đi qua, nguy cơ chết máy 90%",
            detourAdvice = "Đi vòng qua đường Điện Biên Phủ hoặc đường D1 (Nguyễn Gia Trí)",
            isNearUthCampus = true,
            uthCampusNote = "Cổng sau & đường vào Cơ sở 1 - ĐH Giao thông Vận tải TP.HCM (UTH)"
        ),
        FloodBlackspot(
            id = "hcm_nguyen_huu_canh",
            streetName = "Đường Nguyễn Hữu Cảnh (đoạn chân cầu vượt Nguyễn Hữu Cảnh)",
            district = "Quận Bình Thạnh",
            city = "TP. Hồ Chí Minh",
            latitude = 10.7925,
            longitude = 106.7128,
            expectedDepthCm = "25 - 45 cm",
            vehicleAdvice = "Xe máy đi sát dải phân cách giữa; ô tô giữ đều ga số thấp",
            detourAdvice = "Lưu thông qua đường Ngô Tất Tố hoặc cầu Thủ Thiêm",
            isNearUthCampus = true,
            uthCampusNote = "Tuyến giao thông kết nối UTH Cơ sở 1 với trung tâm Quận 1"
        ),
        FloodBlackspot(
            id = "hcm_ql13_binh_trieu",
            streetName = "Quốc lộ 13 (đoạn từ cầu Bình Triệu đến ngã tư Bình Phước)",
            district = "TP. Thủ Đức",
            city = "TP. Hồ Chí Minh",
            latitude = 10.8352,
            longitude = 106.7130,
            expectedDepthCm = "35 - 55 cm",
            vehicleAdvice = "Ngập bánh xe, ùn tắc kéo dài; xe máy dễ ngã do sóng nước từ xe tải",
            detourAdvice = "Đi qua đường Phạm Văn Đồng rồi rẽ vào Quốc lộ 1A",
            isNearUthCampus = true,
            uthCampusNote = "Tuyến đường huyết mạch nối giữa UTH Cơ sở 1 và UTH Cơ sở 2"
        ),
        FloodBlackspot(
            id = "hcm_vo_van_ngan",
            streetName = "Đường Võ Văn Ngân (đoạn dốc trước chợ Thủ Đức)",
            district = "TP. Thủ Đức",
            city = "TP. Hồ Chí Minh",
            latitude = 10.8504,
            longitude = 106.7583,
            expectedDepthCm = "40 - 60 cm (Nước chảy xiết)",
            vehicleAdvice = "Cực kỳ nguy hiểm! Dòng nước dốc chảy xiết cuốn trôi xe máy",
            detourAdvice = "Tránh xa khu vực lòng chảo Chợ Thủ Đức khi đang mưa dông",
            isNearUthCampus = false
        ),
        FloodBlackspot(
            id = "hcm_do_xuan_hop",
            streetName = "Đường Đỗ Xuân Hợp & Lê Văn Việt",
            district = "TP. Thủ Đức",
            city = "TP. Hồ Chí Minh",
            latitude = 10.8120,
            longitude = 106.7780,
            expectedDepthCm = "25 - 40 cm",
            vehicleAdvice = "Phương tiện đi chậm, không tăng ga đột ngột tránh nước tràn cổ hút gió",
            detourAdvice = "Chuyển hướng sang đường Song Hành Xa Lộ Hà Nội",
            isNearUthCampus = false
        ),
        FloodBlackspot(
            id = "hcm_huynh_tan_phat",
            streetName = "Đường Huỳnh Tấn Phát & Trần Xuân Soạn",
            district = "Quận 7",
            city = "TP. Hồ Chí Minh",
            latitude = 10.7420,
            longitude = 106.7350,
            expectedDepthCm = "30 - 50 cm (Ngập triều cường)",
            vehicleAdvice = "Nước mặn xâm nhập ăn mòn gầm xe, cần rửa xe ngay sau khi qua điểm ngập",
            detourAdvice = "Đi tuyến đường Nguyễn Thị Thập hoặc đường Nguyễn Văn Linh",
            isNearUthCampus = false
        )
    )

    // Danh mục điểm đen ngập úng tại Hà Nội
    val HN_FLOOD_SPOTS = listOf(
        FloodBlackspot(
            id = "hn_phung_hung",
            streetName = "Phố Phùng Hưng & Bát Đàn",
            district = "Quận Hoàn Kiếm",
            city = "Hà Nội",
            latitude = 21.0345,
            longitude = 105.8450,
            expectedDepthCm = "25 - 40 cm",
            vehicleAdvice = "Ô tô con và xe máy dễ sặc nước, chú ý nắp hố ga mở",
            detourAdvice = "Đi đường Phan Đình Phùng hoặc Trần Phú",
            isNearUthCampus = false
        ),
        FloodBlackspot(
            id = "hn_nguyen_trai",
            streetName = "Đường Nguyễn Trãi (đoạn chân hầm chui Thanh Xuân)",
            district = "Quận Thanh Xuân",
            city = "Hà Nội",
            latitude = 20.9980,
            longitude = 105.8050,
            expectedDepthCm = "30 - 50 cm",
            vehicleAdvice = "Ùn tắc nghiêm trọng; xe máy nên đi lên cầu vượt trên cao",
            detourAdvice = "Sử dụng đường Vành đai 3 trên cao hoặc tuyến Lê Văn Lương",
            isNearUthCampus = false
        ),
        FloodBlackspot(
            id = "hn_ham_chui_thang_long",
            streetName = "Đường gom Đại lộ Thăng Long (Hầm chui số 3, 5, 6)",
            district = "Quận Nam Từ Liêm",
            city = "Hà Nội",
            latitude = 21.0020,
            longitude = 105.7480,
            expectedDepthCm = "40 - 70 cm",
            vehicleAdvice = "Cấm phương tiện qua lại, ngập lút bánh xe tải",
            detourAdvice = "Lưu thông thẳng trên đường chính Đại lộ Thăng Long",
            isNearUthCampus = false
        )
    )

    /**
     * Đánh giá rủi ro ngập úng dựa trên thời tiết hiện tại và đặc thù địa lý
     */
    fun evaluate(weather: CurrentWeather?, cityName: String): UrbanFloodReport {
        val normalizedCity = cityName.lowercase()
        val isHcm = normalizedCity.contains("hồ chí minh") || normalizedCity.contains("ho chi minh") ||
                normalizedCity.contains("hcm") || normalizedCity.contains("sài gòn") || normalizedCity.contains("sai gon")
        val isHn = normalizedCity.contains("hà nội") || normalizedCity.contains("ha noi") || normalizedCity.contains("hn")

        val cityTitle = if (isHcm) "TP. Hồ Chí Minh" else if (isHn) "Hà Nội" else cityName

        // Kiểm tra khung giờ triều cường thường gặp ở TP.HCM (16:00 - 19:30 và 04:00 - 07:00)
        val hour = Calendar.getInstance().get(Calendar.HOUR_OF_DAY)
        val isTideHours = isHcm && ((hour in 16..19) || (hour in 4..7))

        val desc = weather?.description?.lowercase() ?: ""
        val weatherMain = weather?.weatherMain?.lowercase() ?: ""

        val isThunder = desc.contains("dông") || desc.contains("bão") || desc.contains("thunder") || weatherMain.contains("thunder")
        val isHeavyRain = desc.contains("mưa lớn") || desc.contains("mưa to") || desc.contains("heavy")
        val isModerateRain = desc.contains("mưa rào") || desc.contains("mưa") || weatherMain.contains("rain")
        val isLightRain = desc.contains("mưa nhỏ") || desc.contains("mưa phùn") || desc.contains("drizzle")

        val spots = if (isHcm) HCM_FLOOD_SPOTS else if (isHn) HN_FLOOD_SPOTS else emptyList()

        return when {
            // Mức 1: NGUY HIỂM ĐỎ (Mưa dông / Mưa lớn diện rộng)
            isThunder || isHeavyRain -> {
                val affected = if (spots.isNotEmpty()) spots else createGenericSpots(cityTitle, true)
                UrbanFloodReport(
                    hasAlert = true,
                    level = FloodAlertLevel.SEVERE,
                    title = "BÁO ĐỘNG NGẬP ĐƯỜNG CẤP ĐỘ ĐỎ",
                    summary = "Mưa dông cực lớn kết hợp lượng nước dồn dập. ${affected.size} tuyến đường trọng điểm có nguy cơ ngập sâu 40-60cm, dòng nước xiết.",
                    timeWindow = "Cao điểm trong 1 - 2 giờ tới",
                    cityName = cityTitle,
                    rainfallEstimateMm = if (isThunder) 45.0 else 35.0,
                    isTideAffected = isTideHours,
                    highRiskStreets = affected,
                    safetyTips = listOf(
                        "Tuyệt đối không cố vượt qua các đoạn nước ngập quá nửa bánh xe máy.",
                        "Tránh xa các miệng cống, nắp hố ga và khu vực trũng thấp có dòng nước xoáy.",
                        "Nếu xe bị chết máy trong vùng ngập, không được đề nổ lại để tránh thủy kích vỡ lốc máy.",
                        "Sinh viên UTH chú ý: Đường Ung Văn Khiêm đoạn cổng sau trường ngập rất sâu, khuyến nghị chờ tại sảnh trường."
                    )
                )
            }

            // Mức 2: CẢNH BÁO CAM (Mưa rào vừa hoặc Mưa kết hợp triều cường)
            isModerateRain || (isLightRain && isTideHours) -> {
                val affected = if (spots.isNotEmpty()) spots.take(4) else createGenericSpots(cityTitle, false)
                val tideNote = if (isTideHours) " kết hợp triều cường đang dâng cao" else ""
                UrbanFloodReport(
                    hasAlert = true,
                    level = FloodAlertLevel.WARNING,
                    title = "CẢNH BÁO NGUY CƠ NGẬP ÚNG CỤC BỘ",
                    summary = "Mưa rào diện rộng$tideNote. Xuất hiện điểm nghẽn thoát nước và ngập 20-35cm tại các tuyến đường trũng thấp.",
                    timeWindow = "Dự kiến kéo dài 45 - 90 phút",
                    cityName = cityTitle,
                    rainfallEstimateMm = 20.0,
                    isTideAffected = isTideHours,
                    highRiskStreets = affected,
                    safetyTips = listOf(
                        "Giữ đều tay ga ở số thấp (số 1 hoặc 2 với xe số), tránh đi vào mép đường trũng.",
                        "Bật đèn chiếu gần và giữ khoảng cách an toàn tối thiểu 20m với xe đi trước.",
                        "Theo dõi các tuyến đường thay thế để tránh ùn ứ kéo dài vào giờ tan tầm."
                    )
                )
            }

            // Mức 3: THEO DÕI VÀNG (Mưa nhỏ hoặc Triều cường không mưa)
            isLightRain || isTideHours -> {
                val cause = if (isTideHours) "Đang trong khung giờ đỉnh triều cường ven sông" else "Mưa phùn rải rác"
                UrbanFloodReport(
                    hasAlert = true,
                    level = FloodAlertLevel.CAUTION,
                    title = "THEO DÕI THỦY VĂN & MẶT ĐƯỜNG",
                    summary = "$cause. Mặt đường trơn trượt, nước ứ đọng mép vỉa hè tại một số khu vực trũng.",
                    timeWindow = "Tình hình ổn định, lưu ý trơn trượt",
                    cityName = cityTitle,
                    rainfallEstimateMm = 5.0,
                    isTideAffected = isTideHours,
                    highRiskStreets = spots.filter { it.isNearUthCampus },
                    safetyTips = listOf(
                        "Giảm tốc độ khi ôm cua hoặc qua các vạch sơn phân làn trơn ướt.",
                        "Kiểm tra hệ thống phanh trước khi khởi hành."
                    )
                )
            }

            // Mức 4: AN TOÀN XANH (Khô ráo)
            else -> {
                UrbanFloodReport(
                    hasAlert = false,
                    level = FloodAlertLevel.SAFE,
                    title = "GIAO THÔNG KHÔ RÁO & AN TOÀN",
                    summary = "Thời tiết thuận lợi, không có cảnh báo ngập úng hay triều cường tại $cityTitle.",
                    timeWindow = "Toàn bộ các tuyến đường thông suốt",
                    cityName = cityTitle,
                    rainfallEstimateMm = 0.0,
                    isTideAffected = false,
                    highRiskStreets = emptyList(),
                    safetyTips = listOf(
                        "Đường khô ráo, phương tiện lưu thông bình thường với tốc độ quy định."
                    )
                )
            }
        }
    }

    private fun createGenericSpots(cityName: String, isSevere: Boolean): List<FloodBlackspot> {
        val depth = if (isSevere) "35 - 50 cm" else "20 - 30 cm"
        return listOf(
            FloodBlackspot(
                id = "gen_spot_1",
                streetName = "Các tuyến đường trục chính trũng thấp trung tâm",
                district = "Nội thành",
                city = cityName,
                latitude = 0.0,
                longitude = 0.0,
                expectedDepthCm = depth,
                vehicleAdvice = "Giảm tốc độ, chú ý các đoạn trũng và nắp cống thoát nước",
                detourAdvice = "Lưu thông trên các tuyến đường cao ráo hơn"
            )
        )
    }
}
