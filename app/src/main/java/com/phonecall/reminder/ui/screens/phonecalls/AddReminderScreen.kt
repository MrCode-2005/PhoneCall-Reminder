package com.phonecall.reminder.ui.screens.phonecalls

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.phonecall.reminder.data.model.RecurrenceType
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddReminderScreen(
    reminderId: Long? = null,
    viewModel: PhoneCallsViewModel = hiltViewModel(),
    onNavigateBack: () -> Unit
) {
    val scope = rememberCoroutineScope()
    val scrollState = rememberScrollState()
    
    var callerName by remember { mutableStateOf("") }
    var phoneNumber by remember { mutableStateOf("") }
    var voiceMessage by remember { mutableStateOf("") }
    var selectedHour by remember { mutableIntStateOf(9) }
    var selectedMinute by remember { mutableIntStateOf(0) }
    var recurrenceType by remember { mutableStateOf(RecurrenceType.DAILY) }
    var selectedDays by remember { mutableStateOf(listOf(2, 3, 4, 5, 6)) } // Mon-Fri
    var dayOfMonth by remember { mutableIntStateOf(1) }
    var snoozeMinutes by remember { mutableIntStateOf(5) }
    var repeatCount by remember { mutableIntStateOf(1) }
    var repeatInterval by remember { mutableIntStateOf(5) }
    var selectedRingtoneUri by remember { mutableStateOf<String?>(null) }
    var selectedRingtoneName by remember { mutableStateOf("Default Ringtone") }
    
    var showTimePicker by remember { mutableStateOf(false) }
    var showRingtonePicker by remember { mutableStateOf(false) }
    var isLoading by remember { mutableStateOf(reminderId != null) }
    
    // Load existing reminder if editing
    LaunchedEffect(reminderId) {
        if (reminderId != null) {
            val reminder = viewModel.getReminderById(reminderId)
            reminder?.let {
                callerName = it.callerName
                phoneNumber = it.phoneNumber
                voiceMessage = it.voiceMessage
                selectedHour = it.scheduledTimeHour
                selectedMinute = it.scheduledTimeMinute
                recurrenceType = it.recurrenceType
                selectedDays = it.daysOfWeek
                dayOfMonth = it.dayOfMonth
                snoozeMinutes = it.snoozeMinutes
                repeatCount = it.repeatCount
                repeatInterval = it.repeatIntervalMinutes
                selectedRingtoneUri = it.ringtoneUri
                if (it.ringtoneUri != null) {
                    selectedRingtoneName = "Custom Ringtone"
                }
            }
            isLoading = false
        }
    }
    
    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        if (reminderId == null) "Add Reminder" else "Edit Reminder",
                        fontWeight = FontWeight.Bold
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    TextButton(
                        onClick = {
                            scope.launch {
                                viewModel.saveReminder(
                                    id = reminderId ?: 0,
                                    callerName = callerName,
                                    phoneNumber = phoneNumber,
                                    voiceMessage = voiceMessage,
                                    hour = selectedHour,
                                    minute = selectedMinute,
                                    recurrenceType = recurrenceType,
                                    daysOfWeek = selectedDays,
                                    dayOfMonth = dayOfMonth,
                                    ringtoneUri = selectedRingtoneUri,
                                    snoozeMinutes = snoozeMinutes,
                                    repeatCount = repeatCount,
                                    repeatIntervalMinutes = repeatInterval
                                )
                                onNavigateBack()
                            }
                        },
                        enabled = callerName.isNotBlank()
                    ) {
                        Text("Save", fontWeight = FontWeight.Bold)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background
                )
            )
        }
    ) { paddingValues ->
        if (isLoading) {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator()
            }
        } else {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
                    .verticalScroll(scrollState)
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Caller Info Section
                SectionCard(title = "Caller Information") {
                    OutlinedTextField(
                        value = callerName,
                        onValueChange = { callerName = it },
                        label = { Text("Caller Name") },
                        leadingIcon = { Icon(Icons.Default.Person, null) },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp)
                    )
                    
                    Spacer(modifier = Modifier.height(12.dp))
                    
                    OutlinedTextField(
                        value = phoneNumber,
                        onValueChange = { phoneNumber = it },
                        label = { Text("Phone Number (optional)") },
                        leadingIcon = { Icon(Icons.Default.Phone, null) },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp)
                    )
                }
                
                // Voice Message Section
                SectionCard(title = "Voice Message") {
                    OutlinedTextField(
                        value = voiceMessage,
                        onValueChange = { voiceMessage = it },
                        label = { Text("Message to speak when answered") },
                        leadingIcon = { Icon(Icons.Default.RecordVoiceOver, null) },
                        modifier = Modifier.fillMaxWidth(),
                        minLines = 3,
                        shape = RoundedCornerShape(12.dp)
                    )
                }
                
                // Schedule Section
                SectionCard(title = "Schedule") {
                    // Time Picker
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(12.dp))
                            .background(MaterialTheme.colorScheme.surfaceVariant)
                            .clickable { showTimePicker = true }
                            .padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            Icons.Default.AccessTime,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary
                        )
                        Spacer(modifier = Modifier.width(12.dp))
                        Text(
                            text = formatTime(selectedHour, selectedMinute),
                            style = MaterialTheme.typography.headlineMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                    
                    Spacer(modifier = Modifier.height(16.dp))
                    
                    // Recurrence Type
                    Text(
                        "Repeat",
                        style = MaterialTheme.typography.labelLarge,
                        color = Color.Gray
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        RecurrenceType.entries.forEach { type ->
                            FilterChip(
                                selected = recurrenceType == type,
                                onClick = { recurrenceType = type },
                                label = { Text(type.name.lowercase().replaceFirstChar { it.uppercase() }) },
                                modifier = Modifier.weight(1f)
                            )
                        }
                    }
                    
                    // Weekly Day Selector
                    if (recurrenceType == RecurrenceType.WEEKLY) {
                        Spacer(modifier = Modifier.height(16.dp))
                        Text(
                            "Days",
                            style = MaterialTheme.typography.labelLarge,
                            color = Color.Gray
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceEvenly
                        ) {
                            listOf("S", "M", "T", "W", "T", "F", "S").forEachIndexed { index, day ->
                                val dayNum = index + 1
                                val isSelected = dayNum in selectedDays
                                
                                Box(
                                    modifier = Modifier
                                        .size(40.dp)
                                        .clip(RoundedCornerShape(10.dp))
                                        .background(
                                            if (isSelected) MaterialTheme.colorScheme.primary
                                            else MaterialTheme.colorScheme.surfaceVariant
                                        )
                                        .clickable {
                                            selectedDays = if (isSelected) {
                                                selectedDays - dayNum
                                            } else {
                                                selectedDays + dayNum
                                            }
                                        },
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        text = day,
                                        color = if (isSelected) Color.White else Color.Gray,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                            }
                        }
                    }
                }
                
                // Ringtone Section
                SectionCard(title = "Ringtone") {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(12.dp))
                            .background(MaterialTheme.colorScheme.surfaceVariant)
                            .clickable { showRingtonePicker = true }
                            .padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            Icons.Default.MusicNote,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary
                        )
                        Spacer(modifier = Modifier.width(12.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = selectedRingtoneName,
                                style = MaterialTheme.typography.bodyLarge,
                                fontWeight = FontWeight.Medium
                            )
                            Text(
                                text = "Tap to change ringtone",
                                style = MaterialTheme.typography.bodySmall,
                                color = Color.Gray
                            )
                        }
                        Icon(
                            Icons.Default.ChevronRight,
                            contentDescription = null,
                            tint = Color.Gray
                        )
                    }
                }
                
                // Advanced Options
                SectionCard(title = "Advanced Options") {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text("Snooze Duration")
                            Text(
                                "$snoozeMinutes minutes",
                                style = MaterialTheme.typography.bodySmall,
                                color = Color.Gray
                            )
                        }
                        Slider(
                            value = snoozeMinutes.toFloat(),
                            onValueChange = { snoozeMinutes = it.toInt() },
                            valueRange = 1f..30f,
                            steps = 28,
                            modifier = Modifier.width(150.dp)
                        )
                    }
                    
                    Spacer(modifier = Modifier.height(16.dp))
                    
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text("Repeat Call")
                            Text(
                                "$repeatCount time(s)",
                                style = MaterialTheme.typography.bodySmall,
                                color = Color.Gray
                            )
                        }
                        Slider(
                            value = repeatCount.toFloat(),
                            onValueChange = { repeatCount = it.toInt() },
                            valueRange = 1f..5f,
                            steps = 3,
                            modifier = Modifier.width(150.dp)
                        )
                    }
                    
                    if (repeatCount > 1) {
                        Spacer(modifier = Modifier.height(16.dp))
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text("Repeat Interval")
                                Text(
                                    "$repeatInterval minutes between calls",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = Color.Gray
                                )
                            }
                            Slider(
                                value = repeatInterval.toFloat(),
                                onValueChange = { repeatInterval = it.toInt() },
                                valueRange = 1f..15f,
                                steps = 13,
                                modifier = Modifier.width(150.dp)
                            )
                        }
                    }
                }
                
                Spacer(modifier = Modifier.height(32.dp))
            }
        }
    }
    
    // Time Picker Dialog
    if (showTimePicker) {
        val timePickerState = rememberTimePickerState(
            initialHour = selectedHour,
            initialMinute = selectedMinute
        )
        
        AlertDialog(
            onDismissRequest = { showTimePicker = false },
            confirmButton = {
                TextButton(onClick = {
                    selectedHour = timePickerState.hour
                    selectedMinute = timePickerState.minute
                    showTimePicker = false
                }) {
                    Text("OK")
                }
            },
            dismissButton = {
                TextButton(onClick = { showTimePicker = false }) {
                    Text("Cancel")
                }
            },
            text = {
                TimePicker(state = timePickerState)
            }
        )
    }
    
    // Ringtone Picker Dialog
    if (showRingtonePicker) {
        val context = androidx.compose.ui.platform.LocalContext.current
        val ringtones = remember {
            val list = mutableListOf<Pair<String, String?>>()
            list.add("Default Ringtone" to null)
            
            val ringtoneManager = android.media.RingtoneManager(context).apply {
                setType(android.media.RingtoneManager.TYPE_RINGTONE)
            }
            val cursor = ringtoneManager.cursor
            while (cursor.moveToNext()) {
                val title = cursor.getString(android.media.RingtoneManager.TITLE_COLUMN_INDEX)
                val uri = ringtoneManager.getRingtoneUri(cursor.position)
                list.add(title to uri.toString())
            }
            list
        }
        
        AlertDialog(
            onDismissRequest = { showRingtonePicker = false },
            title = { Text("Select Ringtone") },
            text = {
                androidx.compose.foundation.lazy.LazyColumn(
                    modifier = Modifier.heightIn(max = 400.dp)
                ) {
                    androidx.compose.foundation.lazy.items(ringtones) { item ->
                        val name = item.first
                        val uri = item.second
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable {
                                    selectedRingtoneName = name
                                    selectedRingtoneUri = uri
                                    showRingtonePicker = false
                                }
                                .padding(vertical = 12.dp, horizontal = 8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            RadioButton(
                                selected = selectedRingtoneUri == uri,
                                onClick = {
                                    selectedRingtoneName = name
                                    selectedRingtoneUri = uri
                                    showRingtonePicker = false
                                }
                            )
                            Spacer(modifier = Modifier.width(12.dp))
                            Text(
                                text = name,
                                style = MaterialTheme.typography.bodyLarge
                            )
                        }
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = { showRingtonePicker = false }) {
                    Text("Cancel")
                }
            }
        )
    }
}

@Composable
private fun SectionCard(
    title: String,
    content: @Composable ColumnScope.() -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        )
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Text(
                text = title,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.primary
            )
            Spacer(modifier = Modifier.height(12.dp))
            content()
        }
    }
}

private fun formatTime(hour: Int, minute: Int): String {
    val period = if (hour < 12) "AM" else "PM"
    val displayHour = when {
        hour == 0 -> 12
        hour > 12 -> hour - 12
        else -> hour
    }
    return String.format("%d:%02d %s", displayHour, minute, period)
}
