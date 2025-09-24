package com.sos.chakhaeng.presentation.ui.components.detection

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.sos.chakhaeng.domain.model.ViolationType
import com.sos.chakhaeng.domain.model.violation.ViolationInRangeEntity
import com.sos.chakhaeng.presentation.theme.backgroundLight

@Composable
fun ViolationDetectionSection(
    selectedFilter: ViolationType,
    violations: List<ViolationInRangeEntity>,
    onFilterSelected: (ViolationType) -> Unit,
    onViolationClick: (ViolationInRangeEntity) -> Unit,
    modifier: Modifier = Modifier,
    isActive: Boolean
) {
    Column(
        modifier = modifier.background(backgroundLight),
    ) {

        ViolationFilterChips(
            selectedFilter = selectedFilter,
            onFilterSelected = onFilterSelected,
            modifier = Modifier
                .fillMaxWidth()
                .background(Color.White)
                .padding(16.dp)
        )

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Column(modifier = Modifier
                .fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ){
                    Text(
                        "위반 감지 목록",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                    )

                    Text(
                        "${violations.size}/100",
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.Bold,
                    )
                }
                Spacer(modifier = Modifier.height(8.dp))
                if(isActive) {
                    Text(
                        "최대 100개까지 저장되며, 오래된 순서대로 자동 삭제됩니다",
                        style = MaterialTheme.typography.bodySmall
                    )
                }
                else{
                    Text(
                        "AI가 탐지를 시작하면, 감지 목록을 보여줍니다.",
                        style = MaterialTheme.typography.bodySmall
                    )
                }
                Spacer(modifier = Modifier.height(8.dp))
            }

            ViolationDetectionList(
                violations = violations,
                onViolationClick = onViolationClick,
                modifier = Modifier.weight(1f)
            )
        }
    }
}