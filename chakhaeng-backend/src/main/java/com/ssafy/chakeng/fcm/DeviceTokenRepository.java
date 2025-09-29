package com.ssafy.chakeng.fcm;

import com.ssafy.chakeng.fcm.domain.DeviceToken;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface DeviceTokenRepository extends JpaRepository<DeviceToken, UUID> {
    List<DeviceToken> findAllByUser_IdAndActiveTrue(UUID userId);
    Optional<DeviceToken> findByToken(String token);
}
