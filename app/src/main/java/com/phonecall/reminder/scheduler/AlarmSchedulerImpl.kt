package com.phonecall.reminder.scheduler

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import android.provider.Settings
import com.phonecall.reminder.data.model.RecurrenceType
import com.phonecall.reminder.data.model.Reminder
import com.phonecall.reminder.receiver.AlarmReceiver
import java.util.Calendar
import javax.inject.Inject

class AlarmSchedulerImpl @Inject constructor(
    private val context: Context
) : AlarmScheduler {
    
    private val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
    
    companion object {
        const val EXTRA_REMINDER_ID = "reminder_id"
        const val EXTRA_CALLER_NAME = "caller_name"
        const val EXTRA_PHONE_NUMBER = "phone_number"
        const val EXTRA_VOICE_MESSAGE = "voice_message"
        const val EXTRA_RINGTONE_URI = "ringtone_uri"
        const val EXTRA_REPEAT_INDEX = "repeat_index"
        const val EXTRA_REPEAT_COUNT = "repeat_count"
        const val EXTRA_REPEAT_INTERVAL = "repeat_interval"
        const val EXTRA_SNOOZE_MINUTES = "snooze_minutes"
        const val ACTION_TRIGGER_CALL = "com.phonecall.reminder.TRIGGER_CALL"
    }
    
    override fun scheduleReminder(reminder: Reminder) {
        if (!reminder.isEnabled) return
        
        val triggerTime = calculateNextTriggerTime(reminder)
        if (triggerTime <= System.currentTimeMillis()) return
        
        val intent = createIntent(reminder, 0)
        val pendingIntent = createPendingIntent(reminder.id.toInt(), intent)
        
        scheduleExactAlarm(triggerTime, pendingIntent)
    }
    
    override fun cancelReminder(reminder: Reminder) {
        val intent = Intent(context, AlarmReceiver::class.java).apply {
            action = ACTION_TRIGGER_CALL
        }
        val pendingIntent = PendingIntent.getBroadcast(
            context,
            reminder.id.toInt(),
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        alarmManager.cancel(pendingIntent)
    }
    
    override fun scheduleSnooze(reminder: Reminder, snoozeMinutes: Int) {
        val triggerTime = System.currentTimeMillis() + (snoozeMinutes * 60 * 1000)
        val intent = createIntent(reminder, 0)
        val pendingIntent = createPendingIntent(reminder.id.toInt() + 10000, intent)
        
        scheduleExactAlarm(triggerTime, pendingIntent)
    }
    
    override fun scheduleRepeat(reminder: Reminder, intervalMinutes: Int, repeatIndex: Int) {
        if (repeatIndex >= reminder.repeatCount) return
        
        val triggerTime = System.currentTimeMillis() + (intervalMinutes * 60 * 1000)
        val intent = createIntent(reminder, repeatIndex)
        val pendingIntent = createPendingIntent(reminder.id.toInt() + 20000 + repeatIndex, intent)
        
        scheduleExactAlarm(triggerTime, pendingIntent)
    }
    
    override fun rescheduleAllReminders() {
        // This will be called from BootReceiver
        // Implementation will be handled in the service layer
    }
    
    private fun createIntent(reminder: Reminder, repeatIndex: Int): Intent {
        return Intent(context, AlarmReceiver::class.java).apply {
            action = ACTION_TRIGGER_CALL
            putExtra(EXTRA_REMINDER_ID, reminder.id)
            putExtra(EXTRA_CALLER_NAME, reminder.callerName)
            putExtra(EXTRA_PHONE_NUMBER, reminder.phoneNumber)
            putExtra(EXTRA_VOICE_MESSAGE, reminder.voiceMessage)
            putExtra(EXTRA_RINGTONE_URI, reminder.ringtoneUri)
            putExtra(EXTRA_REPEAT_INDEX, repeatIndex)
            putExtra(EXTRA_REPEAT_COUNT, reminder.repeatCount)
            putExtra(EXTRA_REPEAT_INTERVAL, reminder.repeatIntervalMinutes)
            putExtra(EXTRA_SNOOZE_MINUTES, reminder.snoozeMinutes)
        }
    }
    
    private fun createPendingIntent(requestCode: Int, intent: Intent): PendingIntent {
        return PendingIntent.getBroadcast(
            context,
            requestCode,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
    }
    
    private fun scheduleExactAlarm(triggerTime: Long, pendingIntent: PendingIntent) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            if (alarmManager.canScheduleExactAlarms()) {
                alarmManager.setAlarmClock(
                    AlarmManager.AlarmClockInfo(triggerTime, pendingIntent),
                    pendingIntent
                )
            }
        } else {
            alarmManager.setAlarmClock(
                AlarmManager.AlarmClockInfo(triggerTime, pendingIntent),
                pendingIntent
            )
        }
    }
    
    fun canScheduleExactAlarms(): Boolean {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            alarmManager.canScheduleExactAlarms()
        } else {
            true
        }
    }
    
    fun getExactAlarmSettingsIntent(): Intent? {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            Intent(Settings.ACTION_REQUEST_SCHEDULE_EXACT_ALARM)
        } else {
            null
        }
    }
    
    private fun calculateNextTriggerTime(reminder: Reminder): Long {
        val now = Calendar.getInstance()
        val target = Calendar.getInstance().apply {
            set(Calendar.HOUR_OF_DAY, reminder.scheduledTimeHour)
            set(Calendar.MINUTE, reminder.scheduledTimeMinute)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }
        
        when (reminder.recurrenceType) {
            RecurrenceType.ONCE -> {
                if (target.timeInMillis <= now.timeInMillis) {
                    target.add(Calendar.DAY_OF_YEAR, 1)
                }
            }
            RecurrenceType.DAILY -> {
                if (target.timeInMillis <= now.timeInMillis) {
                    target.add(Calendar.DAY_OF_YEAR, 1)
                }
            }
            RecurrenceType.WEEKLY -> {
                if (reminder.daysOfWeek.isEmpty()) return 0L
                
                var daysToAdd = 0
                val currentDayOfWeek = now.get(Calendar.DAY_OF_WEEK)
                
                // Find the next enabled day
                for (i in 0..7) {
                    val checkDay = ((currentDayOfWeek - 1 + i) % 7) + 1
                    if (checkDay in reminder.daysOfWeek) {
                        if (i == 0 && target.timeInMillis > now.timeInMillis) {
                            daysToAdd = 0
                            break
                        } else if (i > 0) {
                            daysToAdd = i
                            break
                        }
                    }
                }
                target.add(Calendar.DAY_OF_YEAR, daysToAdd)
            }
            RecurrenceType.MONTHLY -> {
                target.set(Calendar.DAY_OF_MONTH, reminder.dayOfMonth)
                if (target.timeInMillis <= now.timeInMillis) {
                    target.add(Calendar.MONTH, 1)
                }
            }
        }
        
        return target.timeInMillis
    }
}
