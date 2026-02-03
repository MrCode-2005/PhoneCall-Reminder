package com.phonecall.reminder.receiver

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.phonecall.reminder.data.db.AppDatabase
import com.phonecall.reminder.scheduler.AlarmSchedulerImpl
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch

class BootReceiver : BroadcastReceiver() {
    
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)
    
    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action != Intent.ACTION_BOOT_COMPLETED && 
            intent.action != "android.intent.action.QUICKBOOT_POWERON") {
            return
        }
        
        val pendingResult = goAsync()
        
        scope.launch {
            try {
                rescheduleAllReminders(context)
            } finally {
                pendingResult.finish()
            }
        }
    }
    
    private suspend fun rescheduleAllReminders(context: Context) {
        val database = androidx.room.Room.databaseBuilder(
            context,
            AppDatabase::class.java,
            AppDatabase.DATABASE_NAME
        ).build()
        
        val scheduler = AlarmSchedulerImpl(context)
        
        // Reschedule all enabled reminders
        val enabledReminders = database.reminderDao().getEnabledRemindersSync()
        enabledReminders.forEach { reminder ->
            scheduler.scheduleReminder(reminder)
        }
        
        database.close()
    }
}
