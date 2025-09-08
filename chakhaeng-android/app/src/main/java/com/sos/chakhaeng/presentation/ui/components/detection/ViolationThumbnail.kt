package com.sos.chakhaeng.presentation.ui.components.detection

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.sos.chakhaeng.presentation.ui.model.ViolationDetectionUiModel
import com.sos.chakhaeng.presentation.ui.model.ViolationType
import com.sos.chakhaeng.presentation.ui.theme.primaryLight

@Composable
fun ViolationThumbnail(
    violation: ViolationDetectionUiModel,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(8.dp))
            .background(MaterialTheme.colorScheme.surfaceVariant)
    ) {
        if (violation.hasImage && violation.thumbnailUrl != null) {
            AsyncImage(
                model = violation.thumbnailUrl,
                contentDescription = "위반 상황 썸네일",
                modifier = Modifier.fillMaxSize(),
                contentScale = ContentScale.Crop,
                error = painterResource(android.R.drawable.ic_menu_camera),
                placeholder = painterResource(android.R.drawable.ic_menu_camera)
            )
        } else {
            DefaultViolationThumbnail(
                violationType = violation.type,
                modifier = Modifier.fillMaxSize()
            )
        }
    }
}

@Composable
private fun DefaultViolationThumbnail(
    violationType: ViolationType,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .background(
                color = Color.LightGray,
                shape = RoundedCornerShape(8.dp)
            ),
        contentAlignment = Alignment.Center
    ) {
        Icon(
            imageVector = Icons.Default.PhotoCamera,
            contentDescription = violationType.displayName,
            modifier = Modifier.size(24.dp)
        )
    }
}