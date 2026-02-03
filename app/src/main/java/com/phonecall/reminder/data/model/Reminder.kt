package com.phonecall.reminder.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.room.TypeConverter
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken

enum class RecurrenceType {
    ONCE,
    DAILY,
    WEEKLY,
    MONTHLY
}

@Entity(tableName = "reminders")
data class Reminder(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val callerName: String,
    val phoneNumber: String,
    val voiceMessage: String,
    val scheduledTimeHour: Int,
    val scheduledTimeMinute: Int,
    val recurrenceType: RecurrenceType = RecurrenceType.ONCE,
    val daysOfWeek: List<Int> = emptyList(), // 1=Sunday, 7=Saturday
    val dayOfMonth: Int = 1,
    val nextTriggerTime: Long = 0L,
    val isEnabled: Boolean = true,
    val ringtoneUri: String? = null,
    val snoozeMinutes: Int = 5,
    val repeatCount: Int = 1,
    val repeatIntervalMinutes: Int = 5,
    val currentRepeatIndex: Int = 0,
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis()
)

class Converters {
    private val gson = Gson()
    
    @TypeConverter
    fun fromIntList(value: List<Int>): String {
        return gson.toJson(value)
    }
    
    @TypeConverter
    fun toIntList(value: String): List<Int> {
        val type = object : TypeToken<List<Int>>() {}.type
        return gson.fromJson(value, type) ?: emptyList()
    }
    
    @TypeConverter
    fun fromRecurrenceType(value: RecurrenceType): String {
        return value.name
    }
    
    @TypeConverter
    fun toRecurrenceType(value: String): RecurrenceType {
        return RecurrenceType.valueOf(value)
    }
}
