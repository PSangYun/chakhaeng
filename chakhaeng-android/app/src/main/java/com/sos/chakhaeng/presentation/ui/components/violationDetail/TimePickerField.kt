@file:OptIn(ExperimentalMaterial3Api::class)

package com.sos.chakhaeng.presentation.ui.components.violationDetail

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.AccessTime
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.maxkeppeker.sheets.core.models.base.rememberUseCaseState
import com.maxkeppeler.sheets.clock.ClockView
import com.maxkeppeler.sheets.clock.models.ClockConfig
import com.maxkeppeler.sheets.clock.models.ClockSelection
import com.sos.chakhaeng.R
import com.sos.chakhaeng.core.utils.formatKoreanTime
import com.sos.chakhaeng.core.utils.toHourMinute
import com.sos.chakhaeng.presentation.theme.NEUTRAL200
import com.sos.chakhaeng.presentation.theme.NEUTRAL800
import com.sos.chakhaeng.presentation.theme.chakhaengTypography
import com.sos.chakhaeng.presentation.theme.onSurfaceLight
import com.sos.chakhaeng.presentation.theme.onSurfaceVariantLight
import com.sos.chakhaeng.presentation.theme.outlineVariantLight
import com.sos.chakhaeng.presentation.theme.primaryLight
import com.sos.chakhaeng.presentation.theme.surfaceLight
import com.sos.chakhaeng.presentation.theme.surfaceVariantLight
import java.time.LocalTime

@Composable
fun TimePickerField(
    label: String = "발생 시각",
    value: String,
    isEditing: Boolean,
    onTimeChange: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    val fieldColors = TextFieldDefaults.colors(
        focusedContainerColor = Color.White,
        unfocusedContainerColor = Color.White,
        disabledContainerColor = Color.White,
        focusedIndicatorColor = NEUTRAL200,
        unfocusedIndicatorColor = NEUTRAL200,
        disabledTextColor = NEUTRAL800,
        disabledPlaceholderColor = NEUTRAL200,
        focusedLabelColor = NEUTRAL800,
        unfocusedLabelColor = NEUTRAL800
    )

    val display = if (!isEditing && value.isBlank()) "" else formatKoreanTime(value)

    // 초기 시간 파싱
    val (initHour, initMinute) = remember(value) { value.toHourMinute() }
    val initialTime = remember(initHour, initMinute) { LocalTime.of(initHour, initMinute) }

    // 바텀시트 상태
    var openSheet by remember { mutableStateOf(false) }
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    // Sheets-Compose-Dialogs 상태 (embedded 모드)
    val useCaseState = rememberUseCaseState(
        embedded = true,
        onCloseRequest = { openSheet = false }
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
            Text(
                text = label,
                style = chakhaengTypography().bodyLarge,
                fontWeight = FontWeight.SemiBold,
                color = NEUTRAL800
            )
            Spacer(Modifier.height(8.dp))

            Box {
                OutlinedTextField(
                    value = display,
                    onValueChange = {},
                    readOnly = true,
                    enabled = isEditing,
                    modifier = Modifier.fillMaxWidth(),
                    placeholder = { if (!isEditing && value.isBlank()) Text("발생한 시간을 알려주세요") },
                    trailingIcon = {
                        IconButton(enabled = isEditing, onClick = { openSheet = true }) {
                            Icon(painterResource(R.drawable.ic_clock), contentDescription = null)
                        }
                    },
                    colors = fieldColors
                )
                if (isEditing) Box(
                    Modifier
                        .matchParentSize()
                        .clickable { openSheet = true }
                )
            }
        }
    }

    if (openSheet) {
        ModalBottomSheet(
            onDismissRequest = { openSheet = false },
            sheetState = sheetState
        ) {
            ClockView(
                useCaseState = useCaseState,
                selection = ClockSelection.HoursMinutes(
                    onPositiveClick = { hours, minutes ->
                        onTimeChange("%02d:%02d:00".format(hours, minutes))
                        openSheet = false
                    }
                ),
                config = ClockConfig(
                    defaultTime = initialTime,
                    is24HourFormat = true
                )
            )
            Spacer(Modifier.height(16.dp))
        }
    }

}

private fun String.toHourMinute(): Pair<Int, Int> = runCatching {
    val p = split(":")
    (p.getOrNull(0)?.toInt() ?: 0) to (p.getOrNull(1)?.toInt() ?: 0)
}.getOrElse { 0 to 0 }