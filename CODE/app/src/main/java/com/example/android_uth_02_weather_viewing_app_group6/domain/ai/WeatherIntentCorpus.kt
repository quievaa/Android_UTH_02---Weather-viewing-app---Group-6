package com.example.android_uth_02_weather_viewing_app_group6.domain.ai
object WeatherIntentCorpus {

    val trainingData: Map<WeatherIntent, List<String>> = mapOf(
        WeatherIntent.GREETING to listOf(
            "xin chào", "chào bạn", "hello", "hi bot", "chào trợ lý", "alo",
            "bạn tên là gì", "bot tên gì", "bạn là ai", "ai đây", "giới thiệu bản thân đi",
            "chào buổi sáng", "good morning", "chúc ngày mới tốt lành", "hé lô"
        ),

        WeatherIntent.GENERAL_OVERVIEW to listOf(
            "thời tiết hôm nay thế nào", "hôm nay trời sao", "tình hình thời tiết",
            "thời tiết hiện tại", "báo thời tiết đi bot", "trời hôm nay đẹp không",
            "thời tiết ngoài trời thế nào", "có nắng hay mây", "thời tiết sài gòn hôm nay",
            "cho tôi biết thời tiết hiện tại", "thời tiết tp hcm", "trời hôm nay ổn không"
        ),

        WeatherIntent.TEMPERATURE_FEEL to listOf(
            "hôm nay bao nhiêu độ", "nhiệt độ hiện tại", "nhiệt độ sài gòn",
            "trời có nóng không", "trời có lạnh không", "oi bức quá", "nắng nóng kinh khủng",
            "nhiệt độ cảm giác thực tế", "feels like bao nhiêu", "ngoài trời nóng cỡ nào",
            "hôm nay mát mẻ không", "bao nhiêu độ c", "nhiệt kế chỉ bao nhiêu"
        ),

        WeatherIntent.RAIN_AND_UMBRELLA to listOf(
            "hôm nay có mưa không", "trời có mưa ko", "hôm nay mưa hông", "chiều nay có mưa không",
            "có cần mang áo mưa không", "mang áo mưa ko", "có nên đem ô không", "mang dù ko",
            "trời sắp mưa chưa", "sắp đổ mưa hả", "xác suất mưa bao nhiêu phần trăm",
            "có mưa dông không", "mưa to hay nhỏ", "mưa rào không", "trời đang mưa hả",
            "ra đường có bị ướt không", "đem ô theo không", "chuẩn bị áo mưa không"
        ),

        WeatherIntent.RAIN_STOP_TIME to listOf(
            "khi nào hết mưa", "mấy giờ tạnh mưa", "bao giờ dứt mưa", "mưa bao lâu nữa thì hết",
            "chừng nào ngưng mưa", "mưa tới mấy giờ", "khi nào trời ráo", "sắp tạnh mưa chưa",
            "mưa rào kéo dài bao lâu", "khi nào hết giông"
        ),

        WeatherIntent.UTH_FLOOD_AND_TRAFFIC to listOf(
            "đường uth có ngập không", "cơ sở 1 có ngập không", "ung văn khiêm có ngập không",
            "nguyễn gia trí có đọng nước không", "cơ sở 2 quận 12 có ngập lụt không",
            "tô ký có ngập không", "cơ sở 3 thủ đức có ngập không", "đỗ xuân hợp có ngập không",
            "đường đi học có ngập nước không", "triều cường hôm nay thế nào", "đường nguyễn hữu cảnh ngập không",
            "tuyến đường nào đang ngập", "quốc lộ 13 ngập không", "đường xá quanh trường giao thông vận tải",
            "ngập úng cs1", "ngập lụt cs2", "ngập cs3", "đi xe máy có chết máy không"
        ),

        WeatherIntent.STUDENT_COMMUTE_ADVICE to listOf(
            "sinh viên uth đi học cần chuẩn bị gì", "đi học ca sáng cần mang gì",
            "đi học ca chiều cần đem gì", "sinh viên đi học trời mưa", "bảo vệ laptop trời mưa thế nào",
            "lời khuyên cho sinh viên giao thông vận tải", "đi học xe buýt hay xe máy",
            "đồ dùng đi học mùa mưa", "sinh viên uth lưu ý gì hôm nay"
        ),

        WeatherIntent.OUTFIT_ADVICE to listOf(
            "hôm nay nên mặc gì", "mặc đồ gì đi làm", "mặc gì đi học", "outfit hôm nay",
            "trang phục phù hợp", "có nên mặc áo khoác không", "có cần mặc áo lạnh không",
            "trời này bận đồ gì", "mặc váy được không", "nên đi giày hay dép",
            "mặc quần ngắn hay dài", "thời tiết này mặc đồ gì cho đẹp và mát"
        ),

        WeatherIntent.LAUNDRY_DRYING to listOf(
            "hôm nay phơi đồ được không", "phơi quần áo có khô không", "giặt đồ hôm nay có ổn không",
            "trời này phơi đồ được hông", "quần áo phơi ngoài trời có bị ẩm mốc không",
            "có nên giặt chăn mền hôm nay không", "phơi đồ bao lâu thì khô", "đem đồ vô chưa trời sắp mưa kìa",
            "thời tiết này giặt đồ được ko"
        ),

        WeatherIntent.OUTDOOR_ACTIVITIES to listOf(
            "chiều nay có chạy bộ được không", "đi đá banh được không", "tập thể dục ngoài trời",
            "đi dạo công viên được không", "có nên đi bơi không", "thể thao ngoài trời hôm nay",
            "đạp xe ngoài trời ổn không", "sáng mai chạy bộ mấy giờ đẹp", "thời tiết tập gym ngoài trời",
            "chơi cầu lông ngoài trời được không"
        ),

        WeatherIntent.HEALTH_AND_WELLNESS to listOf(
            "thời tiết này có dễ bị bệnh không", "trời thay đổi dễ cảm cúm",
            "đau đầu do thời tiết", "viêm xoang có bị nặng không", "thời tiết này muỗi có nhiều không",
            "phòng ngừa sốt xuất huyết", "hen suyễn thời tiết này", "dị ứng thời tiết",
            "trời oi bức dễ say nắng", "uống bao nhiêu nước hôm nay", "lời khuyên sức khỏe thời tiết"
        ),

        WeatherIntent.AIR_QUALITY_AQI to listOf(
            "chất lượng không khí hôm nay thế nào", "chỉ số aqi bao nhiêu", "không khí có ô nhiễm không",
            "có bụi mịn pm2.5 không", "ra đường có cần đeo khẩu trang không", "không khí sài gòn hôm nay",
            "aqi cao hay thấp", "bầu không khí trong lành không", "mức độ ô nhiễm không khí"
        ),

        WeatherIntent.UV_INDEX_SUN to listOf(
            "chỉ số uv hôm nay thế nào", "nắng có gắt không", "tia cực tím hôm nay cấp mấy",
            "có cần bôi kem chống nắng không", "tia uv nguy hiểm không", "mấy giờ nắng gắt nhất",
            "chỉ số bức xạ mặt trời", "đeo kính râm chống uv không", "cháy nắng không"
        ),

        WeatherIntent.HUMIDITY_AND_MOLD to listOf(
            "độ ẩm hôm nay bao nhiêu", "độ ẩm không khí cao không", "độ ẩm 80% có cao không",
            "trời có bị nồm không", "ẩm ướt khó chịu quá", "đồ đạc có bị ẩm mốc không",
            "bảo quản máy ảnh trời ẩm thế nào", "độ ẩm tương đối là gì"
        ),

        WeatherIntent.WIND_AND_STORMS to listOf(
            "gió hôm nay thổi mạnh không", "tốc độ gió bao nhiêu", "có giông lốc cây đổ không",
            "biển đông có bão không", "tin bão mới nhất", "áp thấp nhiệt đới gần bờ không",
            "gió cấp mấy", "sài gòn có ảnh hưởng bão không", "cơn dông lốc"
        ),

        WeatherIntent.SUNRISE_SUNSET to listOf(
            "mặt trời mọc lúc mấy giờ", "bình minh mấy giờ", "mặt trời lặn lúc mấy giờ",
            "hoàng hôn hôm nay lúc mấy giờ", "mấy giờ ngắm hoàng hôn đẹp nhất",
            "giờ vàng chụp ảnh hôm nay", "golden hour lúc mấy giờ"
        ),

        WeatherIntent.HOURLY_FORECAST to listOf(
            "thời tiết chiều nay thế nào", "trưa nay có nắng gắt không", "tối nay trời ra sao",
            "đêm nay nhiệt độ bao nhiêu", "sáng mai trời thế nào", "thời tiết các giờ trong ngày",
            "diễn biến thời tiết hôm nay", "chiều nay mấy giờ mưa"
        ),

        WeatherIntent.FUTURE_FORECAST to listOf(
            "thời tiết ngày mai ra sao", "mai trời có mưa không", "cuối tuần này thời tiết thế nào",
            "thứ bảy chủ nhật trời đẹp không", "dự báo mấy ngày tới", "tuần này có mưa nhiều không",
            "kế hoạch đi chơi cuối tuần"
        ),

        WeatherIntent.CAR_WASH to listOf(
            "hôm nay có nên rửa xe không", "rửa xe máy hôm nay được không", "rửa xe xong trời có mưa không",
            "trời này đi rửa xe ô tô được chưa", "rửa xe hôm nay có phí công không"
        ),

        WeatherIntent.PET_CARE to listOf(
            "dắt chó đi dạo trời này được không", "mặt đường có nóng bỏng chân cún không",
            "chăm sóc thú cưng mùa nắng nóng", "mèo chó có bị sốc nhiệt không",
            "dắt thú cưng đi dạo lúc mấy giờ"
        ),

        WeatherIntent.TRAVEL_TOURISM to listOf(
            "thời tiết đà lạt thế nào", "đi vũng tàu tắm biển được không", "thời tiết hà nội",
            "thời tiết đà nẵng", "thời tiết phú quốc", "thời tiết nha trang", "thời tiết sa pa",
            "du lịch cần thơ trời sao", "thời tiết quy nhơn", "thời tiết phan thiết mũi né",
            "mai tôi đi chơi", "mai tôi đi vũng tàu chơi", "mai đi đà lạt chơi", "mai đi landmark 81 chơi",
            "mai rảnh nên đi đâu chơi", "mai nên đi đâu chơi", "cuối tuần đi đâu chơi", "mai đi chơi ở đâu",
            "mai đi phố đi bộ được không", "mai đi tây ninh chơi", "mai đi cần giờ chơi", "mai đi hồ trị an cắm trại",
            "tối mai tôi đi quận 7", "tối mai đi quận 7 chơi có mưa không", "tối mai đi q7 chơi",
            "tối mai tôi đi quận 7 bạn tra tầm trước và sau 8h có mưa không", "tối mai đi quận 7 trước và sau 8h có mưa không",
            "tối mai đi quận 7 tầm 8h có mưa không", "quận 7 tối mai trước và sau 8h có mưa không", "quận 7 tối mai 8h có mưa ko",
            "tối mai tôi đi quận 7 kiểm tra đoạn đường đi có mưa không", "tối mai tôi đi quận 7 check đoạn đường đi sau đó kiểm tra trên đoạn đường đi có mưa không",
            "tôi đi quận 7 kiểm tra trên đoạn đường đi có mưa không", "đi quận 7 đoạn đường đi có mưa không",
            "kiểm tra đường đi từ địa điểm hiện tại tới quận 7 có mưa không", "từ đây tới quận 7 đường đi có mưa không",
            "mai tôi đi đà lạt đường đi có mưa không", "tôi đi vũng tàu kiểm tra đoạn đường đi có mưa không",
            "tối đi cầu ánh sao quận 7", "tối mai đi hồ bán nguyệt q7", "chiều mai đi phú mỹ hưng chơi",
            "tối đi chơi ở đâu sài gòn", "tối mai rảnh đi đâu chơi", "chỗ nào đi chơi trốn nắng",
            "mai mưa thì đi đâu chơi", "lịch trình đi chơi ngày mai", "gợi ý điểm đi chơi cuối tuần",
            "mai đi dã ngoại ở đâu", "mai rảnh rỗi đi đâu chơi vui", "thời tiết đi vũng tàu ngày mai",
            "thời tiết đi đà lạt ngày mai", "mai đi bến bạch đằng ngắm hoàng hôn", "lộ trình đi có mưa không"
        ),

        WeatherIntent.METEOROLOGY_SCIENCE to listOf(
            "tại sao trời lại mưa rào", "mây dông hình thành thế nào", "sấm sét sinh ra từ đâu",
            "tại sao trời lại oi bức trước khi mưa", "áp suất khí quyển là gì", "hiệu ứng nhà kính là gì"
        ),

        WeatherIntent.FOLK_PROVERBS to listOf(
            "ca dao tục ngữ về thời tiết", "tục ngữ dự báo thời tiết", "chuồn chuồn bay thấp thì mưa",
            "cơn mưa đằng đông vừa trông vừa chạy", "đố câu ca dao thời tiết", "dân gian dự báo thời tiết thế nào"
        ),

        WeatherIntent.BOT_CAPABILITIES_AND_ORIGIN to listOf(
            "bạn làm được những gì", "bạn được huấn luyện thế nào", "ai làm ra bạn",
            "nhóm 6 uth là ai", "trợ lý ảo này thông minh cỡ nào", "hướng dẫn sử dụng bot",
            "bạn biết những kiến thức gì"
        ),

        WeatherIntent.GOODBYE_AND_THANKS to listOf(
            "cảm ơn bot", "thanks bạn nhé", "tuyệt vời quá", "tạm biệt bot", "bye bye",
            "chúc ngủ ngon", "good night", "hẹn gặp lại", "bot dễ thương quá", "ok cảm ơn"
        )
    )

