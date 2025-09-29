package com.ssafy.chakeng.stats.dto;

public record HourlyStatistic(
        int hour,                      // 0-23
        int count
) {}
