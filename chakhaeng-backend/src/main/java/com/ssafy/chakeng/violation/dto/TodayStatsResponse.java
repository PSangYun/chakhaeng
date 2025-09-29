package com.ssafy.chakeng.violation.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class TodayStatsResponse {
    private long todayDetected;
    private long todayReported;
}
