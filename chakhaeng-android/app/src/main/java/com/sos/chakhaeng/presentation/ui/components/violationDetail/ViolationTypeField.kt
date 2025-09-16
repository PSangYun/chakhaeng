package com.sos.chakhaeng.presentation.ui.components.violationDetail

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.LocalPolice
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.sos.chakhaeng.presentation.theme.NEUTRAL100
import com.sos.chakhaeng.presentation.theme.NEUTRAL200
import com.sos.chakhaeng.presentation.theme.NEUTRAL400
import com.sos.chakhaeng.presentation.theme.NEUTRAL800
import com.sos.chakhaeng.presentation.theme.chakhaengTypography
import com.sos.chakhaeng.presentation.theme.primaryLight
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
    val current = options.firstOrNull { it == value } ?: "위반"

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
                        style = chakhaengTypography().titleSmall,
                        fontWeight = FontWeight.SemiBold,
                        color = NEUTRAL800
                    )
                    Text(
                        text = helper,
                        style = chakhaengTypography().bodySmall,
                        color = NEUTRAL200
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
                            //.clip(RoundedCornerShape(18.dp))
                            .semantics { contentDescription = "신고 유형 선택" },
                        value = current,
                        onValueChange = {},
                        readOnly = true,
                        enabled = isEditing,
                        trailingIcon = {
                            ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded)
                        },
                        colors = fieldColors,
                        singleLine = true
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
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .border(1.dp, NEUTRAL200, shape = RoundedCornerShape(6.dp))
                ) {
                    Text(
                        text = current,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 12.dp),
                        style = chakhaengTypography().bodyLarge,
                        color = NEUTRAL800
                    )
                }
            }
        }
    }
}

