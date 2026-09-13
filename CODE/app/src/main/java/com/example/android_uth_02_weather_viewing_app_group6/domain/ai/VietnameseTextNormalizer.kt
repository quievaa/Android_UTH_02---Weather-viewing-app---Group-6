package com.example.android_uth_02_weather_viewing_app_group6.domain.ai

import java.text.Normalizer
import java.util.regex.Pattern

object VietnameseTextNormalizer {

    private val DIACRITICS_PATTERN = Pattern.compile("\\p{InCombiningDiacriticalMarks}+")

    //Chuyển chuỗi tiếng Việt thành không dấu, viết thường, loại bỏ ký tự đặc biệt.
    fun removeAccents(text: String): String {
        val nfdNormalized = Normalizer.normalize(text, Normalizer.Form.NFD)
        val withoutDiacritics = DIACRITICS_PATTERN.matcher(nfdNormalized).replaceAll("")
        return withoutDiacritics
            .replace('đ', 'd')
            .replace('Đ', 'd')
            .replace('ø', 'o')
    }

    //Chuẩn hóa từ viết tắt phổ biến của người Việt.
    fun expandSlangAndAbbreviations(text: String): String {
        var processed = text.lowercase()

        val slangMap = mapOf(
            "\\bko\\b" to "không",
            "\\bk\\b" to "không",
            "\\bhong\\b" to "không",
            "\\bhông\\b" to "không",
            "\\bhem\\b" to "không",
            "\\bkh\\b" to "không",
            "\\bdc\\b" to "được",
            "\\bđc\\b" to "được",
            "\\btui\\b" to "tôi",
            "\\bminh\\b" to "mình",
            "\\bcs1\\b" to "cơ sở 1",
            "\\bcs2\\b" to "cơ sở 2",
            "\\bcs3\\b" to "cơ sở 3",
            "\\buth\\b" to "trường đại học giao thông vận tải tphcm",
            "\\bbữa nay\\b" to "hôm nay",
            "\\bhnay\\b" to "hôm nay",
            "\\bhn\\b" to "hà nội",
            "\\btphcm\\b" to "thành phố hồ chí minh",
            "\\bsg\\b" to "sài gòn",
            "\\bđl\\b" to "đà lạt",
            "\\bvt\\b" to "vũng tàu",
            "\\bdn\\b" to "đà nẵng",
            "\\bpq\\b" to "phú quốc",
            "\\bnt\\b" to "nha trang"
        )

        for ((pattern, replacement) in slangMap) {
            processed = processed.replace(Regex(pattern), replacement)
        }
        return processed
    }

    //Làm sạch văn bản: chuẩn hóa khoảng trắng, viết thường, loại bỏ dấu câu thừa.
    fun clean(text: String): String {
        val expanded = expandSlangAndAbbreviations(text)
        return expanded
            .replace(Regex("[^\\p{L}\\p{Nd}\\s]"), " ")
            .replace(Regex("\\s+"), " ")
            .trim()
    }

    //Tách từ (Tokenize) ra danh sách các từ.
    fun tokenize(text: String): List<String> {
        val cleaned = clean(text)
        if (cleaned.isBlank()) return emptyList()
        return cleaned.split(" ").filter { it.isNotBlank() }
    }

    //Sinh danh sách(unigrams + bigrams) cho cả dạng có dấu và không dấu.
    fun extractNgrams(text: String): Set<String> {
        val tokens = tokenize(text)
        val ngrams = mutableSetOf<String>()

        // 1. Unigrams
        tokens.forEach { ngrams.add(it) }

        // 2. Bigrams
        for (i in 0 until tokens.size - 1) {
            ngrams.add("${tokens[i]} ${tokens[i + 1]}")
        }

        // 3. Trigrams (cho các cụm từ quan trọng)
        for (i in 0 until tokens.size - 2) {
            ngrams.add("${tokens[i]} ${tokens[i + 1]} ${tokens[i + 2]}")
        }

        //Bổ sung bản không dấu tương ứng
        val unaccentedTokens = tokens.map { removeAccents(it) }
        unaccentedTokens.forEach { ngrams.add(it) }
        for (i in 0 until unaccentedTokens.size - 1) {
            ngrams.add("${unaccentedTokens[i]} ${unaccentedTokens[i + 1]}")
        }
        for (i in 0 until unaccentedTokens.size - 2) {
            ngrams.add("${unaccentedTokens[i]} ${unaccentedTokens[i + 1]} ${unaccentedTokens[i + 2]}")
        }

        return ngrams
    }
}
