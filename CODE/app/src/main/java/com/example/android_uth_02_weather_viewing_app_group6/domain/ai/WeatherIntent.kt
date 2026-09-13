package com.example.android_uth_02_weather_viewing_app_group6.domain.ai

/**
 * Danh mục nhóm ý định (Intent Categories) của Trợ lý Thời tiết AI.
 */
enum class IntentCategory(val titleVi: String, val icon: String) {
    GENERAL("⚡ Phổ biến", "⚡"),
    UTH_STUDENT("🏫 Sinh viên UTH", "🏫"),
    LIFE_HEALTH("🏃 Đời sống & Sức khỏe", "🏃"),
    TRAVEL_SCIENCE("🏖️ Du lịch & Khoa học", "🏖️")
}

// Nhóm Ý Định Thời Tiết (Weather Intents) được AI huấn luyện sâu.
enum class WeatherIntent(
    val idName: String,
    val displayName: String,
    val category: IntentCategory,
    val sampleQuestion: String,
    val descriptionVi: String
) {
    GREETING(
        "greeting",
        "Chào hỏi & Làm quen",
        IntentCategory.GENERAL,
        "Xin chào bot, bạn tên là gì?",
        "Chào hỏi, giới thiệu bản thân và chức năng của Trợ lý UTH AI"
    ),
    GENERAL_OVERVIEW(
        "general_overview",
        "Tổng quan thời tiết",
        IntentCategory.GENERAL,
        "Hôm nay thời tiết thế nào?",
        "Cung cấp thông tin tổng thể: nhiệt độ, độ ẩm, tình trạng mây/nắng, gió"
    ),
    TEMPERATURE_FEEL(
        "temperature_feel",
        "Nhiệt độ & Cảm giác",
        IntentCategory.GENERAL,
        "Hôm nay bao nhiêu độ, có oi bức không?",
        "Phân tích nhiệt độ đo được vs nhiệt độ cảm nhận thực tế (feels-like)"
    ),
    RAIN_AND_UMBRELLA(
        "rain_and_umbrella",
        "Mưa & Áo mưa, Ô dù",
        IntentCategory.GENERAL,
        "Chiều nay có mưa không, cần mang áo mưa không?",
        "Dự báo xác suất mưa theo giờ, khuyên mang áo mưa/ô che chắn"
    ),
    RAIN_STOP_TIME(
        "rain_stop_time",
        "Khi nào tạnh mưa",
        IntentCategory.GENERAL,
        "Mưa rào khi nào dứt, mấy giờ tạnh mưa?",
        "Dự đoán thời điểm mưa kết thúc dựa trên biểu đồ Hourly Forecast"
    ),
    UTH_FLOOD_AND_TRAFFIC(
        "uth_flood_and_traffic",
        "Ngập úng & Giao thông UTH",
        IntentCategory.UTH_STUDENT,
        "Đường Ung Văn Khiêm CS1 có ngập không bot?",
        "Cảnh báo ngập lụt chi tiết quanh Cơ sở 1, Cơ sở 2, Cơ sở 3 trường UTH"
    ),
    STUDENT_COMMUTE_ADVICE(
        "student_commute_advice",
        "Đi học UTH ca sáng/chiều",
        IntentCategory.UTH_STUDENT,
        "Sinh viên UTH đi học ca chiều cần chuẩn bị gì?",
        "Tư vấn trang bị cho sinh viên UTH (balo chống ướt, laptop, áo mưa)"
    ),
    OUTFIT_ADVICE(
        "outfit_advice",
        "Tư vấn Trang phục",
        IntentCategory.LIFE_HEALTH,
        "Hôm nay nên mặc đồ gì đi chơi?",
        "Gợi ý trang phục theo nhiệt độ, độ ẩm, độ bức xạ UV và gió"
    ),
    LAUNDRY_DRYING(
        "laundry_drying",
        "Phơi đồ & Giặt giũ",
        IntentCategory.LIFE_HEALTH,
        "Hôm nay giặt đồ phơi có khô không?",
        "Đánh giá điều kiện phơi quần áo dựa trên độ ẩm, nắng và khả năng mưa"
    ),
    OUTDOOR_ACTIVITIES(
        "outdoor_activities",
        "Thể thao & Chạy bộ",
        IntentCategory.LIFE_HEALTH,
        "Chiều nay đi chạy bộ hoặc đá banh được không?",
        "Đánh giá thời tiết cho chạy bộ, đá bóng, đạp xe, hoạt động ngoài trời"
    ),
    HEALTH_AND_WELLNESS(
        "health_and_wellness",
        "Sức khỏe & Phòng bệnh",
        IntentCategory.LIFE_HEALTH,
        "Thời tiết này có dễ bị cảm cúm, đau đầu không?",
        "Cảnh báo dị ứng thời tiết, viêm xoang, cảm cúm, phòng muỗi sốt xuất huyết"
    ),
    AIR_QUALITY_AQI(
        "air_quality_aqi",
        "Chất lượng Không khí & Bụi mịn",
        IntentCategory.LIFE_HEALTH,
        "Không khí hôm nay có ô nhiễm không, có cần đeo khẩu trang?",
        "Chỉ số AQI, bụi PM2.5 và lời khuyên bảo vệ đường hô hấp"
    ),
    UV_INDEX_SUN(
        "uv_index_sun",
        "Tia cực tím UV & Chống nắng",
        IntentCategory.LIFE_HEALTH,
        "Chỉ số UV hôm nay có gắt không, cần thoa kem chống nắng không?",
        "Cảnh báo tia cực tím UV và khung giờ bức xạ cao cần che chắn"
    ),
    HUMIDITY_AND_MOLD(
        "humidity_and_mold",
        "Độ ẩm & Nồm ẩm",
        IntentCategory.LIFE_HEALTH,
        "Độ ẩm 80% có cao không, có bị ẩm mốc đồ đạc không?",
        "Giải thích độ ẩm không khí, bảo quản đồ điện tử, máy ảnh"
    ),
    WIND_AND_STORMS(
        "wind_and_storms",
        "Gió bão & Giông lốc",
        IntentCategory.GENERAL,
        "Hôm nay gió giật cấp mấy, có tin bão gì không?",
        "Thông tin tốc độ gió, cảnh báo giông lốc cây đổ và áp thấp nhiệt đới"
    ),
    SUNRISE_SUNSET(
        "sunrise_sunset",
        "Bình minh & Hoàng hôn",
        IntentCategory.LIFE_HEALTH,
        "Mặt trời lặn lúc mấy giờ để đi ngắm hoàng hôn?",
        "Thời gian bình minh, hoàng hôn và khung giờ vàng (golden hour) chụp ảnh"
    ),
    HOURLY_FORECAST(
        "hourly_forecast",
        "Diễn biến theo giờ",
        IntentCategory.GENERAL,
        "Thời tiết chiều nay và tối nay thế nào?",
        "Chi tiết dự báo các khung giờ cụ thể trong ngày (sáng/trưa/chiều/tối/đêm)"
    ),
    FUTURE_FORECAST(
        "future_forecast",
        "Ngày mai & Cuối tuần",
        IntentCategory.GENERAL,
        "Dự báo thời tiết ngày mai và cuối tuần này?",
        "Dự báo các ngày tới, thứ 7, Chủ nhật để lên kế hoạch dã ngoại"
    ),
    CAR_WASH(
        "car_wash",
        "Rửa xe máy / Ô tô",
        IntentCategory.LIFE_HEALTH,
        "Hôm nay có nên rửa xe máy không bot?",
        "Tư vấn có nên rửa xe không dựa vào xác suất mưa hôm nay và ngày mai"
    ),
    PET_CARE(
        "pet_care",
        "Chăm sóc Thú cưng",
        IntentCategory.LIFE_HEALTH,
        "Trời này dắt chó đi dạo có bị bỏng chân không?",
        "Lưu ý nhiệt độ mặt đường nhựa với đệm chân thú cưng và sốc nhiệt"
    ),
    TRAVEL_TOURISM(
        "travel_tourism",
        "Du lịch các Tỉnh thành",
        IntentCategory.TRAVEL_SCIENCE,
        "Thời tiết Đà Lạt, Vũng Tàu cuối tuần này ra sao?",
        "Thời tiết các điểm du lịch nổi tiếng: Đà Lạt, Vũng Tàu, Phú Quốc, Sa Pa, Hà Nội..."
    ),
    METEOROLOGY_SCIENCE(
        "meteorology_science",
        "Khoa học Khí tượng",
        IntentCategory.TRAVEL_SCIENCE,
        "Tại sao trời lại có mây dông và mưa rào?",
        "Giải thích dễ hiểu các hiện tượng khí tượng tự nhiên"
    ),
    FOLK_PROVERBS(
        "folk_proverbs",
        "Tục ngữ Thời tiết Dân gian",
        IntentCategory.TRAVEL_SCIENCE,
        "Đố bot một câu ca dao tục ngữ dự báo thời tiết?",
        "Kho tàng ca dao, tục ngữ thời tiết truyền thống của Việt Nam"
    ),
    BOT_CAPABILITIES_AND_ORIGIN(
        "bot_capabilities_and_origin",
        "Thông tin & Khả năng của Bot",
        IntentCategory.GENERAL,
        "Bạn được huấn luyện thế nào, bạn làm được những gì?",
        "Giới thiệu kiến trúc AI, đồ án Nhóm 6 UTH và tập dữ liệu huấn luyện"
    ),
    GOODBYE_AND_THANKS(
        "goodbye_and_thanks",
        "Cảm ơn & Tạm biệt",
        IntentCategory.GENERAL,
        "Cảm ơn bot nhiều nhé, tạm biệt!",
        "Phản hồi lịch sự, thân thiện khi người dùng cảm ơn hoặc chào tạm biệt"
    ),
    CUSTOM_KNOWLEDGE(
        "custom_knowledge",
        "Tri thức người dùng tự dạy",
        IntentCategory.GENERAL,
        "",
        "Kiến thức đặc thù do người dùng trực tiếp nạp vào cho AI"
    ),
    UNKNOWN(
        "unknown",
        "Không xác định",
        IntentCategory.GENERAL,
        "",
        "Không nhận diện được ý định rõ ràng, kích hoạt tổng hợp thời tiết thông minh"
    )
}
