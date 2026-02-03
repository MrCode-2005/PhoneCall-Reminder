package com.phonecall.reminder.receiver

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.os.Build
import com.phonecall.reminder.scheduler.AlarmSchedulerImpl
import com.phonecall.reminder.service.CallService

class AlarmReceiver : BroadcastReceiver() {
    
    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action != AlarmSchedulerImpl.ACTION_TRIGGER_CALL) return
        
        val reminderId = intent.getLongExtra(AlarmSchedulerImpl.EXTRA_REMINDER_ID, -1)
        if (reminderId == -1L) return
        
        val callerName = intent.getStringExtra(AlarmSchedulerImpl.EXTRA_CALLER_NAME) ?: "Unknown"
        val phoneNumber = intent.getStringExtra(AlarmSchedulerImpl.EXTRA_PHONE_NUMBER) ?: ""
        val voiceMessage = intent.getStringExtra(AlarmSchedulerImpl.EXTRA_VOICE_MESSAGE) ?: ""
        val ringtoneUri = intent.getStringExtra(AlarmSchedulerImpl.EXTRA_RINGTONE_URI)
        val repeatIndex = intent.getIntExtra(AlarmSchedulerImpl.EXTRA_REPEAT_INDEX, 0)
        val repeatCount = intent.getIntExtra(AlarmSchedulerImpl.EXTRA_REPEAT_COUNT, 1)
        val repeatInterval = intent.getIntExtra(AlarmSchedulerImpl.EXTRA_REPEAT_INTERVAL, 5)
        val snoozeMinutes = intent.getIntExtra(AlarmSchedulerImpl.EXTRA_SNOOZE_MINUTES, 5)
        
        val serviceIntent = Intent(context, CallService::class.java).apply {
            action = CallService.ACTION_START_CALL
            putExtra(CallService.EXTRA_REMINDER_ID, reminderId)
            putExtra(CallService.EXTRA_CALLER_NAME, callerName)
            putExtra(CallService.EXTRA_PHONE_NUMBER, phoneNumber)
            putExtra(CallService.EXTRA_VOICE_MESSAGE, voiceMessage)
            putExtra(CallService.EXTRA_RINGTONE_URI, ringtoneUri)
            putExtra(CallService.EXTRA_REPEAT_INDEX, repeatIndex)
            putExtra(CallService.EXTRA_REPEAT_COUNT, repeatCount)
            putExtra(CallService.EXTRA_REPEAT_INTERVAL, repeatInterval)
            putExtra(CallService.EXTRA_SNOOZE_MINUTES, snoozeMinutes)
        }
        
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            context.startForegroundService(serviceIntent)
        } else {
            context.startService(serviceIntent)
        }
    }
}
