package com.phonecall.reminder.ui.screens.alarms

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.phonecall.reminder.data.model.Alarm
import com.phonecall.reminder.data.repository.AlarmRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

data class AlarmsUiState(
    val alarms: List<Alarm> = emptyList(),
    val isLoading: Boolean = true
)

@HiltViewModel
class AlarmsViewModel @Inject constructor(
    private val alarmRepository: AlarmRepository
) : ViewModel() {
    
    private val _uiState = MutableStateFlow(AlarmsUiState())
    val uiState: StateFlow<AlarmsUiState> = _uiState.asStateFlow()
    
    init {
        loadAlarms()
    }
    
    private fun loadAlarms() {
        viewModelScope.launch {
            alarmRepository.getAllAlarms()
                .collect { alarms ->
                    _uiState.update { it.copy(alarms = alarms, isLoading = false) }
                }
        }
    }
    
    fun toggleAlarm(alarm: Alarm) {
        viewModelScope.launch {
            alarmRepository.setAlarmEnabled(alarm.id, !alarm.isEnabled)
        }
    }
    
    fun deleteAlarm(alarm: Alarm) {
        viewModelScope.launch {
            alarmRepository.deleteAlarm(alarm)
        }
    }
    
    fun saveAlarm(alarm: Alarm) {
        viewModelScope.launch {
            alarmRepository.insertAlarm(alarm)
        }
    }
}
