package com.sos.chakhaeng.core.navigation

import androidx.navigation3.runtime.NavKey
import kotlinx.serialization.Serializable

sealed interface Route : NavKey {
    @Serializable
    data object Login : Route

    @Serializable
    data class ReportDetail(val reportId : String) : Route

    @Serializable
    data class ViolationDetail(val violationId : String?) : Route
}


sealed interface BottomTabRoute : NavKey {
    @Serializable
    data object Home : BottomTabRoute

    @Serializable
    data object Detect : BottomTabRoute

    @Serializable
    data object Report : BottomTabRoute

    @Serializable
    data object Statistics : BottomTabRoute

    @Serializable
    data object Profile : BottomTabRoute
}
