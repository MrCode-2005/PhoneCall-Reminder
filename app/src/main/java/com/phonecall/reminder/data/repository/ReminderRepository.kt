package com.phonecall.reminder.data.repository

import com.phonecall.reminder.data.db.ReminderDao
import com.phonecall.reminder.data.model.Reminder
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ReminderRepository @Inject constructor(
    private val reminderDao: ReminderDao
) {
    
    fun getAllReminders(): Flow<List<Reminder>> = reminderDao.getAllReminders()
    
    fun getEnabledReminders(): Flow<List<Reminder>> = reminderDao.getEnabledReminders()
    
    suspend fun getReminderById(id: Long): Reminder? = reminderDao.getReminderById(id)
    
    suspend fun insertReminder(reminder: Reminder): Long = reminderDao.insertReminder(reminder)
    
    suspend fun updateReminder(reminder: Reminder) = reminderDao.updateReminder(reminder)
    
    suspend fun deleteReminder(reminder: Reminder) = reminderDao.deleteReminder(reminder)
    
    suspend fun deleteReminderById(id: Long) = reminderDao.deleteReminderById(id)
    
    suspend fun setReminderEnabled(id: Long, enabled: Boolean) = 
        reminderDao.setReminderEnabled(id, enabled)
    
    suspend fun updateNextTriggerTime(id: Long, nextTime: Long, repeatIndex: Int = 0) = 
        reminderDao.updateNextTriggerTime(id, nextTime, repeatIndex)
    
    suspend fun getEnabledRemindersSync(): List<Reminder> = reminderDao.getEnabledRemindersSync()
}
