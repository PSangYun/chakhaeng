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
import com.maxkeppeler.sheets.calendar.CalendarView
import com.maxkeppeler.sheets.calendar.models.CalendarConfig
import com.maxkeppeler.sheets.calendar.models.CalendarSelection
import com.sos.chakhaeng.R
import com.sos.chakhaeng.presentation.theme.NEUTRAL200
import com.sos.chakhaeng.presentation.theme.NEUTRAL800
import com.sos.chakhaeng.presentation.theme.chakhaengTypography
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
                    value = if (!isEditing && value.isBlank()) "" else value,
                    onValueChange = {},
                    readOnly = true,
                    enabled = isEditing,
                    modifier = Modifier.fillMaxWidth(),
                    placeholder = { if (!isEditing && value.isBlank()) Text("발생 날짜를 알려주세요") },
                    trailingIcon = {
                        IconButton(enabled = isEditing, onClick = { openSheet = true }) {
                            Icon(painterResource(R.drawable.ic_calendar), contentDescription = null)
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
