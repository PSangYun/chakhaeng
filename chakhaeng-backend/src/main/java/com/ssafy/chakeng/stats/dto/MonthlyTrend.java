package com.ssafy.chakeng.stats.dto;

public record MonthlyTrend(
        String month,                  // e.g. "2025-09"
        int count,
        int changeFromPreviousMonth
) {}
