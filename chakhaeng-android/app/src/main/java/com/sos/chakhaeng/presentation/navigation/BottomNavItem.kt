package com.sos.chakhaeng.presentation.navigation

import com.sos.chakhaeng.R

data class BottomNavItem(
    val route: String,
    val icon: Int,
    val label: String
)

val bottomNavItems = listOf(
    BottomNavItem(
        route = Routes.Home.route,
        icon = R.drawable.home,
        label = "홈"
    ),
    BottomNavItem(
        route = Routes.Detection.route,
        icon = R.drawable.cam,
        label = "탐지"
    ),
    BottomNavItem(
        route = Routes.Report.route,
        icon = R.drawable.report,
        label = "신고"
    ),
    BottomNavItem(
        route = Routes.Statistics.route,
        icon = R.drawable.stats,
        label = "통계"
    ),
    BottomNavItem(
        route = Routes.Profile.route,
        icon = R.drawable.profile,
        label = "프로필"
    )
)
