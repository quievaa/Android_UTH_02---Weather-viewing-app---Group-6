package com.example.android_uth_02_weather_viewing_app_group6.ui.screens

import android.speech.tts.TextToSpeech
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.automirrored.filled.VolumeUp
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Psychology
import androidx.compose.material.icons.filled.School
import androidx.compose.material.icons.filled.Stop
import androidx.compose.material.icons.outlined.CleaningServices
import androidx.compose.material.icons.outlined.SmartToy
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.TabRowDefaults
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.android_uth_02_weather_viewing_app_group6.domain.ai.CustomKnowledgeItem
import com.example.android_uth_02_weather_viewing_app_group6.domain.ai.IntentCategory
import com.example.android_uth_02_weather_viewing_app_group6.domain.ai.WeatherIntent
import com.example.android_uth_02_weather_viewing_app_group6.ui.components.GlassCard
import com.example.android_uth_02_weather_viewing_app_group6.ui.components.Weather3DBackground
import com.example.android_uth_02_weather_viewing_app_group6.ui.model.ChatMessage
import com.example.android_uth_02_weather_viewing_app_group6.ui.model.WeatherCondition
import com.example.android_uth_02_weather_viewing_app_group6.ui.theme.AqiGreen
import com.example.android_uth_02_weather_viewing_app_group6.ui.theme.TextMuted
import com.example.android_uth_02_weather_viewing_app_group6.ui.theme.TextWhite
import com.example.android_uth_02_weather_viewing_app_group6.ui.theme.WeatherSkyBlue
import com.example.android_uth_02_weather_viewing_app_group6.ui.theme.WeatherSkyLight
import com.example.android_uth_02_weather_viewing_app_group6.ui.viewmodel.WeatherUiState
import com.example.android_uth_02_weather_viewing_app_group6.ui.viewmodel.WeatherViewModel
import java.util.Locale

