package com.ssafy.chakeng.stats.dto;

public record ViolationTypeStatistic(
        String violationType,          // enum name을 문자열로
        int count,
        int percentage                 // %
) {}
