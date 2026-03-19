package com.splendid.ui

sealed class Screen(val route: String) {
    object Home : Screen("home")
    object Analytics : Screen("analytics")
    object Calendar : Screen("calendar")
    object More : Screen("more")
    object Categories : Screen("categories")
    object Budget : Screen("budget")
    object IOU : Screen("iou")
    object DayDetail : Screen("day_detail/{dateMillis}") {
        fun createRoute(dateMillis: Long) = "day_detail/$dateMillis"
    }
}
