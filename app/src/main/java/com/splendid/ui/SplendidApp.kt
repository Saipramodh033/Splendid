package com.splendid.ui

import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.splendid.ui.home.HomeScreen
import com.splendid.ui.analytics.AnalyticsScreen
import com.splendid.ui.calendar.CalendarScreen
import com.splendid.ui.more.MoreScreen
import com.splendid.ui.categories.CategoriesScreen
import com.splendid.ui.budget.BudgetScreen
import com.splendid.ui.iou.IOUScreen
import com.splendid.ui.theme.NavIconSelected
import com.splendid.ui.theme.NavIconUnselected

data class BottomNavItem(
    val route: String,
    val icon: ImageVector,
    val label: String
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SplendidApp() {
    val navController = rememberNavController()
    
    val bottomNavItems = listOf(
        BottomNavItem(Screen.Home.route, Icons.Default.Home, "Home"),
        BottomNavItem(Screen.Analytics.route, Icons.Default.BarChart, "Analytics"),
        BottomNavItem(Screen.Calendar.route, Icons.Default.CalendarToday, "Calendar"),
        BottomNavItem(Screen.More.route, Icons.Default.MoreHoriz, "More")
    )
    
    Scaffold(
        bottomBar = {
            val navBackStackEntry by navController.currentBackStackEntryAsState()
            val currentDestination = navBackStackEntry?.destination
            
            // Only show bottom bar on main screens
            if (currentDestination?.route in bottomNavItems.map { it.route }) {
                NavigationBar {
                    bottomNavItems.forEach { item ->
                        val isSelected = currentDestination?.hierarchy?.any { it.route == item.route } == true
                        NavigationBarItem(
                            icon = { 
                                Icon(
                                    item.icon, 
                                    contentDescription = item.label,
                                    tint = if (isSelected) NavIconSelected else NavIconUnselected
                                ) 
                            },
                            label = { Text(item.label) },
                            selected = isSelected,
                            onClick = {
                                navController.navigate(item.route) {
                                    popUpTo(navController.graph.findStartDestination().id) {
                                        saveState = true
                                    }
                                    launchSingleTop = true
                                    restoreState = true
                                }
                            },
                            colors = NavigationBarItemDefaults.colors(
                                selectedIconColor = NavIconSelected,
                                unselectedIconColor = NavIconUnselected,
                                selectedTextColor = NavIconSelected,
                                unselectedTextColor = NavIconUnselected
                            )
                        )
                    }
                }
            }
        }
    ) { innerPadding ->
        NavHost(
            navController = navController,
            startDestination = Screen.Home.route,
            modifier = Modifier.padding(innerPadding)
        ) {
            composable(Screen.Home.route) {
                HomeScreen(navController = navController)
            }
            composable(Screen.Analytics.route) {
                AnalyticsScreen(navController = navController)
            }
            composable(Screen.Calendar.route) {
                CalendarScreen(navController = navController)
            }
            composable(Screen.More.route) {
                MoreScreen(navController = navController)
            }
            composable(Screen.Categories.route) {
                CategoriesScreen(navController = navController)
            }
            composable(Screen.Budget.route) {
                BudgetScreen(navController = navController)
            }
            composable(Screen.IOU.route) {
                IOUScreen(navController = navController)
            }
            composable(Screen.DayDetail.route) { backStackEntry ->
                val dateMillis = backStackEntry.arguments?.getString("dateMillis")?.toLongOrNull()
                    ?: System.currentTimeMillis()
                com.splendid.ui.calendar.DayDetailScreen(
                    navController = navController,
                    dateMillis = dateMillis
                )
            }
        }
    }
}
