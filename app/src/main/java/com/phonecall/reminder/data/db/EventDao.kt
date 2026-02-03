package com.phonecall.reminder.data.db

import androidx.room.*
import com.phonecall.reminder.data.model.Event
import kotlinx.coroutines.flow.Flow

@Dao
interface EventDao {
    
    @Query("SELECT * FROM events ORDER BY dateTimeMillis")
    fun getAllEvents(): Flow<List<Event>>
    
    @Query("SELECT * FROM events WHERE isCompleted = 0 AND dateTimeMillis > :currentTime ORDER BY dateTimeMillis")
    fun getUpcomingEvents(currentTime: Long = System.currentTimeMillis()): Flow<List<Event>>
    
    @Query("SELECT * FROM events WHERE dateTimeMillis BETWEEN :startOfDay AND :endOfDay ORDER BY dateTimeMillis")
    fun getEventsForDay(startOfDay: Long, endOfDay: Long): Flow<List<Event>>
    
    @Query("SELECT * FROM events WHERE id = :id")
    suspend fun getEventById(id: Long): Event?
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertEvent(event: Event): Long
    
    @Update
    suspend fun updateEvent(event: Event)
    
    @Delete
    suspend fun deleteEvent(event: Event)
    
    @Query("DELETE FROM events WHERE id = :id")
    suspend fun deleteEventById(id: Long)
    
    @Query("UPDATE events SET isCompleted = :completed WHERE id = :id")
    suspend fun setEventCompleted(id: Long, completed: Boolean)
    
    @Query("SELECT * FROM events WHERE triggerCall = 1 AND isCompleted = 0 AND dateTimeMillis > :currentTime")
    suspend fun getEventsWithCallTrigger(currentTime: Long = System.currentTimeMillis()): List<Event>
}
