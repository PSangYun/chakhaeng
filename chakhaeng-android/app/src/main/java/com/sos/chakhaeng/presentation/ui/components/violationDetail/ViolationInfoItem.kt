package com.sos.chakhaeng.presentation.ui.components.violationDetail

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp

/**
 * 텍스트성 항목을 모두 커버하는 공통 카드.
 * - isEditing=true면 OutlinedTextField, 아니면 Text로 표시
 * - singleLine, minLines로 단문/장문 모두 대응(신고 내용만 minLines=5 등)
 */
@Composable
fun ViolationInfoItem(
    label: String,
    value: String,
    isEditing: Boolean,
    onValueChange: (String) -> Unit,
    modifier: Modifier = Modifier,
    placeholder: String? = null,
    singleLine: Boolean = true,
    minLines: Int = 1,
    visualTransformation: VisualTransformation = VisualTransformation.None,
    trailingContent: (@Composable () -> Unit)? = null, // 필요시 오른쪽 보조 UI
) {
    ElevatedCard(modifier = modifier.fillMaxWidth()) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Text(
                text = label,
                style = MaterialTheme.typography.labelLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(Modifier.height(8.dp))

            if (isEditing) {
                OutlinedTextField(
                    value = value,
                    onValueChange = onValueChange,
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = singleLine,
                    minLines = minLines,
                    visualTransformation = visualTransformation,
                    placeholder = {
                        if (placeholder != null) Text(placeholder)
                    }
                )
            } else {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = value.ifBlank { "—" },
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.onSurface,
                        modifier = Modifier.weight(1f)
                    )
                    if (trailingContent != null) {
                        Spacer(Modifier.width(8.dp))
                        trailingContent()
                    }
                }
            }
        }
    }
}
