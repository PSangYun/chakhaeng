package com.ssafy.chakeng.fcm;

import com.ssafy.chakeng.fcm.domain.DeviceToken;
import com.ssafy.chakeng.fcm.dto.RegisterFcmTokenRequest;
import com.ssafy.chakeng.user.UserRepository;
import com.ssafy.chakeng.user.domain.User;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class DeviceTokenService {
    private final DeviceTokenRepository repo;
    private final UserRepository userRepo;

    @Transactional
    public void registerOrUpdate(UUID userId, RegisterFcmTokenRequest req) {
        User user = userRepo.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("User not found"));

        repo.findByToken(req.getToken()).ifPresentOrElse(dt -> {
            dt.setUser(user);
            dt.setPlatform(req.getPlatform());
            dt.setActive(true);
        }, () -> {
            DeviceToken dt = new DeviceToken();
            dt.setUser(user);
            dt.setToken(req.getToken());
            dt.setPlatform(req.getPlatform());
            dt.setActive(true);
            repo.save(dt);
        });
    }

    public List<DeviceToken> activeTokens(UUID userId) {
        return repo.findAllByUser_IdAndActiveTrue(userId);
    }

    @Transactional
    public void deactivateTokens(List<String> invalidTokens) {
        if (invalidTokens == null || invalidTokens.isEmpty()) return;
        invalidTokens.forEach(tok ->
                repo.findByToken(tok).ifPresent(dt -> dt.setActive(false))
        );
    }
}
