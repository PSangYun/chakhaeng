package com.sos.chakhaeng.presentation.ui.screen.login

import android.annotation.SuppressLint
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.sos.chakhaeng.R
import com.sos.chakhaeng.core.session.GoogleAuthManager
import com.sos.chakhaeng.presentation.ui.components.angledLinearGradientBackground
import com.sos.chakhaeng.presentation.theme.ChakHaengTheme
import com.sos.chakhaeng.presentation.theme.backgroundLight
import com.sos.chakhaeng.presentation.theme.inversePrimaryLight
import com.sos.chakhaeng.presentation.theme.naverGreen
import com.sos.chakhaeng.presentation.theme.neutral
import com.sos.chakhaeng.presentation.theme.onPrimaryContainerLight
import com.sos.chakhaeng.presentation.theme.primaryContainerLight
import com.sos.chakhaeng.presentation.theme.primaryLight
import com.sos.chakhaeng.presentation.theme.tertiaryLight
import kotlinx.coroutines.launch
import kotlin.math.cos
import kotlin.math.sin

@Composable
fun LoginScreen(
    viewModel: LoginViewModel = hiltViewModel(),
    googleAuthManager: GoogleAuthManager
) {
    val state = viewModel.uiState.collectAsState()
    val scope = rememberCoroutineScope()

    LaunchedEffect(state) {
        if (state.value is LoginUiState.Success) {
            viewModel.consumeSuccess()
        }
    }

    Box(Modifier.fillMaxSize()) {
        LoginGradientBackground(
            Modifier.matchParentSize(),
            angleDeg = 45f)

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            Spacer(Modifier.height(48.dp))

            LoginHeader(
                title = "착행",
                subtitle = "교통위반 신고 및 안전 관리"
            )

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .graphicsLayer { clip = false },
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                GoogleSignInButton(onClick = {
                    scope.launch {
                        val (idToken, user) = googleAuthManager.signInWithGoogle()
                        viewModel.googleLogin(idToken, user)
                        viewModel.sendFcmToken()
                    }
                })
                NaverSignInButton(onClick ={
                    scope.launch {
                        val (idToken, user) = googleAuthManager.signInWithGoogle()
                        viewModel.googleLogin(idToken, user)
                    }
                })
//                TermsText(Modifier.padding(bottom = 24.dp))
                Spacer(Modifier.height(128.dp))
            }
        }
    }
}

/* 배경 그라데이션 */
@SuppressLint("UnusedBoxWithConstraintsScope")
@Composable
fun LoginGradientBackground(
    modifier: Modifier = Modifier,
    colors: List<Color> = listOf(backgroundLight, inversePrimaryLight),
    angleDeg: Float = 90f,
    stops: List<Float>? = null
    ) {
    BoxWithConstraints(modifier) {
        val density = LocalDensity.current
        val wPx = with(density) { maxWidth.toPx() }
        val hPx = with(density) { maxHeight.toPx() }

        // 각도 → 단위 방향 벡터
        val rad = Math.toRadians(angleDeg.toDouble())
        val vx = cos(rad).toFloat()
        val vy = sin(rad).toFloat()

        // 화면을 충분히 덮도록 대각선 길이 사용
        val len = kotlin.math.hypot(wPx, hPx)
        val cx = wPx / 2f
        val cy = hPx / 2f

        val start = Offset(cx - vx * len / 2f, cy - vy * len / 2f)
        val end   = Offset(cx + vx * len / 2f, cy + vy * len / 2f)

        val brush = if (stops != null) {
            require(stops.size == colors.size) { "stops와 colors의 크기가 같아야 합니다." }
            Brush.linearGradient(
                colorStops = stops.zip(colors).map { it.first to it.second }.toTypedArray(),
                start = start, end = end
            )
        } else {
            Brush.linearGradient(colors = colors, start = start, end = end)
        }

        Box(Modifier.fillMaxSize().background(brush))
    }
}

/* 상단 로고/타이틀 */
@Composable
fun LoginHeader(
    title: String,
    subtitle: String,
    modifier: Modifier = Modifier
) {
    Column(modifier, horizontalAlignment = Alignment.CenterHorizontally) {
        // 원형 그라데이션 배지
        Box(
            modifier = Modifier
                .size(240.dp)
                .angledLinearGradientBackground(
                    colors = listOf(primaryLight, onPrimaryContainerLight, tertiaryLight),
                    angleDeg = 45f,
                    shape = RoundedCornerShape(120.dp)
                )
                    ,
            contentAlignment = Alignment.Center
        ) {
            Image(
                painter = painterResource(id = R.drawable.police_rabbit2),
                contentDescription = null,
                modifier = Modifier.size(200.dp),
                contentScale = ContentScale.Fit
            )
        }
        Spacer(Modifier.height(24.dp))
        Text(text = title, style = MaterialTheme.typography.displayMedium, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.scrim)
        Spacer(Modifier.height(8.dp))
        Text(text = subtitle, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onBackground)
    }
}

/* 소셜 버튼 베이스 */
@Composable
fun SocialButton(
    text: String,
    icon: Painter,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    containerColor: Color,
    contentColor: Color,
    border: BorderStroke? = null,
) {
    Button(
        onClick = onClick,
        shape = RoundedCornerShape(16.dp),
        colors = ButtonDefaults.buttonColors(
            containerColor = containerColor,
            contentColor = contentColor
        ),
        border = border,
        modifier = modifier
            .fillMaxWidth()
            .height(64.dp)
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Icon(painter = icon, contentDescription = null, tint = Color.Unspecified)
            Text(text, style = MaterialTheme.typography.bodyLarge, fontWeight = FontWeight.Bold)
        }
    }
}

/* 구글 버튼 (화이트+테두리, 다크도 가독성 유지) */
@Composable
fun GoogleSignInButton(
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    SocialButton(
        text = "Google 계정으로 로그인",
        icon = painterResource(id = R.drawable.ic_google),
        onClick = onClick,
        modifier = modifier,
        containerColor = neutral,
        contentColor = Color.Black,
        border = BorderStroke(1.dp, primaryContainerLight)
    )
}

@Composable
fun NaverSignInButton(
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    SocialButton(
        text = "네이버 계정으로 로그인",
        icon = painterResource(id = R.drawable.ic_naver),
        onClick = onClick,
        modifier = modifier,
        containerColor = naverGreen,
        contentColor = Color.White
    )
}

/* 약관 문구 */
//@Composable
//fun TermsText(
//    modifier: Modifier = Modifier,
//    onClickTerms: () -> Unit = {},
//    onClickPrivacy: () -> Unit = {}
//) {
//    Column(modifier.fillMaxWidth(), horizontalAlignment = Alignment.CenterHorizontally) {
//        Spacer(Modifier.height(16.dp))
//        Text(
//            text = "계속 진행하시면",
//            color = Color(0xFF98A2B3),
//            fontSize = 13.sp
//        )
//        Spacer(Modifier.height(6.dp))
//        // 간단 버전: 클릭 콜백 분리 필요하면 AnnotatedString로 확장 가능
//        Text(
//            text = "이용약관 및 개인정보처리방침에 동의하는 것으로 간주됩니다.",
//            color = Color(0xFF4C42FF),
//            fontSize = 13.sp,
//            textAlign = TextAlign.Center
//        )
//    }
//}

@Preview(showBackground = true)
@Composable
fun LoginScreenPriview() {
    ChakHaengTheme {
        LoginScreen(googleAuthManager = GoogleAuthManager(LocalContext.current))
    }
}