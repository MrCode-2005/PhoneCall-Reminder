package com.phonecall.reminder.service

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.IBinder
import android.os.PowerManager
import androidx.core.app.NotificationCompat
import com.phonecall.reminder.PhoneCallReminderApp
import com.phonecall.reminder.R
import com.phonecall.reminder.audio.RingtoneManager
import com.phonecall.reminder.audio.TextToSpeechManager
import com.phonecall.reminder.audio.VibrationManager
import com.phonecall.reminder.receiver.CallActionReceiver
import com.phonecall.reminder.scheduler.AlarmScheduler
import com.phonecall.reminder.ui.call.IncomingCallActivity
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class CallService : Service() {
    
    @Inject lateinit var ringtoneManager: RingtoneManager
    @Inject lateinit var vibrationManager: VibrationManager
    @Inject lateinit var ttsManager: TextToSpeechManager
    @Inject lateinit var alarmScheduler: AlarmScheduler
    
    private var wakeLock: PowerManager.WakeLock? = null
    private var isCallActive = false
    private var isCallAnswered = false
    
    private var currentReminderId: Long = -1
    private var currentCallerName: String = ""
    private var currentPhoneNumber: String = ""
    private var currentVoiceMessage: String = ""
    private var currentRingtoneUri: String? = null
    private var currentRepeatIndex: Int = 0
    private var currentRepeatCount: Int = 1
    private var currentRepeatInterval: Int = 5
    private var currentSnoozeMinutes: Int = 5
    
    companion object {
        const val ACTION_START_CALL = "com.phonecall.reminder.START_CALL"
        const val ACTION_ANSWER_CALL = "com.phonecall.reminder.ANSWER_CALL"
        const val ACTION_END_CALL = "com.phonecall.reminder.END_CALL"
        const val ACTION_SNOOZE = "com.phonecall.reminder.SNOOZE"
        const val ACTION_TOGGLE_SPEAKER = "com.phonecall.reminder.TOGGLE_SPEAKER"
        
        const val EXTRA_REMINDER_ID = "reminder_id"
        const val EXTRA_CALLER_NAME = "caller_name"
        const val EXTRA_PHONE_NUMBER = "phone_number"
        const val EXTRA_VOICE_MESSAGE = "voice_message"
        const val EXTRA_RINGTONE_URI = "ringtone_uri"
        const val EXTRA_REPEAT_INDEX = "repeat_index"
        const val EXTRA_REPEAT_COUNT = "repeat_count"
        const val EXTRA_REPEAT_INTERVAL = "repeat_interval"
        const val EXTRA_SNOOZE_MINUTES = "snooze_minutes"
        
        private const val NOTIFICATION_ID = 1001
        private const val WAKELOCK_TAG = "PhoneCallReminder:CallWakeLock"
    }
    
    override fun onCreate() {
        super.onCreate()
        ttsManager.initialize()
    }
    
    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        when (intent?.action) {
            ACTION_START_CALL -> {
                currentReminderId = intent.getLongExtra(EXTRA_REMINDER_ID, -1)
                currentCallerName = intent.getStringExtra(EXTRA_CALLER_NAME) ?: "Unknown"
                currentPhoneNumber = intent.getStringExtra(EXTRA_PHONE_NUMBER) ?: ""
                currentVoiceMessage = intent.getStringExtra(EXTRA_VOICE_MESSAGE) ?: ""
                currentRingtoneUri = intent.getStringExtra(EXTRA_RINGTONE_URI)
                currentRepeatIndex = intent.getIntExtra(EXTRA_REPEAT_INDEX, 0)
                currentRepeatCount = intent.getIntExtra(EXTRA_REPEAT_COUNT, 1)
                currentRepeatInterval = intent.getIntExtra(EXTRA_REPEAT_INTERVAL, 5)
                currentSnoozeMinutes = intent.getIntExtra(EXTRA_SNOOZE_MINUTES, 5)
                
                startCall()
            }
            ACTION_ANSWER_CALL -> answerCall()
            ACTION_END_CALL -> endCall()
            ACTION_SNOOZE -> {
                val snoozeMinutes = intent.getIntExtra(EXTRA_SNOOZE_MINUTES, currentSnoozeMinutes)
                snoozeCall(snoozeMinutes)
            }
            ACTION_TOGGLE_SPEAKER -> toggleSpeaker()
        }
        
        return START_STICKY
    }
    
    private fun startCall() {
        isCallActive = true
        isCallAnswered = false
        
        acquireWakeLock()
        startForeground(NOTIFICATION_ID, createIncomingCallNotification())
        
        // Start ringtone and vibration
        ringtoneManager.playRingtone(currentRingtoneUri, loop = true)
        vibrationManager.startCallVibration()
        
        // Launch incoming call activity
        launchIncomingCallActivity()
    }
    
    private fun answerCall() {
        if (!isCallActive || isCallAnswered) return
        
        isCallAnswered = true
        
        // Stop ringtone and vibration
        ringtoneManager.stopRingtone()
        vibrationManager.stopVibration()
        
        // Update notification
        val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        notificationManager.notify(NOTIFICATION_ID, createActiveCallNotification())
        
        // Start TTS with message in earpiece mode
        ttsManager.setSpeakerphone(false)
        ttsManager.speak(currentVoiceMessage, loop = true)
        
        // Send broadcast to update UI
        sendBroadcast(Intent("com.phonecall.reminder.CALL_ANSWERED"))
    }
    
    private fun endCall() {
        isCallActive = false
        isCallAnswered = false
        
        // Stop everything
        ringtoneManager.stopRingtone()
        vibrationManager.stopVibration()
        ttsManager.stop()
        
        releaseWakeLock()
        
        // Send broadcast to close call activity
        sendBroadcast(Intent("com.phonecall.reminder.CALL_ENDED"))
        
        stopForeground(STOP_FOREGROUND_REMOVE)
        stopSelf()
    }
    
    private fun snoozeCall(minutes: Int) {
        // Create a temporary reminder for snooze
        val snoozeReminder = com.phonecall.reminder.data.model.Reminder(
            id = currentReminderId,
            callerName = currentCallerName,
            phoneNumber = currentPhoneNumber,
            voiceMessage = currentVoiceMessage,
            scheduledTimeHour = 0,
            scheduledTimeMinute = 0,
            recurrenceType = com.phonecall.reminder.data.model.RecurrenceType.ONCE,
            ringtoneUri = currentRingtoneUri,
            snoozeMinutes = minutes,
            repeatCount = currentRepeatCount,
            repeatIntervalMinutes = currentRepeatInterval
        )
        
        // Schedule the snooze alarm
        alarmScheduler.scheduleSnooze(snoozeReminder, minutes)
        
        endCall()
    }
    
    private fun toggleSpeaker() {
        if (isCallAnswered) {
            val currentState = ttsManager.isSpeakerphoneOn()
            ttsManager.setSpeakerphone(!currentState)
            sendBroadcast(Intent("com.phonecall.reminder.SPEAKER_TOGGLED").apply {
                putExtra("speaker_on", !currentState)
            })
        }
    }
    
    private fun launchIncomingCallActivity() {
        val intent = Intent(this, IncomingCallActivity::class.java).apply {
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
            addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP)
            putExtra(EXTRA_CALLER_NAME, currentCallerName)
            putExtra(EXTRA_PHONE_NUMBER, currentPhoneNumber)
            putExtra(EXTRA_VOICE_MESSAGE, currentVoiceMessage)
            putExtra(EXTRA_SNOOZE_MINUTES, currentSnoozeMinutes)
        }
        startActivity(intent)
    }
    
    private fun createIncomingCallNotification(): Notification {
        createNotificationChannel()
        
        val fullScreenIntent = Intent(this, IncomingCallActivity::class.java).apply {
            putExtra(EXTRA_CALLER_NAME, currentCallerName)
            putExtra(EXTRA_PHONE_NUMBER, currentPhoneNumber)
            putExtra(EXTRA_VOICE_MESSAGE, currentVoiceMessage)
        }
        val fullScreenPendingIntent = PendingIntent.getActivity(
            this, 0, fullScreenIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        
        val answerIntent = Intent(this, CallActionReceiver::class.java).apply {
            action = CallActionReceiver.ACTION_ANSWER
        }
        val answerPendingIntent = PendingIntent.getBroadcast(
            this, 1, answerIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        
        val declineIntent = Intent(this, CallActionReceiver::class.java).apply {
            action = CallActionReceiver.ACTION_DECLINE
        }
        val declinePendingIntent = PendingIntent.getBroadcast(
            this, 2, declineIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        
        return NotificationCompat.Builder(this, PhoneCallReminderApp.NOTIFICATION_CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_phone)
            .setContentTitle(currentCallerName)
            .setContentText(getString(R.string.incoming_call))
            .setPriority(NotificationCompat.PRIORITY_MAX)
            .setCategory(NotificationCompat.CATEGORY_CALL)
            .setFullScreenIntent(fullScreenPendingIntent, true)
            .addAction(R.drawable.ic_call_answer, getString(R.string.answer), answerPendingIntent)
            .addAction(R.drawable.ic_call_end, getString(R.string.decline), declinePendingIntent)
            .setOngoing(true)
            .setAutoCancel(false)
            .build()
    }
    
    private fun createActiveCallNotification(): Notification {
        val endCallIntent = Intent(this, CallActionReceiver::class.java).apply {
            action = CallActionReceiver.ACTION_END_CALL
        }
        val endCallPendingIntent = PendingIntent.getBroadcast(
            this, 3, endCallIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        
        return NotificationCompat.Builder(this, PhoneCallReminderApp.NOTIFICATION_CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_phone)
            .setContentTitle(currentCallerName)
            .setContentText("Ongoing call")
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setCategory(NotificationCompat.CATEGORY_CALL)
            .addAction(R.drawable.ic_call_end, getString(R.string.end_call), endCallPendingIntent)
            .setOngoing(true)
            .setAutoCancel(false)
            .build()
    }
    
    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                PhoneCallReminderApp.NOTIFICATION_CHANNEL_ID,
                PhoneCallReminderApp.NOTIFICATION_CHANNEL_NAME,
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = getString(R.string.notification_channel_description)
                setShowBadge(true)
            }
            
            val notificationManager = getSystemService(NotificationManager::class.java)
            notificationManager.createNotificationChannel(channel)
        }
    }
    
    private fun acquireWakeLock() {
        if (wakeLock == null) {
            val powerManager = getSystemService(Context.POWER_SERVICE) as PowerManager
            wakeLock = powerManager.newWakeLock(
                PowerManager.PARTIAL_WAKE_LOCK or PowerManager.ACQUIRE_CAUSES_WAKEUP,
                WAKELOCK_TAG
            )
        }
        wakeLock?.acquire(10 * 60 * 1000L) // 10 minutes max
    }
    
    private fun releaseWakeLock() {
        wakeLock?.let {
            if (it.isHeld) {
                it.release()
            }
        }
        wakeLock = null
    }
    
    override fun onBind(intent: Intent?): IBinder? = null
    
    override fun onDestroy() {
        super.onDestroy()
        ringtoneManager.stopRingtone()
        vibrationManager.stopVibration()
        ttsManager.shutdown()
        releaseWakeLock()
    }
}
