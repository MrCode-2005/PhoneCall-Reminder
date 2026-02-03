package com.phonecall.reminder.ui.screens.events

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.phonecall.reminder.data.model.Event
import com.phonecall.reminder.data.repository.EventRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

data class EventsUiState(
    val events: List<Event> = emptyList(),
    val isLoading: Boolean = true
)

@HiltViewModel
class EventsViewModel @Inject constructor(
    private val eventRepository: EventRepository
) : ViewModel() {
    
    private val _uiState = MutableStateFlow(EventsUiState())
    val uiState: StateFlow<EventsUiState> = _uiState.asStateFlow()
    
    init {
        loadEvents()
    }
    
    private fun loadEvents() {
        viewModelScope.launch {
            eventRepository.getAllEvents()
                .collect { events ->
                    _uiState.update { it.copy(events = events, isLoading = false) }
                }
        }
    }
    
    fun toggleEventComplete(event: Event) {
        viewModelScope.launch {
            eventRepository.setEventCompleted(event.id, !event.isCompleted)
        }
    }
    
    fun deleteEvent(event: Event) {
        viewModelScope.launch {
            eventRepository.deleteEvent(event)
        }
    }
    
    fun saveEvent(event: Event) {
        viewModelScope.launch {
            eventRepository.insertEvent(event)
        }
    }
}
