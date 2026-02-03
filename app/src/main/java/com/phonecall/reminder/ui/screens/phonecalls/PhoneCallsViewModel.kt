package com.phonecall.reminder.ui.screens.phonecalls

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.phonecall.reminder.data.model.RecurrenceType
import com.phonecall.reminder.data.model.Reminder
import com.phonecall.reminder.data.repository.ReminderRepository
import com.phonecall.reminder.scheduler.AlarmScheduler
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

data class PhoneCallsUiState(
    val reminders: List<Reminder> = emptyList(),
    val isLoading: Boolean = true
)

@HiltViewModel
class PhoneCallsViewModel @Inject constructor(
    private val reminderRepository: ReminderRepository,
    private val alarmScheduler: AlarmScheduler
) : ViewModel() {
    
    private val _uiState = MutableStateFlow(PhoneCallsUiState())
    val uiState: StateFlow<PhoneCallsUiState> = _uiState.asStateFlow()
    
    init {
        loadReminders()
    }
    
    private fun loadReminders() {
        viewModelScope.launch {
            reminderRepository.getAllReminders()
                .collect { reminders ->
                    _uiState.update {
                        it.copy(reminders = reminders, isLoading = false)
                    }
                }
        }
    }
    
    fun toggleReminderEnabled(reminder: Reminder) {
        viewModelScope.launch {
            val newEnabled = !reminder.isEnabled
            reminderRepository.setReminderEnabled(reminder.id, newEnabled)
            
            if (newEnabled) {
                alarmScheduler.scheduleReminder(reminder.copy(isEnabled = true))
            } else {
                alarmScheduler.cancelReminder(reminder)
            }
        }
    }
    
    fun deleteReminder(reminder: Reminder) {
        viewModelScope.launch {
            alarmScheduler.cancelReminder(reminder)
            reminderRepository.deleteReminder(reminder)
        }
    }
    
    fun saveReminder(
        id: Long = 0,
        callerName: String,
        phoneNumber: String,
        voiceMessage: String,
        hour: Int,
        minute: Int,
        recurrenceType: RecurrenceType,
        daysOfWeek: List<Int>,
        dayOfMonth: Int,
        ringtoneUri: String?,
        snoozeMinutes: Int,
        repeatCount: Int,
        repeatIntervalMinutes: Int
    ) {
        viewModelScope.launch {
            val reminder = Reminder(
                id = id,
                callerName = callerName,
                phoneNumber = phoneNumber,
                voiceMessage = voiceMessage,
                scheduledTimeHour = hour,
                scheduledTimeMinute = minute,
                recurrenceType = recurrenceType,
                daysOfWeek = daysOfWeek,
                dayOfMonth = dayOfMonth,
                ringtoneUri = ringtoneUri,
                snoozeMinutes = snoozeMinutes,
                repeatCount = repeatCount,
                repeatIntervalMinutes = repeatIntervalMinutes,
                isEnabled = true
            )
            
            val savedId = reminderRepository.insertReminder(reminder)
            val savedReminder = reminder.copy(id = savedId)
            alarmScheduler.scheduleReminder(savedReminder)
        }
    }
    
    suspend fun getReminderById(id: Long): Reminder? {
        return reminderRepository.getReminderById(id)
    }
    
    fun triggerTestCall(reminder: Reminder, context: android.content.Context) {
        val serviceIntent = android.content.Intent(context, com.phonecall.reminder.service.CallService::class.java).apply {
            action = com.phonecall.reminder.service.CallService.ACTION_START_CALL
            putExtra(com.phonecall.reminder.service.CallService.EXTRA_REMINDER_ID, reminder.id)
            putExtra(com.phonecall.reminder.service.CallService.EXTRA_CALLER_NAME, reminder.callerName)
            putExtra(com.phonecall.reminder.service.CallService.EXTRA_PHONE_NUMBER, reminder.phoneNumber)
            putExtra(com.phonecall.reminder.service.CallService.EXTRA_VOICE_MESSAGE, reminder.voiceMessage)
            putExtra(com.phonecall.reminder.service.CallService.EXTRA_RINGTONE_URI, reminder.ringtoneUri)
            putExtra(com.phonecall.reminder.service.CallService.EXTRA_REPEAT_INDEX, 0)
            putExtra(com.phonecall.reminder.service.CallService.EXTRA_REPEAT_COUNT, reminder.repeatCount)
            putExtra(com.phonecall.reminder.service.CallService.EXTRA_REPEAT_INTERVAL, reminder.repeatIntervalMinutes)
            putExtra(com.phonecall.reminder.service.CallService.EXTRA_SNOOZE_MINUTES, reminder.snoozeMinutes)
        }
        
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
            context.startForegroundService(serviceIntent)
        } else {
            context.startService(serviceIntent)
        }
    }
}
