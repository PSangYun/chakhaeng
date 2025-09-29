package com.ssafy.chakeng.gamification.dto;

public record MissionDto(
        String id,
        String title,
        String description,
        String iconUrl,
        boolean isCompleted,
        int currentProgress,
        int targetProgress,
        String rewardName
) {}

