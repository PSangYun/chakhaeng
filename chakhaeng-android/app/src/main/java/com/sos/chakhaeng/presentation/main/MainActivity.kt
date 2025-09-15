package com.sos.chakhaeng.presentation.main

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.sos.chakhaeng.core.session.AuthState
import com.sos.chakhaeng.core.session.GoogleAuthManager
import com.sos.chakhaeng.presentation.navigation.ChakhaengNavigation
import com.sos.chakhaeng.presentation.navigation.NoBottomRoute
import com.sos.chakhaeng.presentation.navigation.Routes
import com.sos.chakhaeng.presentation.navigation.shouldShowBottomBar
import com.sos.chakhaeng.presentation.theme.ChakHaengTheme
import com.sos.chakhaeng.presentation.ui.components.BottomNavigationBar
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    @Inject
    lateinit var googleAuthManager: GoogleAuthManager

    private val appEntryViewModel: AppEntryViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        val splashScreen = installSplashScreen()
        super.onCreate(savedInstanceState)
        splashScreen.setKeepOnScreenCondition {
            appEntryViewModel.authState.value is AuthState.Loading
        }
        enableEdgeToEdge()
        setContent {
            ChakHaengTheme {
                ChakhaengApp(
                    googleAuthManager,
                    appEntryViewModel = appEntryViewModel,
                    this
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ChakhaengApp(
    googleAuthManager: GoogleAuthManager,
    appEntryViewModel: AppEntryViewModel,
    activity: MainActivity
){
    val navController = rememberNavController()
    val authState by appEntryViewModel.authState.collectAsState()
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route

    val isBottomBarVisible by remember(currentRoute) {
        derivedStateOf {
            currentRoute.shouldShowBottomBar()
        }
    }

    LaunchedEffect(Unit) {
        appEntryViewModel.init(activity)
    }

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        bottomBar = {
            if (isBottomBarVisible) {
                BottomNavigationBar(navController = navController)
            }
        }
    ) { paddingValues ->
        when(authState) {
            AuthState.Loading -> {

            }
            is AuthState.Authenticated, AuthState.Unauthenticated -> {
                val startDestination =
                    if (authState is AuthState.Authenticated) Routes.Home.route
                    else NoBottomRoute.Login.route

                ChakhaengNavigation(
                    navController = navController,
                    modifier = Modifier,
                    googleAuthManager = googleAuthManager,
                    startDestination = startDestination,
                    paddingValues = paddingValues,
                    authState = authState,
                    appEntryViewModel
                )
            }
        }
    }
}