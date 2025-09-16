package com.sos.chakhaeng.presentation.ui.components.violationDetail

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import com.sos.chakhaeng.presentation.theme.chakhaengTypography

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ViolationDetailTopBar(
    isEditing: Boolean,
    onBackClick: () -> Unit,
    onToggleEdit: () -> Unit
) {
    CenterAlignedTopAppBar(
        title = { Text(
            text = "위반 상세정보",
            style = chakhaengTypography().titleSmall,
            fontWeight = FontWeight.SemiBold
        ) },
        navigationIcon = {
            IconButton(onClick = onBackClick) {
                Icon(Icons.AutoMirrored.Outlined.ArrowBack, contentDescription = "뒤로")
            }
        },
        actions = {
            TextButton(onClick = onToggleEdit) {
                Text(if (isEditing) "완료" else "수정")
            }
        },
        colors = TopAppBarDefaults.topAppBarColors(
            containerColor = Color.White
        )
    )
}
