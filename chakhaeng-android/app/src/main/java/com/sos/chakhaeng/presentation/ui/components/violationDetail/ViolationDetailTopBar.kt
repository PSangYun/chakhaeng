package com.sos.chakhaeng.presentation.ui.components.violationDetail

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.Composable

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ViolationDetailTopBar(
    isEditing: Boolean,
    onBackClick: () -> Unit,
    onToggleEdit: () -> Unit
) {
    CenterAlignedTopAppBar(
        title = { Text("위반 상세정보") },
        navigationIcon = {
            IconButton(onClick = onBackClick) {
                Icon(Icons.AutoMirrored.Outlined.ArrowBack, contentDescription = "뒤로")
            }
        },
        actions = {
            TextButton(onClick = onToggleEdit) {
                Text(if (isEditing) "완료" else "수정")
            }
        }
    )
}
