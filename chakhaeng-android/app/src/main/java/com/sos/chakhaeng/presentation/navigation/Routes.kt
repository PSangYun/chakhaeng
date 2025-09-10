package com.sos.chakhaeng.presentation.navigation

sealed class Routes(val route: String) {
    object Login : Routes("login")
    object Home : Routes("home")
    object Detection : Routes("detection")
    object Report : Routes("report")
    object Statistics : Routes("statistics")
    object Profile : Routes("profile")
    object Streaming : Routes("streaming")
    object ViolationDetail : Routes("violation_detail")
}