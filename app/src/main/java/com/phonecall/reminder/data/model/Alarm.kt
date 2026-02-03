package com.phonecall.reminder.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "alarms")
data class Alarm(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val label: String = "",
    val hour: Int,
    val minute: Int,
    val daysOfWeek: List<Int> = emptyList(), // Empty = one-time, otherwise 1-7 for days
    val isEnabled: Boolean = true,
    val ringtoneUri: String? = null,
    val vibrate: Boolean = true,
    val snoozeMinutes: Int = 5,
    val snoozeCount: Int = 3,
    val nextTriggerTime: Long = 0L,
    val createdAt: Long = System.currentTimeMillis()
)
