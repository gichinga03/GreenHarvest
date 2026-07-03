package com.greenharvest.auth.service;

import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * JWTs are stateless by design, which is exactly what makes "logout" not
 * exist for free — the token stays valid until it expires whether or not
 * the user clicked logout. This service closes that gap for the demo/dev
 * environment.
 *
 * NOTE for the README / reflection: in a real multi-instance production
 * deployment, this in-memory map would be replaced with a shared store
 * (Redis, with TTL = token expiry) so blacklist state is visible to every
 * app instance behind the load balancer. For this project's scope (single
 * instance, short-lived tokens) an in-memory map is a reasonable,
 * explainable trade-off.
 */
@Service
public class TokenBlacklistService {

    private final Map<String, Instant> blacklistedJtis = new ConcurrentHashMap<>();

    public void blacklist(String jti, Instant tokenExpiry) {
        blacklistedJtis.put(jti, tokenExpiry);
    }

    public boolean isBlacklisted(String jti) {
        return blacklistedJtis.containsKey(jti);
    }

    /** Runs every 10 minutes so the map doesn't grow unbounded. */
    @Scheduled(fixedRate = 10 * 60 * 1000)
    public void purgeExpiredEntries() {
        Instant now = Instant.now();
        blacklistedJtis.entrySet().removeIf(entry -> entry.getValue().isBefore(now));
    }
}
