package com.sos.chakhaeng.presentation.ui.screen.login

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.tooling.preview.Preview
import androidx.hilt.navigation.compose.hiltViewModel
import com.sos.chakhaeng.presentation.ui.theme.ChakHaengTheme

@Composable
fun LoginScreen(
    navigateToHome: () -> Unit = {},
    viewModel: LoginViewModel = hiltViewModel()
) {
    val context = LocalContext.current
    val state = viewModel.uiState.collectAsState()

    LaunchedEffect(state) {
        if (state.value is LoginUiState.Success) {
            navigateToHome()
            viewModel.consumeSuccess()
        }
    }

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Button(
            onClick = {viewModel.login() },
            modifier = Modifier.fillMaxWidth()) {
            Text(
                when (state) {
                    LoginUiState.Loading -> "로그인 중..."
                    else -> "로그인"
                }
            )
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