    /**
     * Danh sách các từ khóa trọng số cao (Boost Keywords) giúp tăng tốc độ nhận diện chính xác.
     */
    val highSignalKeywords: Map<WeatherIntent, List<String>> = mapOf(
        WeatherIntent.UTH_FLOOD_AND_TRAFFIC to listOf("ngap", "trieu cuong", "ung van khiem", "nguyen gia tri", "cs1", "cs2", "cs3", "to ky", "do xuan hop", "ket xe"),
        WeatherIntent.STUDENT_COMMUTE_ADVICE to listOf("sinh vien", "ca hoc", "ca sang", "ca chieu", "laptop", "balo", "di hoc"),
        WeatherIntent.RAIN_STOP_TIME to listOf("tanh mua", "het mua", "dut mua", "ngung mua", "may gio tanh"),
        WeatherIntent.RAIN_AND_UMBRELLA to listOf("mua", "ao mua", "cay du", "cay o", "mang o", "mang du", "do mua"),
        WeatherIntent.LAUNDRY_DRYING to listOf("phoi do", "phoi quan ao", "giat do", "giat quan ao", "kho quan ao"),
        WeatherIntent.CAR_WASH to listOf("rua xe", "rua xe may", "rua o to"),
        WeatherIntent.PET_CARE to listOf("dat cho", "thu cung", "cho meo", "bong chan", "dem thit"),
        WeatherIntent.AIR_QUALITY_AQI to listOf("aqi", "bui min", "pm2 5", "o nhiem", "khong khi", "khau trang"),
        WeatherIntent.UV_INDEX_SUN to listOf("uv", "tia cuc tim", "kem chong nang", "chong nang", "chay nang"),
        WeatherIntent.HUMIDITY_AND_MOLD to listOf("do am", "nom", "am moc", "may anh", "am uot"),
        WeatherIntent.WIND_AND_STORMS to listOf("gio", "loc", "bao", "ap thap", "giong loc", "gio giat"),
        WeatherIntent.SUNRISE_SUNSET to listOf("binh minh", "hoang hon", "mat troi moc", "mat troi lan", "golden hour", "gio vang"),
        WeatherIntent.OUTDOOR_ACTIVITIES to listOf("chay bo", "da banh", "da bong", "the duc", "the thao", "dap xe", "di bo", "boi loi"),
        WeatherIntent.HEALTH_AND_WELLNESS to listOf("cam cum", "viem xoang", "dau dau", "muoi", "sot xuat huyet", "di ung", "suc khoe"),
        WeatherIntent.OUTFIT_ADVICE to listOf("mac gi", "trang phuc", "quan ao", "outfit", "ao khoac", "di giay"),
        WeatherIntent.TRAVEL_TOURISM to listOf(
            "da lat", "vung tau", "ha noi", "da nang", "phu quoc", "nha trang", "sa pa", "quy nhon", "can tho", "phan thiet",
            "di choi", "di phuot", "lich trinh", "di dau choi", "choi o dau", "da ngoai", "cam trai", "landmark 81", "pho di bo", "ben bach dang", "tay ninh", "can gio", "ho tri an",
            "quan 7", "q7", "cau anh sao", "ho ban nguyet", "phu my hung", "crescent mall", "sc vivocity",
            "truoc va sau 8h", "tam 8h", "khoang 8h", "8h toi", "20h",
            "doan duong", "duong di", "lo trinh", "tuyen duong", "kiem tra duong", "check doan duong",
            "lan can", "khu vuc lan can", "duong tranh", "tranh ngap", "ne ngap", "canh bao tranh"
        ),

        WeatherIntent.METEOROLOGY_SCIENCE to listOf("tai sao", "vi sao", "khi tuong", "sam set", "may dong"),
        WeatherIntent.FOLK_PROVERBS to listOf("ca dao", "tuc ngu", "chuon chuon", "dan gian"),
        WeatherIntent.FUTURE_FORECAST to listOf("ngay mai", "mai", "cuoi tuan", "thu bay", "chu nhat", "may ngay toi")
    )
}
