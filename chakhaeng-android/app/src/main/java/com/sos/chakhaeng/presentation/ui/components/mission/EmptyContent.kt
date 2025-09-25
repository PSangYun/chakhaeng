package com.sos.chakhaeng.presentation.ui.components.mission

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.sos.chakhaeng.presentation.theme.chakhaengTypography
import com.sos.chakhaeng.presentation.theme.onSurfaceVariantLight

@Composable
fun EmptyContent() {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Text(
                text = "진행 중인 미션이 없습니다",
                style = chakhaengTypography().titleMedium,
                color = onSurfaceVariantLight
            )
            Text(
                text = "새로운 미션이 곧 추가될 예정입니다!",
                style = chakhaengTypography().bodyMedium,
                color = onSurfaceVariantLight
            )
        }
    }
}