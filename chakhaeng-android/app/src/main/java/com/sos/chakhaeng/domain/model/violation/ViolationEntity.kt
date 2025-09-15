package com.sos.chakhaeng.domain.model.violation

data class ViolationEntity (
    val violationType: String = "",   // 자동차·교통 위반 신고 유형
    val location: String = "",       // 신고 발생 지역
    val title: String = "",        // 제목
    val description: String = "",      // 신고 내용(장문)
    val plateNumber: String = "",    // 차량 번호
    val date: String = "",         // 발생 일자 (yyyy-MM-dd)
    val time: String = "",         // 발생 시각 (HH:mm:ss)
//    val videoThumbnailUrl: String? = null,
    val videoUrl: String = ""
)