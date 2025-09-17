package com.sos.chakhaeng.core.navigation.internal.navigator

import androidx.navigation3.runtime.NavKey

sealed interface InternalRoute {
    data class Navigate(
        val route: NavKey,
        val saveState: Boolean,
        val launchSingleTop: Boolean
    ) : InternalRoute

    data object NavigateBack : InternalRoute

    data class NavigateAndClearBackStack(
        val route: NavKey
    ) : InternalRoute
}
