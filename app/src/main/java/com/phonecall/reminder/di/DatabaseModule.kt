package com.phonecall.reminder.di

import android.content.Context
import androidx.room.Room
import com.phonecall.reminder.data.db.*
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {
    
    @Provides
    @Singleton
    fun provideAppDatabase(@ApplicationContext context: Context): AppDatabase {
        return Room.databaseBuilder(
            context,
            AppDatabase::class.java,
            AppDatabase.DATABASE_NAME
        ).build()
    }
    
    @Provides
    @Singleton
    fun provideReminderDao(database: AppDatabase): ReminderDao = database.reminderDao()
    
    @Provides
    @Singleton
    fun provideAlarmDao(database: AppDatabase): AlarmDao = database.alarmDao()
    
    @Provides
    @Singleton
    fun provideEventDao(database: AppDatabase): EventDao = database.eventDao()
    
    @Provides
    @Singleton
    fun provideDailyTaskDao(database: AppDatabase): DailyTaskDao = database.dailyTaskDao()
}
