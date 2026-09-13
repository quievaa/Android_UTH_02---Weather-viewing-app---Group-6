package com.example.android_uth_02_weather_viewing_app_group6.ui.model

import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

data class ChatMessage(
    val id: String = "msg_${System.currentTimeMillis()}_${(100..999).random()}",
    val text: String,
    val isUser: Boolean,
    val timestamp: String = SimpleDateFormat("HH:mm", Locale.getDefault()).format(Date()),
    val tags: List<String> = emptyList(),
    val isThinking: Boolean = false
)