@Composable
fun AiAssistantScreen(
    contentPadding: PaddingValues,
    viewModel: WeatherViewModel
) {
    val context = LocalContext.current
    val chatMessages by viewModel.chatMessages.collectAsState()
    val isAiThinking by viewModel.isAiThinking.collectAsState()
    val uiState by viewModel.uiState.collectAsState()
    val customKnowledgeList by viewModel.customKnowledgeList.collectAsState()
    val currentSuggestionChips by viewModel.currentSuggestionChips.collectAsState()

    var textInput by remember { mutableStateOf("") }
    val listState = rememberLazyListState()
    val focusManager = LocalFocusManager.current

    // Modal Huấn luyện & Tri thức AI
    var showTrainingHub by remember { mutableStateOf(false) }

    // Danh mục gợi ý được chọn (null = Gợi ý thông minh tự động từ phản hồi gần nhất)
    var selectedCategory by remember { mutableStateOf<IntentCategory?>(null) }

    // Text-To-Speech (TTS) đọc phản hồi tiếng Việt
    var tts: TextToSpeech? by remember { mutableStateOf(null) }
    var speakingMessageId by remember { mutableStateOf<String?>(null) }

    DisposableEffect(context) {
        var ttsInstance: TextToSpeech? = null
        ttsInstance = TextToSpeech(context) { status ->
            if (status == TextToSpeech.SUCCESS) {
                ttsInstance?.language = Locale("vi", "VN")
            }
        }
        tts = ttsInstance
        onDispose {
            ttsInstance?.stop()
            ttsInstance?.shutdown()
        }
    }

    // Dữ liệu thời tiết hiện tại để render nền 3D
    val currentWeather = (uiState as? WeatherUiState.Success)?.weather
    val isNight = currentWeather?.iconCode?.endsWith("n") == true || currentWeather?.description?.lowercase()?.contains("đêm") == true
    val condition = currentWeather?.let { WeatherCondition.fromDescription(it.description, isNight) } ?: WeatherCondition.PARTLY_CLOUDY
    val tempC = currentWeather?.temperatureC ?: 28.0
    val windMps = currentWeather?.windSpeedMps ?: 5.0

    // Tự động cuộn xuống tin nhắn mới nhất
    LaunchedEffect(chatMessages.size, isAiThinking) {
        if (chatMessages.isNotEmpty()) {
            listState.animateScrollToItem(chatMessages.size - 1)
        }
    }

    Box(modifier = Modifier.fillMaxSize()) {
        // Nền mô phỏng thời tiết sống động
        Weather3DBackground(
            condition = condition,
            isNight = isNight,
            windSpeedMps = windMps,
            temperatureC = tempC,
            modifier = Modifier.fillMaxSize()
        )

        // Lớp gradient mờ bảo đảm độ tương phản chữ
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    Brush.verticalGradient(
                        colors = listOf(
                            Color(0x660B0F19),
                            Color(0x880B0F19),
                            Color(0xCC0B0F19)
                        )
                    )
                )
        )

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(top = contentPadding.calculateTopPadding())
                .imePadding()
        ) {
            // Header Bar
            AiAssistantHeader(
                currentLocation = currentWeather?.cityName ?: "TP. Hồ Chí Minh",
                temp = tempC.toInt(),
                onOpenTrainingHub = { showTrainingHub = true },
                onClearChat = {
                    tts?.stop()
                    speakingMessageId = null
                    viewModel.clearChat()
                }
            )

            // Dải tab chọn nhóm chủ đề gợi ý nhanh
            CategorySuggestionTabs(
                selectedCategory = selectedCategory,
                onSelectCategory = { selectedCategory = it }
            )

            // Dải câu hỏi gợi ý nhanh (Chips)
            val displayChips = remember(selectedCategory, currentSuggestionChips) {
                if (selectedCategory == null) {
                    currentSuggestionChips
                } else {
                    WeatherIntent.entries
                        .filter { it.category == selectedCategory && it.sampleQuestion.isNotBlank() }
                        .map { it.sampleQuestion }
                }
            }

            LazyRow(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 6.dp),
                contentPadding = PaddingValues(horizontal = 16.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(displayChips) { prompt ->
                    QuickSuggestionChip(
                        text = prompt,
                        onClick = {
                            tts?.stop()
                            speakingMessageId = null
                            viewModel.sendChatMessage(prompt)
                        }
                    )
                }
            }

            // Danh sách tin nhắn trò chuyện
            LazyColumn(
                state = listState,
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth(),
                contentPadding = PaddingValues(
                    start = 16.dp,
                    end = 16.dp,
                    top = 6.dp,
                    bottom = 12.dp
                ),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                items(chatMessages, key = { it.id }) { message ->
                    ChatMessageBubble(
                        message = message,
                        isSpeaking = speakingMessageId == message.id,
                        onToggleSpeak = {
                            if (speakingMessageId == message.id) {
                                tts?.stop()
                                speakingMessageId = null
                            } else {
                                tts?.stop()
                                val cleanText = message.text
                                    .replace(Regex("[*•#👋🌧️👕🛵🏃🏖️☀️📍🔥🕶️🧥🌤️👟❌⏰🍃⏳🌦️💡🎒💻🚗🐾🌲🌊🏛️🌉🏝️⛰️🔬🌾🤖😊✨🎓]"), "")
                                    .replace(Regex("\\s+"), " ")
                                    .trim()
                                tts?.speak(cleanText, TextToSpeech.QUEUE_FLUSH, null, message.id)
                                speakingMessageId = message.id
                            }
                        }
                    )
                }

                if (isAiThinking) {
                    item {
                        AiThinkingBubble()
                    }
                }
            }

            // Thanh nhập câu hỏi dưới đáy màn hình
            AiChatInputBar(
                text = textInput,
                onTextChanged = { textInput = it },
                onSend = {
                    if (textInput.isNotBlank()) {
                        val toSend = textInput
                        textInput = ""
                        focusManager.clearFocus()
                        tts?.stop()
                        speakingMessageId = null
                        viewModel.sendChatMessage(toSend)
                    }
                },
                isThinking = isAiThinking,
                bottomPadding = contentPadding.calculateBottomPadding() + 8.dp
            )
        }
    }

    // Modal Trung tâm Huấn luyện & Tri thức AI
    if (showTrainingHub) {
        AiTrainingHubDialog(
            customKnowledgeList = customKnowledgeList,
            onDismiss = { showTrainingHub = false },
            onSelectPrompt = { prompt ->
                showTrainingHub = false
                viewModel.sendChatMessage(prompt)
            },
            onTeach = { question, answer ->
                viewModel.teachAi(question, answer)
            },
            onDelete = { id ->
                viewModel.deleteCustomKnowledge(id)
            }
        )
    }
}

