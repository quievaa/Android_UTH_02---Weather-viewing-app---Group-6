package com.example.android_uth_02_weather_viewing_app_group6.data.voice

import android.content.Context
import android.os.Bundle
import android.speech.tts.TextToSpeech
import android.speech.tts.UtteranceProgressListener
import android.util.Log
import java.util.Locale
import java.util.UUID

class VoiceWeatherAlertManager(
    context: Context,
    private val onInitComplete: ((Boolean, String?) -> Unit)? = null
) : TextToSpeech.OnInitListener {

    private val appContext = context.applicationContext
    private var tts: TextToSpeech? = null
    private var isInitialized = false
    private var pendingTextToSpeak: String? = null
    private var pendingSpeechRate: Float = 1.0f

    private var onStartCallback: (() -> Unit)? = null
    private var onDoneCallback: (() -> Unit)? = null
    private var onErrorCallback: ((String) -> Unit)? = null

    init {
        try {
            tts = TextToSpeech(appContext, this)
        } catch (e: Exception) {
            Log.e(TAG, "Lỗi khi khởi tạo TextToSpeech engine", e)
            onInitComplete?.invoke(false, "Không thể khởi động hệ thống giọng nói: ${e.message}")
        }
    }

    override fun onInit(status: Int) {
        if (status == TextToSpeech.SUCCESS) {
            val engine = tts
            if (engine == null) {
                isInitialized = false
                onInitComplete?.invoke(false, "TextToSpeech engine null")
                return
            }

            // Ưu tiên Locale tiếng Việt (vi-VN hoặc vi)
            val viLocale = Locale("vi", "VN")
            val langResult = engine.setLanguage(viLocale)

            if (langResult == TextToSpeech.LANG_MISSING_DATA || langResult == TextToSpeech.LANG_NOT_SUPPORTED) {
                val fallbackVi = Locale("vi")
                val fallbackResult = engine.setLanguage(fallbackVi)
                if (fallbackResult == TextToSpeech.LANG_MISSING_DATA || fallbackResult == TextToSpeech.LANG_NOT_SUPPORTED) {
                    Log.w(TAG, "Tiếng Việt chưa được cài đặt trên TTS engine máy này. Dùng ngôn ngữ mặc định.")
                }
            }

            engine.setOnUtteranceProgressListener(object : UtteranceProgressListener() {
                override fun onStart(utteranceId: String?) {
                    onStartCallback?.invoke()
                }

                override fun onDone(utteranceId: String?) {
                    onDoneCallback?.invoke()
                }

                @Deprecated("Deprecated in Java")
                override fun onError(utteranceId: String?) {
                    onErrorCallback?.invoke("Lỗi phát giọng đọc")
                }

                override fun onError(utteranceId: String?, errorCode: Int) {
                    onErrorCallback?.invoke("Lỗi phát giọng đọc (mã lỗi: $errorCode)")
                }
            })

            isInitialized = true
            onInitComplete?.invoke(true, null)

            // Nếu có câu cần đọc trước khi init xong thì đọc ngay
            pendingTextToSpeak?.let { text ->
                speak(text, pendingSpeechRate)
                pendingTextToSpeak = null
            }
        } else {
            isInitialized = false
            Log.e(TAG, "Khởi tạo TextToSpeech thất bại với mã trạng thái: $status")
            onInitComplete?.invoke(false, "Khởi tạo TextToSpeech thất bại")
        }
    }

    fun speak(
        text: String,
        speechRate: Float = 1.0f,
        pitch: Float = 1.0f,
        onStart: (() -> Unit)? = null,
        onDone: (() -> Unit)? = null,
        onError: ((String) -> Unit)? = null
    ) {
        this.onStartCallback = onStart
        this.onDoneCallback = onDone
        this.onErrorCallback = onError

        if (!isInitialized) {
            pendingTextToSpeak = text
            pendingSpeechRate = speechRate
            return
        }

        val engine = tts ?: return

        try {
            engine.stop()
            engine.setSpeechRate(speechRate.coerceIn(0.5f, 2.0f))
            engine.setPitch(pitch.coerceIn(0.5f, 2.0f))

            val utteranceId = "utterance_${UUID.randomUUID()}"
            val params = Bundle().apply {
                putString(TextToSpeech.Engine.KEY_PARAM_UTTERANCE_ID, utteranceId)
            }

            engine.speak(text, TextToSpeech.QUEUE_FLUSH, params, utteranceId)
        } catch (e: Exception) {
            Log.e(TAG, "Lỗi khi gọi speak()", e)
            onError?.invoke("Không thể phát giọng nói: ${e.message}")
        }
    }

    fun stop() {
        try {
            tts?.stop()
            onDoneCallback?.invoke()
        } catch (e: Exception) {
            Log.e(TAG, "Lỗi khi stop()", e)
        }
    }

    fun isSpeaking(): Boolean {
        return try {
            tts?.isSpeaking == true
        } catch (_: Exception) {
            false
        }
    }

    fun setSpeechRate(rate: Float) {
        try {
            tts?.setSpeechRate(rate.coerceIn(0.5f, 2.0f))
        } catch (e: Exception) {
            Log.e(TAG, "Lỗi setSpeechRate", e)
        }
    }

    fun shutdown() {
        try {
            tts?.stop()
            tts?.shutdown()
            tts = null
            isInitialized = false
        } catch (e: Exception) {
            Log.e(TAG, "Lỗi khi giải phóng TTS engine", e)
        }
    }

    companion object {
        private const val TAG = "VoiceAlertManager"
    }
}
