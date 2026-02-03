package com.phonecall.reminder.scheduler

import com.phonecall.reminder.data.model.Reminder

interface AlarmScheduler {
    fun scheduleReminder(reminder: Reminder)
    fun cancelReminder(reminder: Reminder)
    fun scheduleSnooze(reminder: Reminder, snoozeMinutes: Int)
    fun scheduleRepeat(reminder: Reminder, intervalMinutes: Int, repeatIndex: Int)
    fun rescheduleAllReminders()
}