@Composable
private fun AiAssistantHeader(
    currentLocation: String,
    temp: Int,
    onOpenTrainingHub: () -> Unit,
    onClearChat: () -> Unit
) {
    GlassCard(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 4.dp),
        cornerRadius = 20.dp,
        backgroundColor = Color(0x331E293B),
        borderColor = Color(0x3394A3B8)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 14.dp, vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // Glowing Avatar
                Box(
                    modifier = Modifier
                        .size(42.dp)
                        .clip(CircleShape)
                        .background(
                            Brush.linearGradient(
                                listOf(WeatherSkyLight, Color(0xFF6366F1))
                            )
                        )
                        .shadow(elevation = 6.dp, shape = CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Filled.AutoAwesome,
                        contentDescription = "AI Bot",
                        tint = Color.White,
                        modifier = Modifier.size(22.dp)
                    )
                }

                Column {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Text(
                            text = "Trợ lý UTH AI",
                            fontWeight = FontWeight.Bold,
                            fontSize = 15.sp,
                            color = TextWhite
                        )
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(6.dp))
                                .background(Color(0x3338BDF8))
                                .padding(horizontal = 5.dp, vertical = 1.dp)
                        ) {
                            Text(
                                text = "✨ Gemini 3.5",
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFF38BDF8)
                            )
                        }
                        Box(
                            modifier = Modifier
                                .size(7.dp)
                                .clip(CircleShape)
                                .background(AqiGreen)
                        )
                    }
                    Text(
                        text = "$currentLocation • $temp°C • 25+ Chủ đề",
                        fontSize = 12.sp,
                        color = TextMuted
                    )

                }
            }

            Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                // Nút mở Trung tâm Huấn luyện AI
                IconButton(
                    onClick = onOpenTrainingHub,
                    modifier = Modifier
                        .size(36.dp)
                        .clip(CircleShape)
                        .background(Color(0x336366F1))
                ) {
                    Icon(
                        imageVector = Icons.Filled.School,
                        contentDescription = "Huấn luyện AI",
                        tint = Color(0xFF818CF8),
                        modifier = Modifier.size(18.dp)
                    )
                }

                // Nút xóa lịch sử chat
                IconButton(
                    onClick = onClearChat,
                    modifier = Modifier
                        .size(36.dp)
                        .clip(CircleShape)
                        .background(Color(0x22FFFFFF))
                ) {
                    Icon(
                        imageVector = Icons.Outlined.CleaningServices,
                        contentDescription = "Làm mới",
                        tint = TextMuted,
                        modifier = Modifier.size(18.dp)
                    )
                }
            }
        }
    }
}

@Composable
private fun CategorySuggestionTabs(
    selectedCategory: IntentCategory?,
    onSelectCategory: (IntentCategory?) -> Unit
) {
    LazyRow(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 2.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        item {
            CategoryFilterChip(
                label = "✨ Đề xuất",
                isSelected = selectedCategory == null,
                onClick = { onSelectCategory(null) }
            )
        }
        items(IntentCategory.entries) { cat ->
            CategoryFilterChip(
                label = cat.titleVi,
                isSelected = selectedCategory == cat,
                onClick = { onSelectCategory(cat) }
            )
        }
    }
}

