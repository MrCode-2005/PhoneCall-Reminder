package com.phonecall.reminder.ui.call

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.delay
import java.text.SimpleDateFormat
import java.util.*

// Colors matching the reference image
private val BackgroundColor = Color(0xFFF5F5F7)
private val PurpleWaveLight = Color(0xFFD8D8F0)
private val PurpleWaveMid = Color(0xFFC5C5E8)
private val PurpleWaveDark = Color(0xFFB8B8E0)
private val TextBlack = Color(0xFF1A1A1A)
private val TextGray = Color(0xFF666666)
private val EndCallRed = Color(0xFFE53935)
private val BlueIcon = Color(0xFF2196F3)
private val ActiveGreen = Color(0xFF4CAF50)

@Composable
fun ActiveCallScreen(
    callerName: String,
    phoneNumber: String,
    isSpeakerOn: Boolean,
    onToggleSpeaker: () -> Unit,
    onEndCall: () -> Unit
) {
    var currentTime by remember { mutableStateOf(getCurrentTimeFormatted()) }
    var callDuration by remember { mutableLongStateOf(0L) }
    
    // Update time every second
    LaunchedEffect(Unit) {
        while (true) {
            currentTime = getCurrentTimeFormatted()
            delay(1000)
        }
    }
    
    // Call duration timer
    LaunchedEffect(Unit) {
        while (true) {
            delay(1000)
            callDuration++
        }
    }
    
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(BackgroundColor)
    ) {
        // Purple abstract wave background
        ActiveCallWaveBackground()
        
        Column(
            modifier = Modifier
                .fillMaxSize()
                .statusBarsPadding(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Status bar
            ActiveCallStatusBar(currentTime = currentTime)
            
            Spacer(modifier = Modifier.height(40.dp))
            
            // Caller name
            Text(
                text = callerName,
                color = TextBlack,
                fontSize = 34.sp,
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(horizontal = 24.dp)
            )
            
            Spacer(modifier = Modifier.height(12.dp))
            
            // Phone number and carrier
            Row(
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = formatActiveCallPhone(phoneNumber),
                    color = TextGray,
                    fontSize = 16.sp
                )
                Text(
                    text = "   Kerala Mobile Telephony",
                    color = TextGray,
                    fontSize = 16.sp
                )
            }
            
            Spacer(modifier = Modifier.height(8.dp))
            
            // Call duration
            Row(
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(4.dp))
                        .background(ActiveGreen)
                        .padding(horizontal = 6.dp, vertical = 2.dp)
                ) {
                    Text(
                        text = "HD",
                        color = Color.White,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
                
                Spacer(modifier = Modifier.width(8.dp))
                
                Text(
                    text = formatCallDuration(callDuration),
                    color = ActiveGreen,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Medium
                )
            }
            
            Spacer(modifier = Modifier.weight(1f))
            
            // Action buttons - 2 rows of 3
            Column(
                modifier = Modifier.padding(horizontal = 32.dp),
                verticalArrangement = Arrangement.spacedBy(32.dp)
            ) {
                // First row: Notes, Add call, Mute
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceEvenly
                ) {
                    ActiveCallActionButton(
                        icon = Icons.Outlined.Description,
                        label = "Notes"
                    )
                    ActiveCallActionButton(
                        icon = Icons.Outlined.AddCircleOutline,
                        label = "Add call"
                    )
                    ActiveCallActionButton(
                        icon = Icons.Outlined.MicOff,
                        label = "Mute"
                    )
                }
                
                // Second row: Record, Video call, Hold
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceEvenly
                ) {
                    ActiveCallActionButton(
                        icon = Icons.Outlined.FiberManualRecord,
                        label = "Record"
                    )
                    ActiveCallActionButton(
                        icon = Icons.Outlined.Videocam,
                        label = "Video call"
                    )
                    ActiveCallActionButton(
                        icon = Icons.Outlined.Pause,
                        label = "Hold"
                    )
                }
            }
            
            Spacer(modifier = Modifier.height(48.dp))
            
            // Bottom action bar
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 40.dp)
                    .padding(bottom = 48.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Keypad button
                Box(
                    modifier = Modifier
                        .size(56.dp)
                        .clickable(
                            interactionSource = remember { MutableInteractionSource() },
                            indication = null
                        ) { },
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Dialpad,
                        contentDescription = "Keypad",
                        tint = TextGray,
                        modifier = Modifier.size(28.dp)
                    )
                }
                
                // End call button (red)
                Box(
                    modifier = Modifier
                        .size(64.dp)
                        .clip(CircleShape)
                        .background(EndCallRed)
                        .clickable(
                            interactionSource = remember { MutableInteractionSource() },
                            indication = null
                        ) { onEndCall() },
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.CallEnd,
                        contentDescription = "End call",
                        tint = Color.White,
                        modifier = Modifier.size(28.dp)
                    )
                }
                
                // Speaker button
                Box(
                    modifier = Modifier
                        .size(56.dp)
                        .clickable(
                            interactionSource = remember { MutableInteractionSource() },
                            indication = null
                        ) { onToggleSpeaker() },
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = if (isSpeakerOn) Icons.Default.VolumeUp else Icons.Default.Bluetooth,
                        contentDescription = "Speaker",
                        tint = if (isSpeakerOn) ActiveGreen else BlueIcon,
                        modifier = Modifier.size(28.dp)
                    )
                }
            }
        }
    }
}

