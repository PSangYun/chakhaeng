package com.sos.chakhaeng.presentation.ui.navigation

import android.util.Log
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import com.sos.chakhaeng.presentation.ui.screen.home.HomeScreen
import com.sos.chakhaeng.presentation.ui.screen.detection.DetectionScreen
import com.sos.chakhaeng.presentation.ui.screen.report.ReportScreen
import com.sos.chakhaeng.presentation.ui.screen.statistics.StatisticsScreen
import com.sos.chakhaeng.presentation.ui.screen.profile.ProfileScreen
import androidx.compose.ui.Modifier
import com.sos.chakhaeng.datastore.di.GoogleAuthManager
import com.sos.chakhaeng.presentation.ui.screen.login.LoginScreen
import com.sos.chakhaeng.presentation.ui.screen.violationDetail.ViolationDetailScreen
import com.sos.chakhaeng.session.AuthState
import okhttp3.Route

@Composable
fun ChakhaengNavigation(
    navController: NavHostController,
    modifier: Modifier = Modifier,
    googleAuthManager: GoogleAuthManager,
    startDestination: String,
    paddingValues: PaddingValues,
    authState: AuthState
) {
    NavHost(
        navController = navController,
        startDestination = startDestination,
        modifier = modifier
    ) {
        composable(Routes.Login.route) {
            LoginScreen(
                navigateToHome = { navController.navigate(Routes.Home.route) {
                    popUpTo(0)
                    launchSingleTop = true
                } },
                googleAuthManager = googleAuthManager
            )
        }
        composable(Routes.Home.route) {
            HomeScreen(
                onNavigateToDetection = {
                    navController.navigate(Routes.Detection.route) {
                        launchSingleTop = true
                        popUpTo(Routes.Home.route) {
                            inclusive = true
                        }
                    }
                }
            )
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
        composable(Routes.ViolationDetail.route) {
            ViolationDetailScreen(
                onBack = {},
                onSubmitToGovernment = {},
                paddingVaules = paddingValues
            )
        }
    }
    LaunchedEffect(authState) {
        when(authState) {
            is AuthState.Authenticated -> {
                if (navController.currentDestination?.route != Routes.Home.route) {
                    navController.navigate(Routes.Home.route) {
                        Log.d("TAG", "ChakhaengApp: 123")
                        popUpTo(0)
                        launchSingleTop = true
                    }
                }
            }
            AuthState.Unauthenticated -> {
                if (navController.currentDestination?.route != Routes.Login.route) {
                    navController.navigate(Routes.Login.route) {
                        popUpTo(0)
                        launchSingleTop = true
                    }
                }
            }

            AuthState.Loading -> Unit
        }
    }
}