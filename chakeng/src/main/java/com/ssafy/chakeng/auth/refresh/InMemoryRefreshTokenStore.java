package com.ssafy.chakeng.auth.refresh;

import org.springframework.stereotype.Component;

import java.time.Duration;
import java.time.Instant;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

@Component
public class InMemoryRefreshTokenStore implements RefreshTokenStore {

    static class Entry { String token; Instant exp; }

    private final Map<String, Entry> store = new ConcurrentHashMap<>();

    private String key(UUID userId, String token) { return userId + "::" + token; }

    @Override
    public void save(UUID userId, String refresh, Duration ttl) {
        Entry e = new Entry();
        e.token = refresh;
        e.exp = Instant.now().plus(ttl);
        store.put(key(userId, refresh), e);
    }

    @Override
    public boolean isValid(UUID userId, String refresh) {
        Entry e = store.get(key(userId, refresh));
        return e != null && e.exp.isAfter(Instant.now());
    }

    @Override
    public void revoke(UUID userId, String refresh) {
        store.remove(key(userId, refresh));
    }
}
