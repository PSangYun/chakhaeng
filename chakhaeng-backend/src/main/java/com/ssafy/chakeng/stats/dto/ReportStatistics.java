package com.ssafy.chakeng.stats.dto;

import java.util.List;

public record ReportStatistics(
        int totalReports,
        int completedReports,
        int pendingReports,
        int rejectedReports,
        int successRate,               // %
        int totalSuccessRate,          // 서비스 전체 대비 (필요시)
        List<ReportStatusStatistic> reportStatusStats
) {}

