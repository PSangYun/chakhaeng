package com.sos.chakhaeng.presentation.ui.components.map

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Canvas
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import com.google.android.gms.maps.model.BitmapDescriptor
import com.google.android.gms.maps.model.BitmapDescriptorFactory
import com.google.android.gms.maps.model.CameraPosition
import com.google.maps.android.compose.*
import com.sos.chakhaeng.R
import com.sos.chakhaeng.data.mapper.LocationMapper
import com.sos.chakhaeng.domain.model.report.ReportDetailItem
import com.sos.chakhaeng.presentation.theme.chakhaengTypography
import com.sos.chakhaeng.presentation.ui.screen.reportdetail.ReportDetailUiState

@Composable
fun MapComponent(
    reportDetailItem: ReportDetailItem,
    uiState: ReportDetailUiState,
    onLocationRequest: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val mapPosition = LocationMapper.toLatLng(uiState.mapLocation)
    var markerIcon by remember { mutableStateOf<BitmapDescriptor?>(null) }

    // 마커 아이콘 초기화
    LaunchedEffect(Unit) {
        markerIcon = try {
            createSizedMarker(context, 80) ?: BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_RED)
        } catch (e: Exception) {
            BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_RED)
        }
    }

    LaunchedEffect(reportDetailItem.location) {
        if (reportDetailItem.location.isNotEmpty()) {
            onLocationRequest(reportDetailItem.location)
        }
    }

    val cameraPositionState = rememberCameraPositionState {
        position = CameraPosition.fromLatLngZoom(mapPosition, 15f)
    }

    // 마커 위치가 변경되면 카메라도 이동 (DEFAULT 위치가 아닌 경우에만)
    LaunchedEffect(uiState.mapLocation) {
        if (!uiState.mapLocation.isDefault()) {
            val newPosition = LocationMapper.toLatLng(uiState.mapLocation)
            cameraPositionState.move(
                com.google.android.gms.maps.CameraUpdateFactory.newLatLngZoom(newPosition, 15f)
            )
        }
    }

    Column(
        modifier = modifier.fillMaxWidth()
    ) {
        // Google Map 영역
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .height(250.dp),
            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
        ) {
            Box(
                modifier = Modifier.fillMaxSize()
            ) {
                GoogleMap(
                    modifier = Modifier
                        .fillMaxSize()
                        .clip(RoundedCornerShape(8.dp)),
                    cameraPositionState = cameraPositionState,
                    properties = MapProperties(
                        isMyLocationEnabled = false,
                        mapType = MapType.NORMAL
                    ),
                    uiSettings = MapUiSettings(
                        zoomControlsEnabled = true,
                        zoomGesturesEnabled = true,
                        scrollGesturesEnabled = true,
                        rotationGesturesEnabled = false,
                        tiltGesturesEnabled = false,
                        compassEnabled = true,
                        mapToolbarEnabled = false
                    )
                ) {
                    // 마커 아이콘이 준비된 후에만 마커 표시
                    markerIcon?.let { icon ->
                        Marker(
                            state = MarkerState(position = mapPosition),
                            title = "🚨 위반 발생 지점",
                            snippet = buildString {
                                append("위치: ${reportDetailItem.location}")
                            },
                            icon = icon,
                            onClick = {
                                false
                            }
                        )
                    }
                }

                // 로딩 오버레이
                if (uiState.isMapLoading) {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .clip(RoundedCornerShape(8.dp)),
                        contentAlignment = Alignment.Center
                    ) {
                        Card(
                            colors = CardDefaults.cardColors(
                                containerColor = Color.Black.copy(alpha = 0.7f)
                            )
                        ) {
                            Row(
                                modifier = Modifier.padding(16.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                CircularProgressIndicator(
                                    modifier = Modifier.size(20.dp),
                                    color = Color.White,
                                    strokeWidth = 2.dp
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = "지도 로딩 중...",
                                    color = Color.White,
                                    style = chakhaengTypography().bodySmall
                                )
                            }
                        }
                    }
                }

                // 에러 오버레이
                if (uiState.mapError != null) {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .clip(RoundedCornerShape(8.dp)),
                        contentAlignment = Alignment.Center
                    ) {
                        Card(
                            colors = CardDefaults.cardColors(
                                containerColor = MaterialTheme.colorScheme.errorContainer
                            )
                        ) {
                            Text(
                                text = uiState.mapError,
                                modifier = Modifier.padding(16.dp),
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onErrorContainer
                            )
                        }
                    }
                }
            }
        }

    }
}

private fun createSizedMarker(context: Context, size: Int): BitmapDescriptor? {
    return try {
        val vectorDrawable = ContextCompat.getDrawable(context, R.drawable.ic_report_camera)
            ?: return null

        // 크기 조절을 위한 비트맵 생성
        val bitmap = Bitmap.createBitmap(size, size, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(bitmap)

        // 크기 설정 및 그리기
        vectorDrawable.setBounds(0, 0, size, size)
        vectorDrawable.draw(canvas)

        BitmapDescriptorFactory.fromBitmap(bitmap)
    } catch (e: Exception) {
        null
    }
}