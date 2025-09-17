package com.sos.chakhaeng.presentation.main

import androidx.compose.runtime.Composable
import androidx.navigation3.runtime.NavKey
import com.sos.chakhaeng.R
import com.sos.chakhaeng.core.navigation.BottomTabRoute
import kotlin.collections.any
import kotlin.collections.find
import kotlin.collections.map

internal enum class MainTab(
    val iconResId: Int,
    internal val contentDescription: String,
    val label: String,
    val route: BottomTabRoute
) {
    HOME(
        iconResId = R.drawable.home,
        contentDescription = "Home Icon",
        label = "홈",
        route = BottomTabRoute.Home,
    ),
    Detects(
        iconResId = R.drawable.cam,
        contentDescription = "Camera Icon",
        label = "감지",
        route = BottomTabRoute.Detect,
    ),
    Reports(
        iconResId = R.drawable.report,
        contentDescription = "Report Icon",
        label = "신고",
        route = BottomTabRoute.Report,
    ),
    Statistics(
        iconResId = R.drawable.stats,
        contentDescription = "Stats Icon",
        label = "통계",
        route = BottomTabRoute.Statistics,
    ),
    PROFILE(
        iconResId = R.drawable.profile,
        contentDescription = "Profile Icon",
        label = "나의 정보",
        route = BottomTabRoute.Profile,
    );

    companion object {
        @Composable
        fun find(predicate: @Composable (BottomTabRoute) -> Boolean): MainTab? {
            return entries.find { predicate(it.route) }
        }

        @Composable
        fun contains(predicate: @Composable (NavKey) -> Boolean): Boolean {
            return entries.map { it.route }.any { predicate(it) }
        }
    }
}