@Composable
private fun CategoryFilterChip(
    label: String,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(14.dp))
            .background(
                if (isSelected) Brush.linearGradient(listOf(Color(0xFF0284C7), Color(0xFF2563EB)))
                else Brush.linearGradient(listOf(Color(0x22334155), Color(0x221E293B)))
            )
            .border(
                width = 1.dp,
                color = if (isSelected) Color(0xFF38BDF8) else Color(0x2294A3B8),
                shape = RoundedCornerShape(14.dp)
            )
            .clickable { onClick() }
            .padding(horizontal = 10.dp, vertical = 4.dp)
    ) {
        Text(
            text = label,
            fontSize = 11.sp,
            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
            color = if (isSelected) Color.White else Color(0xFF94A3B8)
        )
    }
}

@Composable
private fun QuickSuggestionChip(
    text: String,
    onClick: () -> Unit
) {
    GlassCard(
        modifier = Modifier,
        cornerRadius = 18.dp,
        backgroundColor = Color(0x331E293B),
        borderColor = Color(0x4438BDF8),
        onClick = onClick
    ) {
        Text(
            text = text,
            fontSize = 12.sp,
            fontWeight = FontWeight.Medium,
            color = Color(0xFFE0F2FE),
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 7.dp)
        )
    }
}

@Composable
private fun ChatMessageBubble(
    message: ChatMessage,
    isSpeaking: Boolean,
    onToggleSpeak: () -> Unit
) {
    val isUser = message.isUser
    val formattedTime = message.timestamp

    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = if (isUser) Arrangement.End else Arrangement.Start,
        verticalAlignment = Alignment.Top
    ) {
        if (!isUser) {
            Box(
                modifier = Modifier
                    .padding(top = 4.dp, end = 8.dp)
                    .size(28.dp)
                    .clip(CircleShape)
                    .background(
                        Brush.linearGradient(
                            listOf(WeatherSkyLight, Color(0xFF6366F1))
                        )
                    ),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Outlined.SmartToy,
                    contentDescription = "AI",
                    tint = Color.White,
                    modifier = Modifier.size(15.dp)
                )
            }
        }

        Column(
            horizontalAlignment = if (isUser) Alignment.End else Alignment.Start,
            modifier = Modifier.widthIn(max = 310.dp)
        ) {
            if (isUser) {
                Box(
                    modifier = Modifier
                        .clip(
                            RoundedCornerShape(
                                topStart = 18.dp,
                                topEnd = 4.dp,
                                bottomStart = 18.dp,
                                bottomEnd = 18.dp
                            )
                        )
                        .background(
                            Brush.linearGradient(
                                listOf(WeatherSkyBlue, Color(0xFF2563EB))
                            )
                        )
                        .padding(horizontal = 14.dp, vertical = 10.dp)
                ) {
                    Text(
                        text = message.text,
                        fontSize = 14.sp,
                        color = Color.White,
                        lineHeight = 20.sp
                    )
                }
            } else {
                GlassCard(
                    modifier = Modifier.fillMaxWidth(),
                    cornerRadius = 18.dp,
                    backgroundColor = Color(0x4D1E293B),
                    borderColor = Color(0x3394A3B8)
                ) {
                    Column(
                        modifier = Modifier.padding(horizontal = 14.dp, vertical = 12.dp)
                    ) {
                        // Tag chips & Nút đọc Text-To-Speech
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(bottom = 6.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                                message.tags.forEach { tag ->
                                    Text(
                                        text = "• $tag",
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.SemiBold,
                                        color = WeatherSkyLight
                                    )
                                }
                            }

                            // Nút nghe phát âm giọng nói (TTS)
                            IconButton(
                                onClick = onToggleSpeak,
                                modifier = Modifier
                                    .size(26.dp)
                                    .clip(CircleShape)
                                    .background(if (isSpeaking) Color(0x44EF4444) else Color(0x2238BDF8))
                            ) {
                                Icon(
                                    imageVector = if (isSpeaking) Icons.Filled.Stop else Icons.AutoMirrored.Filled.VolumeUp,
                                    contentDescription = "Đọc giọng nói",
                                    tint = if (isSpeaking) Color(0xFFF87171) else Color(0xFF38BDF8),
                                    modifier = Modifier.size(15.dp)
                                )
                            }
                        }

                        Text(
                            text = message.text,
                            fontSize = 13.5.sp,
                            color = Color(0xFFF1F5F9),
                            lineHeight = 20.sp
                        )
                    }
                }
            }

            // Timestamp
            Text(
                text = formattedTime,
                fontSize = 10.sp,
                color = Color(0x8894A3B8),
                modifier = Modifier.padding(top = 3.dp, start = 4.dp, end = 4.dp)
            )
        }
    }
}

