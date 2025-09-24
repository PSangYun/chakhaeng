package com.sos.chakhaeng.presentation.ui.components.violationDetail

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.sos.chakhaeng.presentation.theme.NEUTRAL200
import com.sos.chakhaeng.presentation.theme.NEUTRAL800
import com.sos.chakhaeng.presentation.theme.chakhaengTypography
import com.sos.chakhaeng.presentation.ui.screen.violationDetail.VIOLATION_TYPE_OPTIONS

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ViolationTypeField(
    modifier: Modifier = Modifier,
    label: String = "자동차·교통 위반 신고 유형",
    helper: String = "가장 적합한 신고 유형을 선택하세요",
    value: String,
    isEditing: Boolean,
    onValueChange: (String) -> Unit,
    options: List<String> = VIOLATION_TYPE_OPTIONS
) {
    var expanded by remember { mutableStateOf(false) }
    val current = options.firstOrNull { it == value } ?: ""

    val fieldColors = TextFieldDefaults.colors(
        focusedContainerColor = Color.White,
        unfocusedContainerColor = Color.White,
        disabledContainerColor =  Color.White,
        focusedIndicatorColor = NEUTRAL200,
        unfocusedIndicatorColor = NEUTRAL200,
        disabledTextColor = NEUTRAL200,
        disabledPlaceholderColor = NEUTRAL200,
        focusedPlaceholderColor =NEUTRAL200 ,
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
            Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(Modifier.weight(1f)) {
                    Text(
                        text = label,
                        style = chakhaengTypography().bodyLarge,
                        fontWeight = FontWeight.SemiBold,
                        color = NEUTRAL800
                    )
                }
            }

            Spacer(Modifier.height(12.dp))

            if (isEditing) {
                ExposedDropdownMenuBox(
                    expanded = expanded,
                    onExpandedChange = { if (isEditing) expanded = !expanded },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    OutlinedTextField(
                        modifier = Modifier
                            .menuAnchor()
                            .fillMaxWidth()
                            .semantics { contentDescription = "신고 유형 선택" },
                        value = current,
                        onValueChange = {},
                        readOnly = true,
                        enabled = isEditing,
                        trailingIcon = {
                            ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded)
                        },
                        colors = fieldColors,
                        singleLine = true,
                        placeholder = { if(value.isBlank()) Text(helper) },
                    )

                    ExposedDropdownMenu(
                        expanded = expanded,
                        onDismissRequest = { expanded = false }) {
                        VIOLATION_TYPE_OPTIONS.forEach { option ->
                            DropdownMenuItem(
                                text = { Text(option) },
                                onClick = {
                                    onValueChange(option)
                                    expanded = false
                                })
                        }
                    }
                }
            } else {
                OutlinedTextField(
                    modifier = Modifier
                        .fillMaxWidth()
                        .semantics { contentDescription = "신고 유형 선택" },
                    value = current,
                    onValueChange = {},
                    readOnly = true,
                    enabled = false,
                    colors = fieldColors,
                    singleLine = true,
                    placeholder = { if(value.isBlank()) Text(helper) },
                )
            }
        }
    }
}

