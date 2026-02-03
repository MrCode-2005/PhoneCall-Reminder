package com.phonecall.reminder.data.repository

import com.phonecall.reminder.data.db.AlarmDao
import com.phonecall.reminder.data.model.Alarm
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AlarmRepository @Inject constructor(
    private val alarmDao: AlarmDao
) {
    
    fun getAllAlarms(): Flow<List<Alarm>> = alarmDao.getAllAlarms()
    
    fun getEnabledAlarms(): Flow<List<Alarm>> = alarmDao.getEnabledAlarms()
    
    suspend fun getAlarmById(id: Long): Alarm? = alarmDao.getAlarmById(id)
    
    suspend fun insertAlarm(alarm: Alarm): Long = alarmDao.insertAlarm(alarm)
    
    suspend fun updateAlarm(alarm: Alarm) = alarmDao.updateAlarm(alarm)
    
    suspend fun deleteAlarm(alarm: Alarm) = alarmDao.deleteAlarm(alarm)
    
    suspend fun deleteAlarmById(id: Long) = alarmDao.deleteAlarmById(id)
    
    suspend fun setAlarmEnabled(id: Long, enabled: Boolean) = alarmDao.setAlarmEnabled(id, enabled)
    
    suspend fun updateNextTriggerTime(id: Long, nextTime: Long) = 
        alarmDao.updateNextTriggerTime(id, nextTime)
    
    suspend fun getEnabledAlarmsSync(): List<Alarm> = alarmDao.getEnabledAlarmsSync()
}
