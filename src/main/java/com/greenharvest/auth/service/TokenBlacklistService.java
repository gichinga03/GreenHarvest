package com.greenharvest.auth.service;

import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;


@Service
public class TokenBlacklistService {

    private final Map<String, Instant> blacklistedJtis = new ConcurrentHashMap<>();

    public void blacklist(String jti, Instant tokenExpiry) {
        blacklistedJtis.put(jti, tokenExpiry);
    }

    public boolean isBlacklisted(String jti) {
        return blacklistedJtis.containsKey(jti);
    }

    @Scheduled(fixedRate = 10 * 60 * 1000)
    public void purgeExpiredEntries() {
        Instant now = Instant.now();
        blacklistedJtis.entrySet().removeIf(entry -> entry.getValue().isBefore(now));
    }
}
