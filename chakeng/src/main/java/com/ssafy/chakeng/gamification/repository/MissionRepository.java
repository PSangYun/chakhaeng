package com.ssafy.chakeng.gamification.repository;

import com.ssafy.chakeng.gamification.domain.Mission;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface MissionRepository extends JpaRepository<Mission, String> {
    List<Mission> findByUserId(UUID userId);
}