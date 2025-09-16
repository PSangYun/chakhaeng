package com.sos.chakhaeng.presentation.ui.screen.violationDetail

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.calculateEndPadding
import androidx.compose.foundation.layout.calculateStartPadding
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.sos.chakhaeng.R
import com.sos.chakhaeng.core.utils.rememberVideoPicker
import com.sos.chakhaeng.core.utils.showToast
import com.sos.chakhaeng.presentation.theme.BLUE50
import com.sos.chakhaeng.presentation.theme.NEUTRAL800
import com.sos.chakhaeng.presentation.theme.chakhaengTypography
import com.sos.chakhaeng.presentation.theme.primaryLight
import com.sos.chakhaeng.presentation.ui.components.UploadingOverlay
import com.sos.chakhaeng.presentation.ui.components.violationDetail.DatePickerField
import com.sos.chakhaeng.presentation.ui.components.violationDetail.TimePickerField
import com.sos.chakhaeng.presentation.ui.components.violationDetail.ViolationDetailTopBar
import com.sos.chakhaeng.presentation.ui.components.violationDetail.ViolationInfoItem
import com.sos.chakhaeng.presentation.ui.components.violationDetail.ViolationMediaSection
import com.sos.chakhaeng.presentation.ui.components.violationDetail.ViolationTypeField
import com.sos.chakhaeng.presentation.ui.components.violationDetail.ViolationVideoPlayerDialog

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ViolationDetailScreen(
    viewModel: ViolationDetailViewModel = hiltViewModel(),
    onBack: () -> Unit,
    paddingVaules: PaddingValues
) {
    val context = LocalContext.current
    val state by viewModel.uiState.collectAsStateWithLifecycle()

    val entity = state.violationDetail
    val snackBarHostState = remember { SnackbarHostState() }
    var videoDialogVisible by remember { mutableStateOf(false) }

    val scrollBehavior = TopAppBarDefaults.pinnedScrollBehavior()

    val openVideoPicker = rememberVideoPicker { uri ->
        viewModel.onVideoSelected(uri)     // 업로드/교체 로직으로 연결
    }
    LaunchedEffect(Unit) {
        viewModel.event.collect { message ->
            context.showToast(message)
        }
    }
    Scaffold(
        modifier = Modifier
            .fillMaxSize()
            .background(color = Color.White)
            .nestedScroll(scrollBehavior.nestedScrollConnection), // 옵션
        topBar = {
            ViolationDetailTopBar(
                isEditing = state.isEditing,
                onBackClick = onBack,
                onToggleEdit = { viewModel.toggleEdit(onSave = { /* TODO */ }) }
            )
        },
        snackbarHost = { SnackbarHost(snackBarHostState) }
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
                    .background(color = Color.White)
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp),
            ) {

                ElevatedCard(
                    modifier = Modifier
                        .fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.elevatedCardColors(
                        containerColor = BLUE50
                    ),
                    elevation = CardDefaults.elevatedCardElevation(defaultElevation = 0.dp)
                ) {
                    Column(
                        modifier = Modifier.padding(horizontal = 16.dp, vertical = 14.dp),
                        verticalArrangement = Arrangement.Center
                    ) {
                        Row(Modifier,
                            verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                painter = painterResource(R.drawable.ic_ai),
                                contentDescription = null,
                                tint = primaryLight,
                                modifier = Modifier
                                    .size(40.dp)            // 배지 크기
                                    .padding(8.dp)          // 내부 여백
                            )

                            Spacer(Modifier.width(4.dp))
                            Text(
                                text = "AI 자동 생성 완료",
                                style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.SemiBold),
                                color = NEUTRAL800
                            )

                        }
                        Spacer(Modifier.height(4.dp))
                        Text(
                            text = "AI가 위반 상황을 분석하여 신고서를 자동 작성했습니다. 각 항목을 확인하고 필요시 수정하세요.",
                            style = MaterialTheme.typography.bodyMedium,
                            color = NEUTRAL800.copy(alpha = 0.8f),
                            lineHeight = 20.sp
                        )
                    }
                }

                ViolationMediaSection(
//                    videoUrl = "https://test-streams.mux.dev/x36xhzz/x36xhzz.m3u8",
                    videoUrl = state.lastUploaded?.downloadUrl.orEmpty(),
                    onRequestUpload = { openVideoPicker() },
                    onRequestEdit = { openVideoPicker() },
                    onRequestDelete = { }
                )

                // 이하 공통 카드들
                ViolationTypeField(
                    value = entity.violationType,
                    isEditing = state.isEditing,
                    onValueChange = viewModel::updateViolationType
                )
                ViolationInfoItem(
                    "신고 발생 지역",
                    entity.location,
                    state.isEditing,
                    viewModel::updateLocation,
                    placeholder = "도로명 주소 또는 지점 설명"
                )
                ViolationInfoItem(
                    "제목",
                    entity.title,
                    state.isEditing,
                    viewModel::updateTitle,
                    placeholder = "예: 강남대로 522에서 신호위반"
                )
                ViolationInfoItem(
                    "신고 내용", entity.description, state.isEditing, viewModel::updateDescription,
                    placeholder = "상세한 상황 설명을 입력하세요", singleLine = false, minLines = 5
                )
                ViolationInfoItem(
                    "차량 번호",
                    entity.plateNumber,
                    state.isEditing,
                    viewModel::updatePlateNumber,
                    placeholder = "예: 12가1234"
                )
                DatePickerField(
                    value = entity.date,
                    isEditing = state.isEditing,
                    onDateChange = viewModel::updateDate,
                )
                TimePickerField(
                    value = entity.time,
                    isEditing = state.isEditing,
                    onTimeChange = viewModel::updateTime
                )

                Spacer(Modifier.height(8.dp))
                Button(
                    onClick = { viewModel.onSubmit() },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(64.dp),
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
                    )
                }
                Spacer(Modifier.height(16.dp))

            }
        }

        UploadingOverlay(
            visible = state.isUploading,
            progress = state.uploadProgress.takeIf { !it.isNaN() },
            lottieUrl = "https://lottie.host/3828abb2-6b0d-40c4-9878-435899ab26fa/q4i8TQV1qV.json"
        )
    }

    if (videoDialogVisible && entity.videoUrl.isNotBlank()) {
        ViolationVideoPlayerDialog(
            url = entity.videoUrl,
            onDismiss = { videoDialogVisible = false }
        )
    }
}