package com.phonecall.reminder.audio

import android.content.Context
import android.media.AudioAttributes
import android.media.AudioManager
import android.media.MediaPlayer
import android.media.RingtoneManager as AndroidRingtoneManager
import android.net.Uri
import android.os.Build
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class RingtoneManager @Inject constructor(
    private val context: Context
) {
    private var mediaPlayer: MediaPlayer? = null
    private val audioManager = context.getSystemService(Context.AUDIO_SERVICE) as AudioManager
    
    fun playRingtone(ringtoneUri: String? = null, loop: Boolean = true) {
        stopRingtone()
        
        try {
            val uri = if (ringtoneUri != null) {
                Uri.parse(ringtoneUri)
            } else {
                AndroidRingtoneManager.getDefaultUri(AndroidRingtoneManager.TYPE_RINGTONE)
            }
            
            mediaPlayer = MediaPlayer().apply {
                setAudioAttributes(
                    AudioAttributes.Builder()
                        .setUsage(AudioAttributes.USAGE_NOTIFICATION_RINGTONE)
                        .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                        .build()
                )
                setDataSource(context, uri)
                isLooping = loop
                prepare()
                start()
            }
        } catch (e: Exception) {
            e.printStackTrace()
            // Fallback to default ringtone if custom one fails
            try {
                val defaultUri = AndroidRingtoneManager.getDefaultUri(AndroidRingtoneManager.TYPE_RINGTONE)
                mediaPlayer = MediaPlayer().apply {
                    setAudioAttributes(
                        AudioAttributes.Builder()
                            .setUsage(AudioAttributes.USAGE_NOTIFICATION_RINGTONE)
                            .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                            .build()
                    )
                    setDataSource(context, defaultUri)
                    isLooping = loop
                    prepare()
                    start()
                }
            } catch (e2: Exception) {
                e2.printStackTrace()
            }
        }
    }
    
    fun stopRingtone() {
        mediaPlayer?.apply {
            if (isPlaying) {
                stop()
            }
            release()
        }
        mediaPlayer = null
    }
    
    fun isPlaying(): Boolean = mediaPlayer?.isPlaying == true
    
    fun getAvailableRingtones(): List<Pair<String, Uri>> {
        val ringtones = mutableListOf<Pair<String, Uri>>()
        val ringtoneManager = AndroidRingtoneManager(context).apply {
            setType(AndroidRingtoneManager.TYPE_RINGTONE)
        }
        
        val cursor = ringtoneManager.cursor
        while (cursor.moveToNext()) {
            val title = cursor.getString(AndroidRingtoneManager.TITLE_COLUMN_INDEX)
            val uri = ringtoneManager.getRingtoneUri(cursor.position)
            ringtones.add(title to uri)
        }
        
        return ringtones
    }
    
    fun getDefaultRingtoneUri(): Uri? {
        return AndroidRingtoneManager.getDefaultUri(AndroidRingtoneManager.TYPE_RINGTONE)
    }
}
