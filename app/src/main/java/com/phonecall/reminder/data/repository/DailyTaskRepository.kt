package com.phonecall.reminder.data.repository

import com.phonecall.reminder.data.db.DailyTaskDao
import com.phonecall.reminder.data.model.DailyTask
import kotlinx.coroutines.flow.Flow
import java.util.Calendar
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class DailyTaskRepository @Inject constructor(
    private val dailyTaskDao: DailyTaskDao
) {
    
    fun getAllTasks(): Flow<List<DailyTask>> = dailyTaskDao.getAllTasks()
    
    fun getScheduledTasks(): Flow<List<DailyTask>> = dailyTaskDao.getScheduledTasks()
    
    suspend fun getTaskById(id: Long): DailyTask? = dailyTaskDao.getTaskById(id)
    
    suspend fun insertTask(task: DailyTask): Long = dailyTaskDao.insertTask(task)
    
    suspend fun updateTask(task: DailyTask) = dailyTaskDao.updateTask(task)
    
    suspend fun deleteTask(task: DailyTask) = dailyTaskDao.deleteTask(task)
    
    suspend fun deleteTaskById(id: Long) = dailyTaskDao.deleteTaskById(id)
    
    suspend fun setTaskCompleted(id: Long, completed: Boolean) {
        val task = dailyTaskDao.getTaskById(id) ?: return
        val now = System.currentTimeMillis()
        val newStreak = if (completed) {
            task.streak + 1
        } else {
            0
        }
        dailyTaskDao.setTaskCompleted(
            id = id,
            completed = completed,
            completedDate = if (completed) now else null,
            streak = newStreak
        )
    }
    
    suspend fun resetDailyTasks() {
        val calendar = Calendar.getInstance().apply {
            set(Calendar.HOUR_OF_DAY, 0)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }
        dailyTaskDao.resetDailyTasks(calendar.timeInMillis)
    }
    
    suspend fun getTasksWithCallTrigger(): List<DailyTask> = dailyTaskDao.getTasksWithCallTrigger()
}
