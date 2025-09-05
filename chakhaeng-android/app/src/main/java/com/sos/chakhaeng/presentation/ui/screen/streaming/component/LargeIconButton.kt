package com.sos.chakhaeng.presentation.ui.screen.streaming.component

import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.unit.dp

@Composable
fun LargeIconButton(
    onClick: () -> Unit,
    icon: ImageVector,
    contentDesc: String,
    emphasized: Boolean = false
) {
    val boxSize = if (emphasized) 96.dp else 72.dp
    val iconSize = if (emphasized) 56.dp else 40.dp
    Box(
        modifier = Modifier
            .size(boxSize)
            .padding(4.dp)
            .pointerInput(Unit) { detectTapGestures(onTap = { onClick() }) },
        contentAlignment = Alignment.Center
    ) {
        Surface(
            shape = MaterialTheme.shapes.extraLarge,
            color = Color(0x33000000),
            tonalElevation = 0.dp,
            shadowElevation = 0.dp,
            modifier = Modifier.fillMaxSize()
        ) {}
        Icon(icon, contentDescription = contentDesc, tint = Color.White, modifier = Modifier.size(iconSize))
    }
}