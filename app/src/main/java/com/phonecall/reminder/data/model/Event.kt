package com.phonecall.reminder.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "events")
data class Event(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val title: String,
    val description: String = "",
    val dateTimeMillis: Long, // Scheduled date and time
    val triggerCall: Boolean = false, // Whether to trigger a fake call
    val callerName: String? = null,
    val phoneNumber: String? = null,
    val voiceMessage: String? = null,
    val ringtoneUri: String? = null,
    val isCompleted: Boolean = false,
    val createdAt: Long = System.currentTimeMillis()
)
