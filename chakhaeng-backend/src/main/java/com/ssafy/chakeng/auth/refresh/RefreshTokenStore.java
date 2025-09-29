package com.ssafy.chakeng.auth.refresh;

import java.time.Duration;
import java.util.UUID;

public interface RefreshTokenStore {
    void save(UUID userId, String refresh, Duration ttl);
    boolean isValid(UUID userId, String refresh);
    void revoke(UUID userId, String refresh);
}
