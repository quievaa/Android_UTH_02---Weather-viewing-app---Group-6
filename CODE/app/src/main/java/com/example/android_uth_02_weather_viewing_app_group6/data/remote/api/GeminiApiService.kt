package com.example.android_uth_02_weather_viewing_app_group6.data.remote.api

import com.google.gson.annotations.SerializedName
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.Header
import retrofit2.http.POST
import retrofit2.http.Path

data class GeminiPart(
    @SerializedName("text") val text: String
)

data class GeminiContent(
    @SerializedName("role") val role: String? = "user",
    @SerializedName("parts") val parts: List<GeminiPart>
)

data class GeminiSystemInstruction(
    @SerializedName("parts") val parts: List<GeminiPart>
)

data class GeminiRequest(
    @SerializedName("system_instruction") val systemInstruction: GeminiSystemInstruction? = null,
    @SerializedName("contents") val contents: List<GeminiContent>
)

data class GeminiCandidateContent(
    @SerializedName("parts") val parts: List<GeminiPart>? = null,
    @SerializedName("role") val role: String? = null
)

data class GeminiCandidate(
    @SerializedName("content") val content: GeminiCandidateContent? = null,
    @SerializedName("finishReason") val finishReason: String? = null
)

data class GeminiResponse(
    @SerializedName("candidates") val candidates: List<GeminiCandidate>? = null,
    @SerializedName("modelVersion") val modelVersion: String? = null
)

interface GeminiApiService {
    @POST("v1beta/models/{model}:generateContent")
    suspend fun generateContent(
        @Path("model") model: String,
        @Header("x-goog-api-key") apiKey: String,
        @Body request: GeminiRequest
    ): Response<GeminiResponse>
}
