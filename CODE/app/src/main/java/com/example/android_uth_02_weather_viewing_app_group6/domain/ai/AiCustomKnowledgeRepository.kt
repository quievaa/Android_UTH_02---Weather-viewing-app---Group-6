package com.example.android_uth_02_weather_viewing_app_group6.domain.ai

import android.content.Context
import android.content.SharedPreferences
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken

data class CustomKnowledgeItem(
    val id: String = System.currentTimeMillis().toString(),
    val question: String,
    val answer: String,
    val createdAt: Long = System.currentTimeMillis()
)

/**
 * Kho Tri thức Tùy chỉnh (Custom Knowledge Repository).
 * Cho phép người dùng trực tiếp "huấn luyện", dạy thêm các câu hỏi và câu trả lời riêng cho Trợ lý AI.
 */
class AiCustomKnowledgeRepository(context: Context) {

    private val prefs: SharedPreferences = context.getSharedPreferences("ai_custom_knowledge_prefs", Context.MODE_PRIVATE)
    private val gson = Gson()
    private val keyKnowledgeList = "custom_knowledge_list_v1"

    init {
        // Khởi tạo một số câu hỏi mẫu đặc thù cho sinh viên UTH nếu chưa có dữ liệu
        if (!prefs.contains(keyKnowledgeList)) {
            val defaultSamples = listOf(
                CustomKnowledgeItem(
                    id = "sample_uth_1",
                    question = "Chỗ trú mưa gần Cơ sở 1 UTH ở đâu?",
                    answer = "☕ **Gợi ý trú mưa gần CS1 (Ung Văn Khiêm):**\n• Bạn có thể ghé các quán cà phê dọc đường D2 (Nguyễn Gia Trí) như Highland, Phúc Long, hoặc The Coffee House.\n• Ngay trong sảnh tòa nhà chính CS1 UTH cũng có khu vực tự học có mái che và quạt mát!"
                ),
                CustomKnowledgeItem(
                    id = "sample_uth_2",
                    question = "Học thể dục UTH trời mưa có phải học không?",
                    answer = "🏃 **Thông tin học Giáo dục thể chất UTH khi mưa:**\n• Nếu học tại sân bóng ngoài trời hoặc sân vận động khi mưa to dông sét, giảng viên thường chuyển sang học lý thuyết trong nhà thi đấu hoặc thông báo bù giờ sau."
                ),
                CustomKnowledgeItem(
                    id = "sample_uth_3",
                    question = "Xe buýt nào đi qua các cơ sở UTH?",
                    answer = "🚌 **Tuyến xe buýt kết nối UTH:**\n• **Cơ sở 1 (Bình Thạnh):** Tuyến 05, 44, 104 đi qua Ung Văn Khiêm & D2.\n• **Cơ sở 2 (Quận 12):** Tuyến 24, 145 đi qua Tô Ký - Chợ Cầu.\n• **Cơ sở 3 (TP. Thủ Đức):** Tuyến 29, 99 đi qua Đỗ Xuân Hợp."
                )
            )
            saveAll(defaultSamples)
        }
    }

    fun getAll(): List<CustomKnowledgeItem> {
        val json = prefs.getString(keyKnowledgeList, null) ?: return emptyList()
        return try {
            val type = object : TypeToken<List<CustomKnowledgeItem>>() {}.type
            gson.fromJson(json, type) ?: emptyList()
        } catch (e: Exception) {
            emptyList()
        }
    }

    fun addKnowledge(question: String, answer: String): CustomKnowledgeItem {
        val current = getAll().toMutableList()
        val newItem = CustomKnowledgeItem(question = question.trim(), answer = answer.trim())
        current.add(0, newItem)
        saveAll(current)
        return newItem
    }

    fun deleteKnowledge(id: String) {
        val updated = getAll().filter { it.id != id }
        saveAll(updated)
    }

    /**
     * Tìm kiếm xem câu hỏi của người dùng có khớp với kiến thức người dùng đã dạy không.
     */
    fun findMatchingAnswer(query: String): String? {
        val cleanedQuery = VietnameseTextNormalizer.clean(query)
        val unaccentedQuery = VietnameseTextNormalizer.removeAccents(cleanedQuery)
        val list = getAll()

        for (item in list) {
            val itemClean = VietnameseTextNormalizer.clean(item.question)
            val itemUnaccented = VietnameseTextNormalizer.removeAccents(itemClean)

            // Khớp chính xác hoặc chứa câu hỏi
            if (unaccentedQuery == itemUnaccented ||
                unaccentedQuery.contains(itemUnaccented) ||
                itemUnaccented.contains(unaccentedQuery)
            ) {
                return item.answer
            }
        }
        return null
    }

    private fun saveAll(list: List<CustomKnowledgeItem>) {
        val json = gson.toJson(list)
        prefs.edit().putString(keyKnowledgeList, json).apply()
    }
}
