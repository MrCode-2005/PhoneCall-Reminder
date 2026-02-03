package com.phonecall.reminder.data.repository

import com.phonecall.reminder.data.db.EventDao
import com.phonecall.reminder.data.model.Event
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class EventRepository @Inject constructor(
    private val eventDao: EventDao
) {
    
    fun getAllEvents(): Flow<List<Event>> = eventDao.getAllEvents()
    
    fun getUpcomingEvents(): Flow<List<Event>> = eventDao.getUpcomingEvents()
    
    fun getEventsForDay(startOfDay: Long, endOfDay: Long): Flow<List<Event>> = 
        eventDao.getEventsForDay(startOfDay, endOfDay)
    
    suspend fun getEventById(id: Long): Event? = eventDao.getEventById(id)
    
    suspend fun insertEvent(event: Event): Long = eventDao.insertEvent(event)
    
    suspend fun updateEvent(event: Event) = eventDao.updateEvent(event)
    
    suspend fun deleteEvent(event: Event) = eventDao.deleteEvent(event)
    
    suspend fun deleteEventById(id: Long) = eventDao.deleteEventById(id)
    
    suspend fun setEventCompleted(id: Long, completed: Boolean) = 
        eventDao.setEventCompleted(id, completed)
    
    suspend fun getEventsWithCallTrigger(): List<Event> = eventDao.getEventsWithCallTrigger()
}
