package com.sos.chakhaeng.presentation.ui.screen.violationDetail

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.sos.chakhaeng.R
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.sos.chakhaeng.presentation.ui.components.violationDetail.DatePickerField
import com.sos.chakhaeng.presentation.ui.components.violationDetail.TimePickerField
import com.sos.chakhaeng.presentation.ui.components.violationDetail.ViolationDetailTopBar
import com.sos.chakhaeng.presentation.ui.components.violationDetail.ViolationInfoItem
import com.sos.chakhaeng.presentation.ui.components.violationDetail.ViolationMediaSection
import com.sos.chakhaeng.presentation.ui.components.violationDetail.ViolationTypeField
import com.sos.chakhaeng.presentation.ui.components.violationDetail.ViolationVideoPlayerDialog
import com.sos.chakhaeng.presentation.ui.theme.chakhaengTypography
import com.sos.chakhaeng.presentation.ui.theme.onPrimaryContainerLight
import com.sos.chakhaeng.presentation.ui.theme.primaryLight

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ViolationDetailScreen(
    viewModel: ViolationDetailViewModel = hiltViewModel(),
    onBack: () -> Unit,
    onSubmitToGovernment: (ViolationDetailUiState) -> Unit,
    paddingVaules: PaddingValues
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()
    var videoDialogVisible by remember { mutableStateOf(false) }

    val scrollBehavior = TopAppBarDefaults.pinnedScrollBehavior()

    Scaffold(
        modifier = Modifier
            .fillMaxSize()
            .nestedScroll(scrollBehavior.nestedScrollConnection), // 옵션
        topBar = {
            ViolationDetailTopBar(
                isEditing = state.isEditing,
                onBackClick = onBack,
                onToggleEdit = { viewModel.toggleEdit(onSave = { /* TODO */ }) }
            )
        },
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { innerPadding ->
        val ld = LocalLayoutDirection.current
        val start = paddingVaules.calculateStartPadding(ld)
        val end = paddingVaules.calculateEndPadding(ld)
        val bottom = paddingVaules.calculateBottomPadding()

        Column(
            Modifier
                .padding(start = start, end = end, bottom = bottom)
                .padding(innerPadding)
                .verticalScroll(rememberScrollState())
        ) {

            Column(
                Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp),
            ) {

                ElevatedCard(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.elevatedCardColors(
                        containerColor = primaryLight.copy(alpha = 0.1f) // 은은한 배경 틴트
                    ),
                    elevation = CardDefaults.elevatedCardElevation(defaultElevation = 0.dp)
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 16.dp, vertical = 14.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            painter = painterResource(R.drawable.ic_ai),
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier
                                .size(40.dp)            // 배지 크기
                                .padding(8.dp)          // 내부 여백
                        )

                        Spacer(Modifier.width(12.dp))

                        Column(Modifier.weight(1f)) {
                            Text(
                                text = "AI 자동 생성 완료",
                                style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.SemiBold),
                                color = onPrimaryContainerLight
                            )
                            Spacer(Modifier.height(4.dp))
                            Text(
                                text = "AI가 위반 상황을 분석하여 신고서를 자동 작성했습니다. 각 항목을 확인하고 필요시 수정하세요.",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onPrimaryContainer,
                                lineHeight = 20.sp
                            )
                        }
                    }
                }

                ViolationMediaSection(
                    videoThumbnailUrl = state.videoThumbnailUrl,
                    onPlayVideoClick = {
                        if (state.videoUrl.isNullOrBlank()) {
                            LaunchedEffect(Unit) {
                                snackbarHostState.showSnackbar("동영상 주소를 불러오지 못했습니다.")
                            }
                        } else {
                            videoDialogVisible = true
                        }
                    },
                    photoUrls = state.photoUrls
                )

                // 이하 공통 카드들
                ViolationTypeField(
                    value = state.reportType,
                    isEditing = state.isEditing,
                    onValueChange = viewModel::updateReportType
                )
                ViolationInfoItem("신고 발생 지역", state.region, state.isEditing, viewModel::updateRegion, placeholder = "도로명 주소 또는 지점 설명")
                ViolationInfoItem("제목", state.title, state.isEditing, viewModel::updateTitle, placeholder = "예: 강남대로 522에서 신호위반")
                ViolationInfoItem("신고 내용", state.content, state.isEditing, viewModel::updateContent,
                    placeholder = "상세한 상황 설명을 입력하세요", singleLine = false, minLines = 5)
                ViolationInfoItem("차량 번호", state.carNumber, state.isEditing, viewModel::updateCarNumber, placeholder = "예: 12가1234")
                DatePickerField(
                    value = state.date,
                    isEditing = state.isEditing,
                    onDateChange = viewModel::updateDate,
                )
                TimePickerField(
                    value = state.time,
                    isEditing = state.isEditing,
                    onTimeChange = viewModel::updateTime
                )

                Spacer(Modifier.height(8.dp))
                Button(
                    onClick = { onSubmitToGovernment(state) },
                    modifier = Modifier.fillMaxWidth().height(64.dp),
                    shape = RoundedCornerShape(16.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.error,
                        contentColor = MaterialTheme.colorScheme.onError
                    )
                ) {
                    Icon(
                        painter = painterResource(R.drawable.ic_siren),
                        contentDescription = "국민안전신문고 신고하기",
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(Modifier.width(8.dp))
                    Text(
                    text = "국민안전신문고 신고하기",
                    style = chakhaengTypography().titleSmall,
                    fontWeight = FontWeight.SemiBold
                ) }
                Spacer(Modifier.height(16.dp))

            }
        }
    }



    // ✅ 동영상 재생 다이얼로그
    if (videoDialogVisible && !state.videoUrl.isNullOrBlank()) {
        ViolationVideoPlayerDialog(
            url = state.videoUrl!!,
            onDismiss = { videoDialogVisible = false }
        )
    }
}