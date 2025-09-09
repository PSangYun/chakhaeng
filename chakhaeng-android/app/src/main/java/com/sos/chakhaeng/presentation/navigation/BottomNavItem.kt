package com.sos.chakhaeng.presentation.navigation

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.ui.graphics.vector.ImageVector

data class BottomNavItem(
    val route: String,
    val icon: ImageVector,
    val label: String
)

val bottomNavItems = listOf(
    BottomNavItem(
        route = Routes.Home.route,
        icon = Icons.Default.Home,
        label = "홈"
    ),
    BottomNavItem(
        route = Routes.ViolationDetail.route,
        icon = Icons.Default.PhotoCamera,
        label = "탐지"
    ),
    BottomNavItem(
        route = Routes.Report.route,
        icon = Icons.Default.Report,
        label = "신고"
    ),
    BottomNavItem(
        route = Routes.Statistics.route,
        icon = Icons.Default.BarChart,
        label = "통계"
    ),
    BottomNavItem(
        route = Routes.Profile.route,
        icon = Icons.Default.Person,
        label = "프로필"
    )
)