@Composable
private fun ActiveCallStatusBar(currentTime: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = currentTime,
            color = TextBlack,
            fontSize = 15.sp,
            fontWeight = FontWeight.Medium
        )
        
        Row(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = Icons.Default.SignalCellular4Bar,
                contentDescription = null,
                tint = TextBlack,
                modifier = Modifier.size(16.dp)
            )
            Icon(
                imageVector = Icons.Default.Wifi,
                contentDescription = null,
                tint = TextBlack,
                modifier = Modifier.size(16.dp)
            )
            Icon(
                imageVector = Icons.Default.BatteryFull,
                contentDescription = null,
                tint = TextBlack,
                modifier = Modifier.size(18.dp)
            )
        }
    }
}

@Composable
private fun ActiveCallWaveBackground() {
    Canvas(
        modifier = Modifier.fillMaxSize()
    ) {
        val width = size.width
        val height = size.height
        
        val wavePath1 = Path().apply {
            moveTo(width * 0.3f, height * 0.25f)
            cubicTo(
                width * 0.1f, height * 0.35f,
                width * -0.1f, height * 0.45f,
                width * 0.1f, height * 0.55f
            )
            cubicTo(
                width * 0.3f, height * 0.65f,
                width * 0.5f, height * 0.75f,
                width * 0.4f, height * 0.85f
            )
            cubicTo(
                width * 0.3f, height * 0.95f,
                width * 0.6f, height * 1.0f,
                width * 0.5f, height * 0.9f
            )
            cubicTo(
                width * 0.4f, height * 0.8f,
                width * 0.7f, height * 0.7f,
                width * 0.6f, height * 0.6f
            )
            cubicTo(
                width * 0.5f, height * 0.5f,
                width * 0.8f, height * 0.4f,
                width * 0.6f, height * 0.35f
            )
            cubicTo(
                width * 0.4f, height * 0.3f,
                width * 0.5f, height * 0.2f,
                width * 0.3f, height * 0.25f
            )
            close()
        }
        
        val wavePath2 = Path().apply {
            moveTo(width * 0.5f, height * 0.3f)
            cubicTo(
                width * 0.7f, height * 0.35f,
                width * 0.9f, height * 0.4f,
                width * 0.8f, height * 0.5f
            )
            cubicTo(
                width * 0.7f, height * 0.6f,
                width * 0.5f, height * 0.7f,
                width * 0.6f, height * 0.75f
            )
            cubicTo(
                width * 0.7f, height * 0.8f,
                width * 0.4f, height * 0.85f,
                width * 0.5f, height * 0.9f
            )
            cubicTo(
                width * 0.6f, height * 0.95f,
                width * 0.3f, height * 0.85f,
                width * 0.4f, height * 0.75f
            )
            cubicTo(
                width * 0.5f, height * 0.65f,
                width * 0.3f, height * 0.55f,
                width * 0.4f, height * 0.45f
            )
            cubicTo(
                width * 0.5f, height * 0.35f,
                width * 0.3f, height * 0.3f,
                width * 0.5f, height * 0.3f
            )
            close()
        }
        
        drawPath(
            path = wavePath1,
            brush = Brush.linearGradient(
                colors = listOf(
                    PurpleWaveLight.copy(alpha = 0.6f),
                    PurpleWaveMid.copy(alpha = 0.5f)
                ),
                start = Offset(0f, 0f),
                end = Offset(width, height)
            )
        )
        
        drawPath(
            path = wavePath2,
            brush = Brush.linearGradient(
                colors = listOf(
                    PurpleWaveMid.copy(alpha = 0.5f),
                    PurpleWaveDark.copy(alpha = 0.4f)
                ),
                start = Offset(width, 0f),
                end = Offset(0f, height)
            )
        )
    }
}

@Composable
private fun ActiveCallActionButton(
    icon: ImageVector,
    label: String,
    isActive: Boolean = false,
    onClick: () -> Unit = {}
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier.clickable(
            interactionSource = remember { MutableInteractionSource() },
            indication = null
        ) { onClick() }
    ) {
        Icon(
            imageVector = icon,
            contentDescription = label,
            tint = if (isActive) ActiveGreen else TextGray,
            modifier = Modifier.size(28.dp)
        )
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = label,
            color = if (isActive) ActiveGreen else TextGray,
            fontSize = 12.sp
        )
    }
}

private fun getCurrentTimeFormatted(): String {
    return SimpleDateFormat("H:mm", Locale.getDefault()).format(Date())
}

private fun formatCallDuration(seconds: Long): String {
    val mins = seconds / 60
    val secs = seconds % 60
    return String.format("%02d:%02d", mins, secs)
}

private fun formatActiveCallPhone(phone: String): String {
    if (phone.length == 10) {
        return "${phone.substring(0, 5)} ${phone.substring(5)}"
    }
    return phone
}
