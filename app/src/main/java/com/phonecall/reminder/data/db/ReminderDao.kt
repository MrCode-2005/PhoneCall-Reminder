package com.phonecall.reminder.data.db

import androidx.room.*
import com.phonecall.reminder.data.model.Reminder
import kotlinx.coroutines.flow.Flow

@Dao
interface ReminderDao {
    
    @Query("SELECT * FROM reminders ORDER BY scheduledTimeHour, scheduledTimeMinute")
    fun getAllReminders(): Flow<List<Reminder>>
    
    @Query("SELECT * FROM reminders WHERE isEnabled = 1 ORDER BY nextTriggerTime")
    fun getEnabledReminders(): Flow<List<Reminder>>
    
    @Query("SELECT * FROM reminders WHERE id = :id")
    suspend fun getReminderById(id: Long): Reminder?
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertReminder(reminder: Reminder): Long
    
    @Update
    suspend fun updateReminder(reminder: Reminder)
    
    @Delete
    suspend fun deleteReminder(reminder: Reminder)
    
    @Query("DELETE FROM reminders WHERE id = :id")
    suspend fun deleteReminderById(id: Long)
    
    @Query("UPDATE reminders SET isEnabled = :enabled, updatedAt = :updatedAt WHERE id = :id")
    suspend fun setReminderEnabled(id: Long, enabled: Boolean, updatedAt: Long = System.currentTimeMillis())
    
    @Query("UPDATE reminders SET nextTriggerTime = :nextTime, currentRepeatIndex = :repeatIndex, updatedAt = :updatedAt WHERE id = :id")
    suspend fun updateNextTriggerTime(id: Long, nextTime: Long, repeatIndex: Int = 0, updatedAt: Long = System.currentTimeMillis())
    
    @Query("SELECT * FROM reminders WHERE isEnabled = 1")
    suspend fun getEnabledRemindersSync(): List<Reminder>
}
