package com.sos.chakhaeng.presentation.ui.navigation

sealed class Routes(val route: String) {
    object Home : Routes("home")
    object Detection : Routes("detection")
    object Report : Routes("report")
    object Statistics : Routes("statistics")
    object Profile : Routes("profile")
}