@Composable
private fun AiThinkingBubble() {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.Start,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .padding(end = 8.dp)
                .size(28.dp)
                .clip(CircleShape)
                .background(
                    Brush.linearGradient(
                        listOf(WeatherSkyLight, Color(0xFF6366F1))
                    )
                ),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.Outlined.SmartToy,
                contentDescription = "AI",
                tint = Color.White,
                modifier = Modifier.size(15.dp)
            )
        }

        GlassCard(
            modifier = Modifier,
            cornerRadius = 18.dp,
            backgroundColor = Color(0x4D1E293B),
            borderColor = Color(0x3394A3B8)
        ) {
            Row(
                modifier = Modifier.padding(horizontal = 14.dp, vertical = 10.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                val infiniteTransition = rememberInfiniteTransition(label = "ai_thinking")
                val dot1Scale by infiniteTransition.animateFloat(
                    initialValue = 0.4f,
                    targetValue = 1.0f,
                    animationSpec = infiniteRepeatable(
                        animation = tween(600, easing = FastOutSlowInEasing),
                        repeatMode = RepeatMode.Reverse
                    ),
                    label = "dot1"
                )
                val dot2Scale by infiniteTransition.animateFloat(
                    initialValue = 0.4f,
                    targetValue = 1.0f,
                    animationSpec = infiniteRepeatable(
                        animation = tween(600, delayMillis = 200, easing = FastOutSlowInEasing),
                        repeatMode = RepeatMode.Reverse
                    ),
                    label = "dot2"
                )
                val dot3Scale by infiniteTransition.animateFloat(
                    initialValue = 0.4f,
                    targetValue = 1.0f,
                    animationSpec = infiniteRepeatable(
                        animation = tween(600, delayMillis = 400, easing = FastOutSlowInEasing),
                        repeatMode = RepeatMode.Reverse
                    ),
                    label = "dot3"
                )

                Box(
                    modifier = Modifier
                        .size(7.dp)
                        .scale(dot1Scale)
                        .clip(CircleShape)
                        .background(WeatherSkyLight)
                )
                Box(
                    modifier = Modifier
                        .size(7.dp)
                        .scale(dot2Scale)
                        .clip(CircleShape)
                        .background(WeatherSkyLight)
                )
                Box(
                    modifier = Modifier
                        .size(7.dp)
                        .scale(dot3Scale)
                        .clip(CircleShape)
                        .background(WeatherSkyLight)
                )
                Spacer(modifier = Modifier.width(4.dp))
                Text(
                    text = "Trợ lý UTH AI đang phân tích dữ liệu...",
                    fontSize = 12.sp,
                    color = TextMuted
                )
            }
        }
    }
}

