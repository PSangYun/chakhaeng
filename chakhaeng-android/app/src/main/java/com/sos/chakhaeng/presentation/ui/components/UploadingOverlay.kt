package com.sos.chakhaeng.presentation.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.airbnb.lottie.compose.LottieAnimation
import com.airbnb.lottie.compose.LottieCompositionSpec
import com.airbnb.lottie.compose.LottieConstants
import com.airbnb.lottie.compose.animateLottieCompositionAsState
import com.airbnb.lottie.compose.rememberLottieComposition
import com.sos.chakhaeng.presentation.theme.chakhaengTypography
import com.sos.chakhaeng.presentation.theme.primaryLight
import com.sos.chakhaeng.presentation.theme.scrimLight
import kotlin.math.roundToInt

@Composable
fun UploadingOverlay(
    visible: Boolean,
    progress: Float? = null,
    lottieUrl: String? = null,
    lottieRawRes: Int? = null
) {
    AnimatedVisibility(
        visible = visible,
        enter = fadeIn(),
        exit = fadeOut()
    ) {
        Box(
            Modifier
                .fillMaxSize()
                .background(scrimLight.copy(alpha = 0.55f)),
            contentAlignment = Alignment.Center
        ) {
            Surface(
                tonalElevation = 6.dp,
                shape = MaterialTheme.shapes.large
            ) {
                Column(
                    Modifier
                        .padding(horizontal = 24.dp, vertical = 20.dp)
                        .widthIn(min = 260.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    val spec = remember(lottieUrl, lottieRawRes) {
                        when {
                            lottieUrl != null -> LottieCompositionSpec.Url(lottieUrl)
                            lottieRawRes != null -> LottieCompositionSpec.RawRes(lottieRawRes)
                            else -> null
                        }
                    }
                    if (spec != null) {
                        val composition by rememberLottieComposition(spec)
                        val anim by animateLottieCompositionAsState(
                            composition = composition,
                            iterations = LottieConstants.IterateForever,
                            isPlaying = true
                        )
                        LottieAnimation(
                            composition = composition,
                            progress = { anim },
                            modifier = Modifier.size(140.dp)
                        )
                        Spacer(Modifier.height(8.dp))
                    }

                    Text(
                        "동영상 업로드 중...",
                        style = chakhaengTypography().titleMedium.copy(fontWeight = FontWeight.SemiBold)
                    )
                    Spacer(Modifier.height(8.dp))
                    if (progress != null && !progress.isNaN()) {
                        LinearProgressIndicator(
                            progress = { progress.coerceIn(0f, 1f) },
                            modifier = Modifier.fillMaxWidth(),
                            color = primaryLight
                        )
                        Spacer(Modifier.height(6.dp))
                        Text("${(progress * 100).roundToInt()}%")
                    } else {
                        LinearProgressIndicator(modifier = Modifier.fillMaxWidth())
                    }

                }
            }
        }
    }
}