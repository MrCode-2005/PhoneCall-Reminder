package com.phonecall.reminder.audio

import android.content.Context
import android.media.AudioAttributes
import android.media.AudioFocusRequest
import android.media.AudioManager
import android.os.Build
import android.os.Handler
import android.os.Looper
import android.speech.tts.TextToSpeech
import android.speech.tts.UtteranceProgressListener
import java.util.Locale
import java.util.UUID
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class TextToSpeechManager @Inject constructor(
    private val context: Context
) {
    private var tts: TextToSpeech? = null
    private var isInitialized = false
    private var currentMessage: String? = null
    private var isLooping = false
    private var onInitCallback: ((Boolean) -> Unit)? = null
    
    private val audioManager = context.getSystemService(Context.AUDIO_SERVICE) as AudioManager
    private var audioFocusRequest: AudioFocusRequest? = null
    private var isSpeakerOn = false
    
    private val handler = Handler(Looper.getMainLooper())
    
    fun initialize(onInit: (Boolean) -> Unit = {}) {
        onInitCallback = onInit
        tts = TextToSpeech(context) { status ->
            isInitialized = status == TextToSpeech.SUCCESS
            if (isInitialized) {
                configureTts()
            }
            onInitCallback?.invoke(isInitialized)
        }
    }
    
    private fun configureTts() {
        tts?.apply {
            language = Locale.US
            setSpeechRate(0.95f) // Slightly slower for natural pacing
            setPitch(1.0f)
            
            setOnUtteranceProgressListener(object : UtteranceProgressListener() {
                override fun onStart(utteranceId: String?) {}
                
                override fun onDone(utteranceId: String?) {
                    if (isLooping && currentMessage != null) {
                        // Add a small delay before repeating
                        handler.postDelayed({
                            speakInternal(currentMessage!!)
                        }, 1500)
                    }
                }
                
                override fun onError(utteranceId: String?) {}
            })
        }
    }
    
    fun speak(message: String, loop: Boolean = false) {
        if (!isInitialized) {
            initialize { success ->
                if (success) {
                    speak(message, loop)
                }
            }
            return
        }
        
        currentMessage = message
        isLooping = loop
        
        requestAudioFocus()
        setAudioRouting()
        speakInternal(message)
    }
    
    private fun speakInternal(message: String) {
        val utteranceId = UUID.randomUUID().toString()
        tts?.speak(message, TextToSpeech.QUEUE_FLUSH, null, utteranceId)
    }
    
    fun stop() {
        isLooping = false
        currentMessage = null
        tts?.stop()
        abandonAudioFocus()
    }
    
    fun shutdown() {
        stop()
        tts?.shutdown()
        tts = null
        isInitialized = false
    }
    
    fun setSpeakerphone(enabled: Boolean) {
        isSpeakerOn = enabled
        setAudioRouting()
    }
    
    fun isSpeakerphoneOn(): Boolean = isSpeakerOn
    
    private fun setAudioRouting() {
        if (isSpeakerOn) {
            audioManager.mode = AudioManager.MODE_IN_COMMUNICATION
            audioManager.isSpeakerphoneOn = true
        } else {
            audioManager.mode = AudioManager.MODE_IN_COMMUNICATION
            audioManager.isSpeakerphoneOn = false
        }
    }
    
    private fun requestAudioFocus() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val focusRequest = AudioFocusRequest.Builder(AudioManager.AUDIOFOCUS_GAIN_TRANSIENT)
                .setAudioAttributes(
                    AudioAttributes.Builder()
                        .setUsage(AudioAttributes.USAGE_VOICE_COMMUNICATION)
                        .setContentType(AudioAttributes.CONTENT_TYPE_SPEECH)
                        .build()
                )
                .build()
            audioFocusRequest = focusRequest
            audioManager.requestAudioFocus(focusRequest)
        } else {
            @Suppress("DEPRECATION")
            audioManager.requestAudioFocus(
                null,
                AudioManager.STREAM_VOICE_CALL,
                AudioManager.AUDIOFOCUS_GAIN_TRANSIENT
            )
        }
    }
    
    private fun abandonAudioFocus() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            audioFocusRequest?.let { audioManager.abandonAudioFocusRequest(it) }
        } else {
            @Suppress("DEPRECATION")
            audioManager.abandonAudioFocus(null)
        }
        audioManager.mode = AudioManager.MODE_NORMAL
        audioManager.isSpeakerphoneOn = false
    }
    
    fun previewVoice(message: String = "Hello! This is a preview of the voice message.") {
        speak(message, loop = false)
    }
    
    fun isReady(): Boolean = isInitialized
}
