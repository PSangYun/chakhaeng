package com.sos.chakhaeng.presentation.ui.components.violationDetail

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import com.sos.chakhaeng.presentation.theme.chakhaengTypography
import com.sos.chakhaeng.presentation.theme.onSurfaceLight
import com.sos.chakhaeng.presentation.theme.outlineVariantLight
import com.sos.chakhaeng.presentation.theme.primaryLight
import com.sos.chakhaeng.presentation.theme.surfaceLight
import com.sos.chakhaeng.presentation.theme.surfaceVariantLight

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
    val fieldColors = TextFieldDefaults.colors(
        focusedContainerColor = if(isEditing)  surfaceLight else surfaceVariantLight,
        unfocusedContainerColor = if(isEditing)  surfaceLight else surfaceVariantLight,
        disabledContainerColor = if(isEditing)  surfaceLight else surfaceVariantLight,
        focusedIndicatorColor = primaryLight,
        unfocusedIndicatorColor = outlineVariantLight,
//        disabledIndicatorColor = primaryLight,
        disabledTextColor = onSurfaceLight,
        disabledPlaceholderColor = onSurfaceLight,
//        cursorColor = MaterialTheme.colorScheme.primary
    )
    ElevatedCard(modifier = modifier.fillMaxWidth()) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Text(
                text = label,
                style = chakhaengTypography().bodyMedium,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )


            Spacer(Modifier.height(8.dp))

                OutlinedTextField(
                    value = value,
                    onValueChange = onValueChange,
                    enabled = isEditing,
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = singleLine,
                    minLines = minLines,
                    visualTransformation = visualTransformation,
                    placeholder = {
                        if (placeholder != null) Text(placeholder)
                    },
                    colors = fieldColors,
                )
        }
    }
}
