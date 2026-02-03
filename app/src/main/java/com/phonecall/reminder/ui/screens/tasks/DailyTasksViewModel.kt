package com.phonecall.reminder.ui.screens.tasks

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.phonecall.reminder.data.model.DailyTask
import com.phonecall.reminder.data.repository.DailyTaskRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

data class DailyTasksUiState(
    val tasks: List<DailyTask> = emptyList(),
    val isLoading: Boolean = true
)

@HiltViewModel
class DailyTasksViewModel @Inject constructor(
    private val taskRepository: DailyTaskRepository
) : ViewModel() {
    
    private val _uiState = MutableStateFlow(DailyTasksUiState())
    val uiState: StateFlow<DailyTasksUiState> = _uiState.asStateFlow()
    
    init {
        loadTasks()
        resetDailyTasksIfNeeded()
    }
    
    private fun loadTasks() {
        viewModelScope.launch {
            taskRepository.getAllTasks()
                .collect { tasks ->
                    _uiState.update { it.copy(tasks = tasks, isLoading = false) }
                }
        }
    }
    
    private fun resetDailyTasksIfNeeded() {
        viewModelScope.launch {
            taskRepository.resetDailyTasks()
        }
    }
    
    fun toggleTaskComplete(task: DailyTask) {
        viewModelScope.launch {
            taskRepository.setTaskCompleted(task.id, !task.isCompleted)
        }
    }
    
    fun deleteTask(task: DailyTask) {
        viewModelScope.launch {
            taskRepository.deleteTask(task)
        }
    }
    
    fun saveTask(task: DailyTask) {
        viewModelScope.launch {
            taskRepository.insertTask(task)
        }
    }
}
