package com.phonecall.reminder.ui.navigation

sealed class Screen(val route: String) {
    data object PhoneCalls : Screen("phone_calls")
    data object Alarms : Screen("alarms")
    data object Events : Screen("events")
    data object DailyTasks : Screen("daily_tasks")
    data object AddReminder : Screen("add_reminder")
    data object EditReminder : Screen("edit_reminder/{id}") {
        fun createRoute(id: Long) = "edit_reminder/$id"
    }
    data object AddAlarm : Screen("add_alarm")
    data object AddEvent : Screen("add_event")
    data object AddTask : Screen("add_task")
    data object Settings : Screen("settings")
}
