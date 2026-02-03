package com.phonecall.reminder.data.db

import androidx.room.*
import com.phonecall.reminder.data.model.DailyTask
import kotlinx.coroutines.flow.Flow

@Dao
interface DailyTaskDao {
    
    @Query("SELECT * FROM daily_tasks ORDER BY createdAt")
    fun getAllTasks(): Flow<List<DailyTask>>
    
    @Query("SELECT * FROM daily_tasks WHERE scheduledTimeHour IS NOT NULL ORDER BY scheduledTimeHour, scheduledTimeMinute")
    fun getScheduledTasks(): Flow<List<DailyTask>>
    
    @Query("SELECT * FROM daily_tasks WHERE id = :id")
    suspend fun getTaskById(id: Long): DailyTask?
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTask(task: DailyTask): Long
    
    @Update
    suspend fun updateTask(task: DailyTask)
    
    @Delete
    suspend fun deleteTask(task: DailyTask)
    
    @Query("DELETE FROM daily_tasks WHERE id = :id")
    suspend fun deleteTaskById(id: Long)
    
    @Query("UPDATE daily_tasks SET isCompleted = :completed, lastCompletedDate = :completedDate, streak = :streak WHERE id = :id")
    suspend fun setTaskCompleted(id: Long, completed: Boolean, completedDate: Long?, streak: Int)
    
    @Query("UPDATE daily_tasks SET isCompleted = 0 WHERE lastCompletedDate < :startOfDay")
    suspend fun resetDailyTasks(startOfDay: Long)
    
    @Query("SELECT * FROM daily_tasks WHERE triggerCall = 1 AND scheduledTimeHour IS NOT NULL")
    suspend fun getTasksWithCallTrigger(): List<DailyTask>
}