@Composable
private fun AiChatInputBar(
    text: String,
    onTextChanged: (String) -> Unit,
    onSend: () -> Unit,
    isThinking: Boolean,
    bottomPadding: androidx.compose.ui.unit.Dp
) {
    GlassCard(
        modifier = Modifier
            .fillMaxWidth()
            .padding(start = 16.dp, end = 16.dp, top = 4.dp, bottom = bottomPadding),
        cornerRadius = 28.dp,
        backgroundColor = Color(0x661E293B),
        borderColor = Color(0x4494A3B8)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 8.dp, vertical = 4.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            OutlinedTextField(
                value = text,
                onValueChange = onTextChanged,
                placeholder = {
                    Text(
                        text = "Hỏi thời tiết, trang phục, UTH...",
                        fontSize = 14.sp,
                        color = Color(0x8894A3B8)
                    )
                },
                modifier = Modifier
                    .weight(1f)
                    .padding(end = 4.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedContainerColor = Color.Transparent,
                    unfocusedContainerColor = Color.Transparent,
                    focusedBorderColor = Color.Transparent,
                    unfocusedBorderColor = Color.Transparent,
                    focusedTextColor = TextWhite,
                    unfocusedTextColor = TextWhite
                ),
                singleLine = true,
                keyboardOptions = KeyboardOptions(imeAction = ImeAction.Send),
                keyboardActions = KeyboardActions(onSend = { onSend() })
            )

            val canSend = text.isNotBlank() && !isThinking
            IconButton(
                onClick = onSend,
                enabled = canSend,
                modifier = Modifier
                    .size(42.dp)
                    .clip(CircleShape)
                    .background(
                        if (canSend) Brush.linearGradient(
                            listOf(WeatherSkyBlue, WeatherSkyLight)
                        ) else Brush.linearGradient(
                            listOf(Color(0x3364748B), Color(0x3364748B))
                        )
                    )
            ) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.Send,
                    contentDescription = "Gửi tin nhắn",
                    tint = if (canSend) Color.White else Color(0x66FFFFFF),
                    modifier = Modifier.size(19.dp)
                )
            }
        }
    }
}

/**
 * Modal Trung tâm Huấn luyện & Tri thức AI (AI Training & Knowledge Hub).
 */
