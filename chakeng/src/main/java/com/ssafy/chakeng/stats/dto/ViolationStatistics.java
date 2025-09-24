package com.ssafy.chakeng.stats.dto;

import java.util.List;

public record ViolationStatistics(
        int totalDetections,
        int detectionAccuracy,         // %
        int weeklyDetections,
        double dailyAverageDetections,
        List<ViolationTypeStatistic> violationTypeStats,
        List<HourlyStatistic> hourlyStats,
        List<MonthlyTrend> monthlyTrend
) {}

