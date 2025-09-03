package com.sos.chakhaeng.presentation.ui.screen.login

import android.app.Activity
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.tooling.preview.Preview
import androidx.hilt.navigation.compose.hiltViewModel
import com.sos.chakhaeng.core.datastore.di.GoogleAuthManager
import com.sos.chakhaeng.presentation.ui.theme.ChakHaengTheme

@Composable
fun LoginScreen(
    viewModel: LoginViewModel = hiltViewModel()
) {
    val context = LocalContext.current
    val activity = context as Activity
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Button(onClick = {viewModel.login(
            getIdToken = {
                GoogleAuthManager(context).signInWithGoogle(activity)
            }
        )},
            modifier = Modifier.fillMaxWidth()) {
            Text("로그인")
        }
    }
}

@Preview(showBackground = true)
@Composable
fun LoginScreenPriview() {
    ChakHaengTheme {
        LoginScreen()
    }
}