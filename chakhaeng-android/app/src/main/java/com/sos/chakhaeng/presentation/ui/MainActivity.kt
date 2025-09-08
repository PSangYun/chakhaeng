package com.sos.chakhaeng.presentation.ui

import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.compose.rememberNavController
import com.sos.chakhaeng.presentation.ui.theme.ChakHaengTheme
import com.sos.chakhaeng.presentation.ui.navigation.ChakhaengNavigation
import com.sos.chakhaeng.presentation.ui.components.BottomNavigationBar
import dagger.hilt.android.AndroidEntryPoint
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.compose.currentBackStackEntryAsState
import com.sos.chakhaeng.datastore.di.GoogleAuthManager
import com.sos.chakhaeng.session.AppEntryViewModel
import com.sos.chakhaeng.session.AuthState
import com.sos.chakhaeng.presentation.ui.navigation.Routes
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
                    appEntryViewModel = appEntryViewModel
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ChakhaengApp(
    googleAuthManager: GoogleAuthManager,
    appEntryViewModel: AppEntryViewModel) {
    val navController = rememberNavController()

//    val vm: AppEntryViewModel = hiltViewModel()
    val authState by appEntryViewModel.authState.collectAsState()

    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route

    val showBottomBar by remember(currentRoute) {
        derivedStateOf {
            currentRoute != Routes.Login.route
        }
    }

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        bottomBar = {
            if (showBottomBar) {
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
                    else Routes.Login.route

                ChakhaengNavigation(
                    navController = navController,
                    modifier = Modifier,
                    googleAuthManager = googleAuthManager,
                    startDestination = startDestination,
                    paddingValues = paddingValues,
                    authState = authState
                )
            }
        }
    }

}