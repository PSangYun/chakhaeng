package com.sos.chakhaeng.domain.model.report

import androidx.compose.ui.graphics.Color
import com.sos.chakhaeng.presentation.theme.ReportStatusColors

enum class ReportStatus(
    val displayName: String,
    val backgroundColor: Color,
    val textColor: Color
) {
    PROCESSING("처리중", ReportStatusColors.Processing, ReportStatusColors.ProcessingText),
    COMPLETED("완료", ReportStatusColors.Completed, ReportStatusColors.CompletedText),
    REJECTED("반려", ReportStatusColors.Rejected, ReportStatusColors.RejectedText)
}