package com.phonecall.reminder.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "daily_tasks")
data class DailyTask(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val title: String,
    val description: String = "",
    val scheduledTimeHour: Int? = null, // Optional scheduled time
    val scheduledTimeMinute: Int? = null,
    val triggerCall: Boolean = false, // Whether to trigger a fake call
    val callerName: String? = null,
    val phoneNumber: String? = null,
    val voiceMessage: String? = null,
    val isCompleted: Boolean = false,
    val lastCompletedDate: Long? = null, // Reset daily
    val streak: Int = 0,
    val createdAt: Long = System.currentTimeMillis()
)
