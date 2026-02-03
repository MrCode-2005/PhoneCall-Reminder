package com.phonecall.reminder.data.db

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.phonecall.reminder.data.model.*

@Database(
    entities = [
        Reminder::class,
        Alarm::class,
        Event::class,
        DailyTask::class
    ],
    version = 1,
    exportSchema = false
)
@TypeConverters(Converters::class)
abstract class AppDatabase : RoomDatabase() {
    
    abstract fun reminderDao(): ReminderDao
    abstract fun alarmDao(): AlarmDao
    abstract fun eventDao(): EventDao
    abstract fun dailyTaskDao(): DailyTaskDao
    
    companion object {
        const val DATABASE_NAME = "phone_call_reminder_db"
    }
}
