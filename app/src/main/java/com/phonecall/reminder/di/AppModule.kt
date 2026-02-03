package com.phonecall.reminder.di

import android.content.Context
import com.phonecall.reminder.audio.RingtoneManager
import com.phonecall.reminder.audio.TextToSpeechManager
import com.phonecall.reminder.audio.VibrationManager
import com.phonecall.reminder.scheduler.AlarmScheduler
import com.phonecall.reminder.scheduler.AlarmSchedulerImpl
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object AppModule {
    
    @Provides
    @Singleton
    fun provideAlarmScheduler(@ApplicationContext context: Context): AlarmScheduler {
        return AlarmSchedulerImpl(context)
    }
    
    @Provides
    @Singleton
    fun provideRingtoneManager(@ApplicationContext context: Context): RingtoneManager {
        return RingtoneManager(context)
    }
    
    @Provides
    @Singleton
    fun provideVibrationManager(@ApplicationContext context: Context): VibrationManager {
        return VibrationManager(context)
    }
    
    @Provides
    @Singleton
    fun provideTextToSpeechManager(@ApplicationContext context: Context): TextToSpeechManager {
        return TextToSpeechManager(context)
    }
}
