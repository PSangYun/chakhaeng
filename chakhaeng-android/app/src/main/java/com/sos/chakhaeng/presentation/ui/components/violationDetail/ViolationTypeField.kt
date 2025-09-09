package com.sos.chakhaeng.presentation.ui.components.violationDetail

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.sos.chakhaeng.presentation.theme.onSurfaceLight
import com.sos.chakhaeng.presentation.theme.outlineVariantLight
import com.sos.chakhaeng.presentation.theme.primaryLight
import com.sos.chakhaeng.presentation.theme.surfaceLight
import com.sos.chakhaeng.presentation.theme.surfaceVariantLight
import com.sos.chakhaeng.presentation.ui.screen.violationDetail.VIOLATION_TYPE_OPTIONS

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ViolationTypeField(
    label: String = "자동차·교통 위반 신고 유형",
    value: String,
    isEditing: Boolean,
    onValueChange: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    var expanded by remember { mutableStateOf(false) }
    val display = VIOLATION_TYPE_OPTIONS.firstOrNull { it == value } ?: "위반"

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
        Column(Modifier.fillMaxWidth().padding(16.dp)) {
            Text(
                text = label,
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(Modifier.height(8.dp))

            ExposedDropdownMenuBox(
                expanded = expanded,
                onExpandedChange = { if (isEditing) expanded = !expanded }
            ) {
                OutlinedTextField(
                    value = display,
                    onValueChange = {},
                    readOnly = true,
                    enabled = isEditing,                 // 보기 모드: 비활성(회색 박스)
                    modifier = Modifier
                        .menuAnchor()
                        .fillMaxWidth(),
                    trailingIcon = {
                        ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded)
                    },
                    colors = fieldColors
                )

                ExposedDropdownMenu(
                    expanded = expanded,
                    onDismissRequest = { expanded = false }
                ) {
                    VIOLATION_TYPE_OPTIONS.forEach { option ->
                        DropdownMenuItem(
                            text = { Text(option) },
                            onClick = {
                                onValueChange(option)
                                expanded = false
                            }
                        )
                    }
                }
            }
        }
    }
}
