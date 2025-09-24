package com.ssafy.chakeng.stats.dto;

public record ReportStatusStatistic(
        String status, // "처리중", "완료", "반려" 등
        int count
) {}
