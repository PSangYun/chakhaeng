package com.sos.chakhaeng.presentation.ui.components.allbadges

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.sos.chakhaeng.presentation.theme.chakhaengTypography
import com.sos.chakhaeng.presentation.theme.errorLight
import com.sos.chakhaeng.presentation.theme.onSurfaceVariantLight

@Composable
fun ErrorContent(
    error: String,
    onRetry: () -> Unit
) {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Text(
                text = "오류가 발생했습니다",
                style = chakhaengTypography().titleMedium,
                color = errorLight
            )
            Text(
                text = error,
                style = chakhaengTypography().bodyMedium,
                color = onSurfaceVariantLight
            )
            Button(onClick = onRetry) {
                Text("다시 시도")
            }
        }
    }
}