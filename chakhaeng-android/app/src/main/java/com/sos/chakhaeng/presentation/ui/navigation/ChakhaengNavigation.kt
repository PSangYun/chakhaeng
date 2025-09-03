package com.sos.chakhaeng.presentation.ui.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import com.sos.chakhaeng.presentation.ui.screen.home.HomeScreen
import com.sos.chakhaeng.presentation.ui.screen.detection.DetectionScreen
import com.sos.chakhaeng.presentation.ui.screen.report.ReportScreen
import com.sos.chakhaeng.presentation.ui.screen.statistics.StatisticsScreen
import com.sos.chakhaeng.presentation.ui.screen.profile.ProfileScreen
import androidx.compose.ui.Modifier
import com.sos.chakhaeng.presentation.ui.screen.login.LoginScreen

@Composable
fun ChakhaengNavigation(
    navController: NavHostController,
    modifier: Modifier = Modifier
) {
    NavHost(
        navController = navController,
        startDestination = Routes.Login.route,
        modifier = modifier
    ) {
        composable(Routes.Login.route) {
            LoginScreen(
                navigateToHome = { navController.navigate(Routes.Home.route) {
                    popUpTo(0)
                    launchSingleTop = true
                } }
            )
        }
        composable(Routes.Home.route) {
            // Hilt가 자동으로 ViewModel을 주입합니다
            HomeScreen()
        }
        composable(Routes.Detection.route) {
            DetectionScreen()
        }
        composable(Routes.Report.route) {
            ReportScreen()
        }
        composable(Routes.Statistics.route) {
            StatisticsScreen()
        }
        composable(Routes.Profile.route) {
            ProfileScreen()
        }
    }
}