@Composable
private fun AiTrainingHubDialog(
    customKnowledgeList: List<CustomKnowledgeItem>,
    onDismiss: () -> Unit,
    onSelectPrompt: (String) -> Unit,
    onTeach: (question: String, answer: String) -> Unit,
    onDelete: (id: String) -> Unit
) {
    var selectedTab by remember { mutableIntStateOf(0) }
    var teachQuestion by remember { mutableStateOf("") }
    var teachAnswer by remember { mutableStateOf("") }
    var filterCategory by remember { mutableStateOf<IntentCategory?>(null) }

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth(0.94f)
                .fillMaxHeight(0.85f)
                .clip(RoundedCornerShape(24.dp))
                .background(Color(0xFF0F172A))
                .border(1.dp, Color(0x446366F1), RoundedCornerShape(24.dp))
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp)
            ) {
                // Dialog Header
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(36.dp)
                                .clip(CircleShape)
                                .background(Brush.linearGradient(listOf(Color(0xFF6366F1), Color(0xFF38BDF8)))),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Filled.School,
                                contentDescription = null,
                                tint = Color.White,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                        Column {
                            Text(
                                text = "Trung tâm Huấn luyện AI",
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Bold,
                                color = TextWhite
                            )
                            Text(
                                text = "Mô hình NLP Tiếng Việt & Dữ liệu Tri thức",
                                fontSize = 11.sp,
                                color = TextMuted
                            )
                        }
                    }

                    IconButton(onClick = onDismiss) {
                        Icon(
                            imageVector = Icons.Filled.Close,
                            contentDescription = "Đóng",
                            tint = TextMuted
                        )
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Tab Switcher
                TabRow(
                    selectedTabIndex = selectedTab,
                    containerColor = Color(0x331E293B),
                    contentColor = TextWhite,
                    indicator = { tabPositions ->
                        TabRowDefaults.SecondaryIndicator(
                            Modifier.tabIndicatorOffset(tabPositions[selectedTab]),
                            color = Color(0xFF38BDF8)
                        )
                    }
                ) {
                    Tab(
                        selected = selectedTab == 0,
                        onClick = { selectedTab = 0 },
                        text = {
                            Text(
                                "📚 Bộ Huấn luyện (25+)",
                                fontSize = 13.sp,
                                fontWeight = if (selectedTab == 0) FontWeight.Bold else FontWeight.Normal
                            )
                        }
                    )
                    Tab(
                        selected = selectedTab == 1,
                        onClick = { selectedTab = 1 },
                        text = {
                            Text(
                                "✍️ Dạy thêm (${customKnowledgeList.size})",
                                fontSize = 13.sp,
                                fontWeight = if (selectedTab == 1) FontWeight.Bold else FontWeight.Normal
                            )
                        }
                    )
                }

                Spacer(modifier = Modifier.height(12.dp))

                if (selectedTab == 0) {
                    // TAB 0: Thống kê & Danh mục huấn luyện
                    Column(modifier = Modifier.fillMaxSize()) {
                        // Thống kê tóm tắt
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            StatCard(title = "Ý định", value = "25+", modifier = Modifier.weight(1f))
                            StatCard(title = "Mô hình", value = "Gemini 3.5", modifier = Modifier.weight(1.3f))
                            StatCard(title = "Huấn luyện", value = "Live NLP", modifier = Modifier.weight(1.2f))
                            StatCard(title = "Tự dạy", value = "${customKnowledgeList.size}", modifier = Modifier.weight(0.9f))

                        }

                        Spacer(modifier = Modifier.height(10.dp))

                        // Category filter chips
                        LazyRow(
                            horizontalArrangement = Arrangement.spacedBy(6.dp),
                            modifier = Modifier.padding(bottom = 8.dp)
                        ) {
                            item {
                                CategoryFilterChip(
                                    label = "Tất cả",
                                    isSelected = filterCategory == null,
                                    onClick = { filterCategory = null }
                                )
                            }
                            items(IntentCategory.entries) { cat ->
                                CategoryFilterChip(
                                    label = cat.titleVi,
                                    isSelected = filterCategory == cat,
                                    onClick = { filterCategory = cat }
                                )
                            }
                        }

                        // Danh sách các Intent đã huấn luyện
                        val filteredIntents = remember(filterCategory) {
                            if (filterCategory == null) WeatherIntent.entries.filter { it.sampleQuestion.isNotBlank() }
                            else WeatherIntent.entries.filter { it.category == filterCategory && it.sampleQuestion.isNotBlank() }
                        }

                        LazyColumn(
                            modifier = Modifier.fillMaxSize(),
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            items(filteredIntents) { intent ->
                                GlassCard(
                                    modifier = Modifier.fillMaxWidth(),
                                    cornerRadius = 14.dp,
                                    backgroundColor = Color(0x331E293B),
                                    borderColor = Color(0x2294A3B8),
                                    onClick = { onSelectPrompt(intent.sampleQuestion) }
                                ) {
                                    Column(modifier = Modifier.padding(12.dp)) {
                                        Row(
                                            modifier = Modifier.fillMaxWidth(),
                                            horizontalArrangement = Arrangement.SpaceBetween,
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Text(
                                                text = intent.displayName,
                                                fontSize = 13.sp,
                                                fontWeight = FontWeight.Bold,
                                                color = Color(0xFF38BDF8)
                                            )
                                            Text(
                                                text = intent.category.titleVi,
                                                fontSize = 11.sp,
                                                color = TextMuted
                                            )
                                        }
                                        Spacer(modifier = Modifier.height(3.dp))
                                        Text(
                                            text = intent.descriptionVi,
                                            fontSize = 11.sp,
                                            color = Color(0xFFCBD5E1)
                                        )
                                        Spacer(modifier = Modifier.height(6.dp))
                                        Row(
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .clip(RoundedCornerShape(8.dp))
                                                .background(Color(0x2238BDF8))
                                                .padding(horizontal = 8.dp, vertical = 4.dp),
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Text(
                                                text = "💬 Ví dụ: \"${intent.sampleQuestion}\"",
                                                fontSize = 11.sp,
                                                color = Color(0xFFBAE6FD)
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }
                } else {
                    // TAB 1: Dạy thêm cho AI
                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        item {
                            GlassCard(
                                modifier = Modifier.fillMaxWidth(),
                                cornerRadius = 16.dp,
                                backgroundColor = Color(0x441E293B),
                                borderColor = Color(0x336366F1)
                            ) {
                                Column(modifier = Modifier.padding(14.dp)) {
                                    Text(
                                        text = "✨ Dạy câu hỏi & câu trả lời mới cho AI:",
                                        fontSize = 13.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = TextWhite
                                    )
                                    Spacer(modifier = Modifier.height(8.dp))

                                    OutlinedTextField(
                                        value = teachQuestion,
                                        onValueChange = { teachQuestion = it },
                                        placeholder = { Text("Nhập câu hỏi (Ví dụ: Chỗ gửi xe CS1 UTH ở đâu?)", fontSize = 12.sp, color = TextMuted) },
                                        modifier = Modifier.fillMaxWidth(),
                                        colors = OutlinedTextFieldDefaults.colors(
                                            focusedBorderColor = Color(0xFF6366F1),
                                            unfocusedBorderColor = Color(0x4494A3B8),
                                            focusedTextColor = TextWhite,
                                            unfocusedTextColor = TextWhite
                                        ),
                                        singleLine = true
                                    )

                                    Spacer(modifier = Modifier.height(8.dp))

                                    OutlinedTextField(
                                        value = teachAnswer,
                                        onValueChange = { teachAnswer = it },
                                        placeholder = { Text("Nhập câu trả lời AI sẽ đáp...", fontSize = 12.sp, color = TextMuted) },
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .height(90.dp),
                                        colors = OutlinedTextFieldDefaults.colors(
                                            focusedBorderColor = Color(0xFF6366F1),
                                            unfocusedBorderColor = Color(0x4494A3B8),
                                            focusedTextColor = TextWhite,
                                            unfocusedTextColor = TextWhite
                                        ),
                                        maxLines = 4
                                    )

                                    Spacer(modifier = Modifier.height(10.dp))

                                    Button(
                                        onClick = {
                                            if (teachQuestion.isNotBlank() && teachAnswer.isNotBlank()) {
                                                onTeach(teachQuestion, teachAnswer)
                                                teachQuestion = ""
                                                teachAnswer = ""
                                            }
                                        },
                                        enabled = teachQuestion.isNotBlank() && teachAnswer.isNotBlank(),
                                        modifier = Modifier.fillMaxWidth(),
                                        colors = ButtonDefaults.buttonColors(
                                            containerColor = Color(0xFF6366F1),
                                            contentColor = Color.White
                                        ),
                                        shape = RoundedCornerShape(12.dp)
                                    ) {
                                        Text("🎓 Huấn luyện & Lưu vào bộ nhớ AI", fontSize = 13.sp, fontWeight = FontWeight.SemiBold)
                                    }
                                }
                            }
                        }

                        item {
                            Text(
                                text = "Danh sách kiến thức đã nạp (${customKnowledgeList.size} mục):",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                color = TextMuted,
                                modifier = Modifier.padding(top = 4.dp)
                            )
                        }

                        items(customKnowledgeList) { item ->
                            GlassCard(
                                modifier = Modifier.fillMaxWidth(),
                                cornerRadius = 14.dp,
                                backgroundColor = Color(0x331E293B),
                                borderColor = Color(0x2294A3B8),
                                onClick = { onSelectPrompt(item.question) }
                            ) {
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(12.dp),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.Top
                                ) {
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(
                                            text = "❓ ${item.question}",
                                            fontSize = 13.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = Color(0xFFBAE6FD)
                                        )
                                        Spacer(modifier = Modifier.height(4.dp))
                                        Text(
                                            text = item.answer,
                                            fontSize = 11.sp,
                                            color = Color(0xFFCBD5E1),
                                            maxLines = 3
                                        )
                                    }

                                    IconButton(
                                        onClick = { onDelete(item.id) },
                                        modifier = Modifier.size(30.dp)
                                    ) {
                                        Icon(
                                            imageVector = Icons.Filled.Delete,
                                            contentDescription = "Xóa",
                                            tint = Color(0xFFF87171),
                                            modifier = Modifier.size(16.dp)
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun StatCard(
    title: String,
    value: String,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(12.dp))
            .background(Color(0x331E293B))
            .border(1.dp, Color(0x2294A3B8), RoundedCornerShape(12.dp))
            .padding(vertical = 8.dp, horizontal = 6.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(
                text = value,
                fontSize = 13.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF38BDF8)
            )
            Text(
                text = title,
                fontSize = 10.sp,
                color = TextMuted
            )
        }
    }
}
