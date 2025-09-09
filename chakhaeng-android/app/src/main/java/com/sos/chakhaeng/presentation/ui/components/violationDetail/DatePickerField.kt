@file:OptIn(ExperimentalMaterial3Api::class)

package com.sos.chakhaeng.presentation.ui.components.violationDetail

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.CalendarMonth
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.maxkeppeker.sheets.core.models.base.rememberUseCaseState
import com.maxkeppeler.sheets.calendar.CalendarDialog
import com.maxkeppeler.sheets.calendar.CalendarView
import com.sos.chakhaeng.R
import com.maxkeppeler.sheets.calendar.models.CalendarConfig
import com.maxkeppeler.sheets.calendar.models.CalendarSelection
import com.sos.chakhaeng.presentation.theme.chakhaengTypography
import com.sos.chakhaeng.presentation.theme.onSurfaceLight
import com.sos.chakhaeng.presentation.theme.onSurfaceVariantLight
import com.sos.chakhaeng.presentation.theme.outlineVariantLight
import com.sos.chakhaeng.presentation.theme.primaryLight
import com.sos.chakhaeng.presentation.theme.surfaceLight
import com.sos.chakhaeng.presentation.theme.surfaceVariantLight
import java.time.LocalDate
import java.time.format.DateTimeFormatter

@Composable
fun DatePickerField(
    label: String = "발생 일자",
    value: String,                 // "yyyy-MM-dd"
    isEditing: Boolean,
    onDateChange: (String) -> Unit,
    modifier: Modifier = Modifier,
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

    // 날짜 파싱/포맷터
    val formatter = remember { DateTimeFormatter.ISO_LOCAL_DATE }
    val currentDate = remember(value) {
        value.safeParseLocalDate(formatter)
    }

    // 바텀시트 열림 상태 + SheetState
    var openSheet by remember { mutableStateOf(false) }
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    // UseCaseState: View(임베디드)로 사용
    val useCaseState = rememberUseCaseState(
        embedded = true,
        onCloseRequest = { openSheet = false }
    )

    // 입력 상자 (읽기 전용)
    ElevatedCard(modifier = modifier.fillMaxWidth()) {
        Column(Modifier.fillMaxWidth().padding(16.dp)) {
            Text(label, style = chakhaengTypography().bodyMedium,
                fontWeight = FontWeight.SemiBold,
                color = onSurfaceVariantLight
            )
            Spacer(Modifier.height(8.dp))

            Box {
                OutlinedTextField(
                    value = if (!isEditing && value.isBlank()) "" else value,
                    onValueChange = {},
                    readOnly = true,
                    enabled = isEditing,
                    modifier = Modifier.fillMaxWidth(),
                    placeholder = { if (!isEditing && value.isBlank()) Text("—") },
                    trailingIcon = {
                        IconButton(enabled = isEditing, onClick = { openSheet = true }) {
                            Icon(painterResource(R.drawable.ic_calendar), contentDescription = null)
                        }
                    },
                    colors = fieldColors
                )
                if (isEditing) Box(
                    Modifier.matchParentSize().clickable { openSheet = true }
                )
            }
        }
    }

    // ✅ 바텀시트로 띄우는 캘린더
    if (openSheet) {
        ModalBottomSheet(
            onDismissRequest = { openSheet = false },
            sheetState = sheetState
        ) {
            // Sheets-Compose-Dialogs 의 View 버전 사용
            CalendarView(
                useCaseState = useCaseState,
                selection = CalendarSelection.Date(selectedDate = currentDate) { picked ->
                    onDateChange(picked.format(formatter))
                    openSheet = false
                },
                config = CalendarConfig(monthSelection = true, yearSelection = true),
                // header = ... (필요시)
            )
            Spacer(Modifier.height(16.dp))
        }
    }
}

// ---------- helpers ----------
private fun String?.safeParseLocalDate(
    formatter: java.time.format.DateTimeFormatter
): java.time.LocalDate {
    if (this.isNullOrBlank()) return java.time.LocalDate.now()
    return runCatching { java.time.LocalDate.parse(this, formatter) }
        .getOrElse { java.time.LocalDate.now() }
}
