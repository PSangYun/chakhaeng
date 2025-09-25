package com.sos.chakhaeng.presentation.ui.components.report

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.sos.chakhaeng.domain.model.report.ReportDetailItem
import com.sos.chakhaeng.presentation.theme.chakhaengTypography
import com.sos.chakhaeng.presentation.theme.errorContainerLight
import com.sos.chakhaeng.presentation.theme.errorLight
import com.sos.chakhaeng.presentation.ui.components.map.MapComponent
import com.sos.chakhaeng.presentation.ui.components.map.MapComponentForReportDetail
import com.sos.chakhaeng.presentation.ui.screen.detectionDetail.DetectionDetailUiState
import com.sos.chakhaeng.presentation.ui.screen.reportdetail.ReportDetailUiState

@Composable
fun LocationInfoSection (
    reportDetailItem: ReportDetailItem,
    detectionDetailUiState: DetectionDetailUiState,
    onLocationRequest: (String) -> Unit
){
    Card(
        modifier = Modifier
            .fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = Color.White
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
        ) {
            Text(
                text = "위반 발생 지점",
                style = chakhaengTypography().titleSmall,
                fontWeight = FontWeight.Bold
            )

            Spacer(modifier = Modifier.padding(8.dp))

            MapComponent(
                reportDetailItem = reportDetailItem,
                uiState = detectionDetailUiState,
                onLocationRequest = onLocationRequest,
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.padding(8.dp))

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(
                        color = errorContainerLight,
                        shape = RoundedCornerShape(12.dp) // Rounded shape 적용
                    )
                    .padding(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {

                Icon(
                    imageVector = Icons.Default.LocationOn,
                    contentDescription = null,
                    tint = errorLight,
                    modifier = Modifier.size(20.dp)
                )

                Text(
                    text = "위 지도에서 정확한 위치를 확인하세요",
                    style = chakhaengTypography().bodySmall,
                    color = errorLight,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(start = 4.dp)
                )

            }
        }
    }
}

@Composable
fun LocationInfoSectionForReportDetail (
    reportDetailItem: ReportDetailItem,
    reportDetailUiState: ReportDetailUiState,
    onLocationRequest: (String) -> Unit
){
    Card(
        modifier = Modifier
            .fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = Color.White
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
        ) {
            Text(
                text = "위반 발생 지점",
                style = chakhaengTypography().titleSmall,
                fontWeight = FontWeight.Bold
            )

            Spacer(modifier = Modifier.padding(8.dp))

            MapComponentForReportDetail(
                reportDetailItem = reportDetailItem,
                uiState = reportDetailUiState,
                onLocationRequest = onLocationRequest,
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.padding(8.dp))

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(
                        color = errorContainerLight,
                        shape = RoundedCornerShape(12.dp) // Rounded shape 적용
                    )
                    .padding(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {

                Icon(
                    imageVector = Icons.Default.LocationOn,
                    contentDescription = null,
                    tint = errorLight,
                    modifier = Modifier.size(20.dp)
                )

                Text(
                    text = "위 지도에서 정확한 위치를 확인하세요",
                    style = chakhaengTypography().bodySmall,
                    color = errorLight,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(start = 4.dp)
                )

            }
        }
    }
}