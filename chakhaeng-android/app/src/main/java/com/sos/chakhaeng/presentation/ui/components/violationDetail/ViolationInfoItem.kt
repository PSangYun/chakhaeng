package com.sos.chakhaeng.presentation.ui.components.violationDetail

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import com.sos.chakhaeng.presentation.theme.NEUTRAL100
import com.sos.chakhaeng.presentation.theme.NEUTRAL200
import com.sos.chakhaeng.presentation.theme.NEUTRAL800
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
        focusedContainerColor = if (isEditing) NEUTRAL100 else Color.White,
        unfocusedContainerColor = if (isEditing) NEUTRAL100 else Color.White,
        disabledContainerColor = if (isEditing) NEUTRAL100 else Color.White,
        focusedIndicatorColor = NEUTRAL200,
        unfocusedIndicatorColor = NEUTRAL200,
        disabledTextColor = NEUTRAL800,
        disabledPlaceholderColor = NEUTRAL200,
        focusedLabelColor = NEUTRAL800,
        unfocusedLabelColor = NEUTRAL800
    )

    ElevatedCard(
        modifier = modifier
            .fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.elevatedCardColors(containerColor = Color.White),
    ) {
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
