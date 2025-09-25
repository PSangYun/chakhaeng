package com.ssafy.chakeng.gamification.dto;

public record BadgeDto(
        String id,
        String name,
        String description,
        String iconUrl,
        boolean isUnlocked
) {}