package com.phonecall.reminder

import android.app.Application
import android.app.NotificationChannel
import android.app.NotificationManager
import android.os.Build
import dagger.hilt.android.HiltAndroidApp

@HiltAndroidApp
class PhoneCallReminderApp : Application() {
    
    companion object {
        const val NOTIFICATION_CHANNEL_ID = "call_reminder_channel"
        const val NOTIFICATION_CHANNEL_NAME = "Call Reminders"
    }
    
    override fun onCreate() {
        super.onCreate()
        createNotificationChannel()
    }
    
    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                NOTIFICATION_CHANNEL_ID,
                NOTIFICATION_CHANNEL_NAME,
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = getString(R.string.notification_channel_description)
                setShowBadge(true)
                enableVibration(true)
                vibrationPattern = longArrayOf(0, 1000, 500, 1000)
            }
            
            val notificationManager = getSystemService(NotificationManager::class.java)
            notificationManager.createNotificationChannel(channel)
        }
    }
}
