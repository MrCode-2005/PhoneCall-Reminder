package com.phonecall.reminder.audio

import android.content.Context
import android.media.AudioAttributes
import android.media.AudioManager
import android.media.MediaPlayer
import android.media.Ringtone
import android.media.RingtoneManager as AndroidRingtoneManager
import android.net.Uri
import android.os.Build
import android.util.Log
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class RingtoneManager @Inject constructor(
    private val context: Context
) {
    private var ringtone: Ringtone? = null
    private var mediaPlayer: MediaPlayer? = null
    private val audioManager = context.getSystemService(Context.AUDIO_SERVICE) as AudioManager
    
    companion object {
        private const val TAG = "RingtoneManager"
    }
    
    fun playRingtone(ringtoneUri: String? = null, loop: Boolean = true) {
        Log.d(TAG, "playRingtone called with uri: $ringtoneUri, loop: $loop")
        stopRingtone()
        
        try {
            val uri = if (ringtoneUri != null) {
                Uri.parse(ringtoneUri)
            } else {
                AndroidRingtoneManager.getDefaultUri(AndroidRingtoneManager.TYPE_RINGTONE)
            }
            
            Log.d(TAG, "Playing ringtone from URI: $uri")
            
            // Check if it's a content:// URI (system ringtone) or file:// URI (custom audio)
            if (uri.toString().startsWith("content://") && !uri.toString().contains("external")) {
                // Use Ringtone API for system ringtones (more reliable)
                playWithRingtoneApi(uri, loop)
            } else {
                // Use MediaPlayer for custom audio files
                playWithMediaPlayer(uri, loop)
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error playing ringtone: ${e.message}", e)
            // Fallback to default ringtone
            playDefaultRingtone(loop)
        }
    }
    
    private fun playWithRingtoneApi(uri: Uri, loop: Boolean) {
        try {
            ringtone = AndroidRingtoneManager.getRingtone(context, uri)?.apply {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                    isLooping = loop
                }
                audioAttributes = AudioAttributes.Builder()
                    .setUsage(AudioAttributes.USAGE_NOTIFICATION_RINGTONE)
                    .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                    .build()
                play()
            }
            
            if (ringtone == null) {
                Log.w(TAG, "Ringtone is null, falling back to MediaPlayer")
                playWithMediaPlayer(uri, loop)
            } else {
                Log.d(TAG, "Ringtone started playing successfully")
            }
        } catch (e: Exception) {
            Log.e(TAG, "Ringtone API failed: ${e.message}", e)
            playWithMediaPlayer(uri, loop)
        }
    }
    
    private fun playWithMediaPlayer(uri: Uri, loop: Boolean) {
        try {
            Log.d(TAG, "Using MediaPlayer for: $uri")
            mediaPlayer = MediaPlayer().apply {
                setAudioAttributes(
                    AudioAttributes.Builder()
                        .setUsage(AudioAttributes.USAGE_NOTIFICATION_RINGTONE)
                        .setContentType(AudioAttributes.CONTENT_TYPE_MUSIC)
                        .build()
                )
                setDataSource(context, uri)
                isLooping = loop
                setOnPreparedListener { mp ->
                    Log.d(TAG, "MediaPlayer prepared, starting playback")
                    mp.start()
                }
                setOnErrorListener { _, what, extra ->
                    Log.e(TAG, "MediaPlayer error: what=$what, extra=$extra")
                    playDefaultRingtone(loop)
                    true
                }
                prepareAsync()
            }
        } catch (e: Exception) {
            Log.e(TAG, "MediaPlayer setup failed: ${e.message}", e)
            playDefaultRingtone(loop)
        }
    }
    
    private fun playDefaultRingtone(loop: Boolean) {
        try {
            Log.d(TAG, "Playing default ringtone as fallback")
            val defaultUri = AndroidRingtoneManager.getDefaultUri(AndroidRingtoneManager.TYPE_RINGTONE)
            ringtone = AndroidRingtoneManager.getRingtone(context, defaultUri)?.apply {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                    isLooping = loop
                }
                play()
            }
            
            if (ringtone == null || ringtone?.isPlaying != true) {
                Log.e(TAG, "Even default ringtone failed to play!")
            }
        } catch (e: Exception) {
            Log.e(TAG, "Default ringtone failed: ${e.message}", e)
        }
    }
    
    fun stopRingtone() {
        Log.d(TAG, "Stopping ringtone")
        
        ringtone?.let {
            if (it.isPlaying) {
                it.stop()
            }
        }
        ringtone = null
        
        mediaPlayer?.let {
            try {
                if (it.isPlaying) {
                    it.stop()
                }
                it.release()
            } catch (e: Exception) {
                Log.e(TAG, "Error stopping MediaPlayer: ${e.message}")
            }
        }
        mediaPlayer = null
    }
    
    fun isPlaying(): Boolean {
        return ringtone?.isPlaying == true || mediaPlayer?.isPlaying == true
    }
    
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

