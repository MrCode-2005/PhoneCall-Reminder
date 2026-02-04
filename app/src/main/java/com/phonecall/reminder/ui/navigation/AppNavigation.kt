package com.phonecall.reminder.ui.navigation

import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import com.phonecall.reminder.ui.screens.alarms.AddAlarmScreen
import com.phonecall.reminder.ui.screens.alarms.AlarmsScreen
import com.phonecall.reminder.ui.screens.events.AddEventScreen
import com.phonecall.reminder.ui.screens.events.EventsScreen
import com.phonecall.reminder.ui.screens.phonecalls.AddReminderScreen
import com.phonecall.reminder.ui.screens.phonecalls.PhoneCallsScreen
import com.phonecall.reminder.ui.screens.tasks.AddTaskScreen
import com.phonecall.reminder.ui.screens.tasks.DailyTasksScreen

data class BottomNavItem(
    val route: String,
    val title: String,
    val selectedIcon: ImageVector,
    val unselectedIcon: ImageVector
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AppNavigation(navController: NavHostController) {
    val bottomNavItems = listOf(
        BottomNavItem(
            route = Screen.PhoneCalls.route,
            title = "Calls",
            selectedIcon = Icons.Filled.Phone,
            unselectedIcon = Icons.Outlined.Phone
        ),
        BottomNavItem(
            route = Screen.Alarms.route,
            title = "Alarms",
            selectedIcon = Icons.Filled.Alarm,
            unselectedIcon = Icons.Outlined.Alarm
        ),
        BottomNavItem(
            route = Screen.Events.route,
            title = "Events",
            selectedIcon = Icons.Filled.Event,
            unselectedIcon = Icons.Outlined.Event
        ),
        BottomNavItem(
            route = Screen.DailyTasks.route,
            title = "Tasks",
            selectedIcon = Icons.Filled.CheckCircle,
            unselectedIcon = Icons.Outlined.CheckCircle
        )
    )
    
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route
    
    val showBottomBar = currentRoute in bottomNavItems.map { it.route }
    
    Scaffold(
        bottomBar = {
            if (showBottomBar) {
                NavigationBar(
                    containerColor = MaterialTheme.colorScheme.surface,
                    contentColor = MaterialTheme.colorScheme.onSurface
                ) {
                    bottomNavItems.forEach { item ->
                        val selected = currentRoute == item.route
                        NavigationBarItem(
                            icon = {
                                Icon(
                                    imageVector = if (selected) item.selectedIcon else item.unselectedIcon,
                                    contentDescription = item.title
                                )
                            },
                            label = { Text(item.title) },
                            selected = selected,
                            onClick = {
                                if (currentRoute != item.route) {
                                    navController.navigate(item.route) {
                                        popUpTo(Screen.PhoneCalls.route) {
                                            saveState = true
                                        }
                                        launchSingleTop = true
                                        restoreState = true
                                    }
                                }
                            },
                            colors = NavigationBarItemDefaults.colors(
                                selectedIconColor = MaterialTheme.colorScheme.primary,
                                selectedTextColor = MaterialTheme.colorScheme.primary,
                                indicatorColor = MaterialTheme.colorScheme.primaryContainer
                            )
                        )
                    }
                }
            }
        }
    ) { paddingValues ->
        NavHost(
            navController = navController,
            startDestination = Screen.PhoneCalls.route,
            modifier = Modifier.padding(paddingValues)
        ) {
            composable(Screen.PhoneCalls.route) {
                PhoneCallsScreen(
                    onAddReminder = { navController.navigate(Screen.AddReminder.route) },
                    onEditReminder = { id -> navController.navigate(Screen.EditReminder.createRoute(id)) }
                )
            }
            
            composable(Screen.Alarms.route) {
                AlarmsScreen(
                    onAddAlarm = { navController.navigate(Screen.AddAlarm.route) }
                )
            }
            
            composable(Screen.Events.route) {
                EventsScreen(
                    onAddEvent = { navController.navigate(Screen.AddEvent.route) }
                )
            }
            
            composable(Screen.DailyTasks.route) {
                DailyTasksScreen(
                    onAddTask = { navController.navigate(Screen.AddTask.route) }
                )
            }
            
            composable(Screen.AddReminder.route) {
                AddReminderScreen(
                    onNavigateBack = { navController.popBackStack() }
                )
            }
            
            composable(Screen.EditReminder.route) { backStackEntry ->
                val id = backStackEntry.arguments?.getString("id")?.toLongOrNull() ?: return@composable
                AddReminderScreen(
                    reminderId = id,
                    onNavigateBack = { navController.popBackStack() }
                )
            }
            
            composable(Screen.AddAlarm.route) {
                AddAlarmScreen(
                    onNavigateBack = { navController.popBackStack() }
                )
            }
            
            composable(Screen.AddEvent.route) {
                AddEventScreen(
                    onNavigateBack = { navController.popBackStack() }
                )
            }
            
            composable(Screen.AddTask.route) {
                AddTaskScreen(
                    onNavigateBack = { navController.popBackStack() }
                )
            }
        }
    }
}
