package com.phonecall.reminder.ui.call

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.Build
import android.os.Bundle
import android.view.WindowManager
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsControllerCompat
import com.phonecall.reminder.service.CallService
import com.phonecall.reminder.ui.theme.PhoneCallReminderTheme
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class IncomingCallActivity : ComponentActivity() {
    
    private var callerName by mutableStateOf("")
    private var phoneNumber by mutableStateOf("")
    private var voiceMessage by mutableStateOf("")
    private var snoozeMinutes by mutableStateOf(5)
    private var isCallAnswered by mutableStateOf(false)
    private var isSpeakerOn by mutableStateOf(false)
    
    private val callEndedReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            when (intent?.action) {
                "com.phonecall.reminder.CALL_ENDED" -> finish()
                "com.phonecall.reminder.CALL_ANSWERED" -> isCallAnswered = true
                "com.phonecall.reminder.SPEAKER_TOGGLED" -> {
                    isSpeakerOn = intent.getBooleanExtra("speaker_on", false)
                }
            }
        }
    }
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        setupFullScreen()
        extractIntentData()
        registerReceivers()
        
        setContent {
            PhoneCallReminderTheme(darkTheme = false) {
                if (isCallAnswered) {
                    ActiveCallScreen(
                        callerName = callerName,
                        phoneNumber = phoneNumber,
                        isSpeakerOn = isSpeakerOn,
                        onEndCall = { endCall() },
                        onToggleSpeaker = { toggleSpeaker() }
                    )
                } else {
                    IncomingCallScreen(
                        callerName = callerName,
                        phoneNumber = phoneNumber,
                        onAnswer = { answerCall() },
                        onDecline = { declineCall() }
                    )
                }
            }
        }
    }
    
    private fun setupFullScreen() {
        // Show on lock screen
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O_MR1) {
            setShowWhenLocked(true)
            setTurnScreenOn(true)
        } else {
            @Suppress("DEPRECATION")
            window.addFlags(
                WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED or
                WindowManager.LayoutParams.FLAG_TURN_SCREEN_ON
            )
        }
        
        window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
        
        // Full screen immersive
        WindowCompat.setDecorFitsSystemWindows(window, false)
        WindowInsetsControllerCompat(window, window.decorView).let { controller ->
            controller.hide(WindowInsetsCompat.Type.systemBars())
            controller.systemBarsBehavior = WindowInsetsControllerCompat.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE
        }
    }
    
    private fun extractIntentData() {
        callerName = intent.getStringExtra(CallService.EXTRA_CALLER_NAME) ?: "Unknown"
        phoneNumber = intent.getStringExtra(CallService.EXTRA_PHONE_NUMBER) ?: ""
        voiceMessage = intent.getStringExtra(CallService.EXTRA_VOICE_MESSAGE) ?: ""
        snoozeMinutes = intent.getIntExtra(CallService.EXTRA_SNOOZE_MINUTES, 5)
    }
    
    private fun registerReceivers() {
        val filter = IntentFilter().apply {
            addAction("com.phonecall.reminder.CALL_ENDED")
            addAction("com.phonecall.reminder.CALL_ANSWERED")
            addAction("com.phonecall.reminder.SPEAKER_TOGGLED")
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            registerReceiver(callEndedReceiver, filter, Context.RECEIVER_NOT_EXPORTED)
        } else {
            registerReceiver(callEndedReceiver, filter)
        }
    }
    
    private fun answerCall() {
        val intent = Intent(this, CallService::class.java).apply {
            action = CallService.ACTION_ANSWER_CALL
        }
        startService(intent)
    }
    
    private fun declineCall() {
        val intent = Intent(this, CallService::class.java).apply {
            action = CallService.ACTION_END_CALL
        }
        startService(intent)
        finish()
    }
    
    private fun endCall() {
        val intent = Intent(this, CallService::class.java).apply {
            action = CallService.ACTION_END_CALL
        }
        startService(intent)
        finish()
    }
    
    private fun toggleSpeaker() {
        val intent = Intent(this, CallService::class.java).apply {
            action = CallService.ACTION_TOGGLE_SPEAKER
        }
        startService(intent)
    }
    
    override fun onDestroy() {
        super.onDestroy()
        try {
            unregisterReceiver(callEndedReceiver)
        } catch (e: Exception) {
            // Ignore if not registered
        }
    }
    
    @Deprecated("Deprecated in Java")
    override fun onBackPressed() {
        // Prevent back press during call
    }
}
