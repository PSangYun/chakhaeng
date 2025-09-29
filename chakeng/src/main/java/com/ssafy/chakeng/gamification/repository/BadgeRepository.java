package com.ssafy.chakeng.gamification.repository;

import com.ssafy.chakeng.gamification.domain.Badge;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface BadgeRepository extends JpaRepository<Badge, String> {
    List<Badge> findByUserId(UUID userId);
}
