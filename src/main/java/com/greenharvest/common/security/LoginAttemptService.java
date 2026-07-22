package com.greenharvest.common.security;

import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class LoginAttemptService {

    private static final int MAX_ATTEMPTS = 5;
    private static final long LOCK_TIME_DURATION_MILLIS = 5 * 60 * 1000; // 5 minutes

    // In-memory store: Key = Email (lowercase), Value = Attempt Metadata
    private final Map<String, ClientAttempt> attemptsCache = new ConcurrentHashMap<>();

    private static class ClientAttempt {
        int attempts;
        long lockTime;

        ClientAttempt(int attempts, long lockTime) {
            this.attempts = attempts;
            this.lockTime = lockTime;
        }
    }

    /**
     * Checks if the account associated with the email is currently locked out.
     */
    public boolean isBlocked(String email) {
        String key = email.toLowerCase().trim();
        ClientAttempt client = attemptsCache.get(key);

        if (client == null) {
            return false;
        }

        if (client.lockTime > 0) {
            // Check if 5-minute lockout period has expired
            if (System.currentTimeMillis() - client.lockTime > LOCK_TIME_DURATION_MILLIS) {
                // Lockout expired -> Reset
                attemptsCache.remove(key);
                return false;
            }
            return true; // Still locked out
        }

        return false;
    }

    /**
     * Records a failed login attempt. Locks out the account if limit is reached.
     */
    public void loginFailed(String email) {
        String key = email.toLowerCase().trim();
        attemptsCache.compute(key, (k, client) -> {
            if (client == null) {
                return new ClientAttempt(1, 0);
            }

            int newAttempts = client.attempts + 1;
            long newLockTime = client.lockTime;

            if (newAttempts >= MAX_ATTEMPTS) {
                newLockTime = System.currentTimeMillis(); // Trigger 5-min lock
            }

            return new ClientAttempt(newAttempts, newLockTime);
        });
    }

    /**
     * Resets failed attempt counter upon successful login.
     */
    public void loginSucceeded(String email) {
        attemptsCache.remove(email.toLowerCase().trim());
    }

    /**
     * Returns remaining lockout time in seconds (for user feedback).
     */
    public long getRemainingLockoutSeconds(String email) {
        String key = email.toLowerCase().trim();
        ClientAttempt client = attemptsCache.get(key);
        if (client == null || client.lockTime == 0) return 0;

        long elapsed = System.currentTimeMillis() - client.lockTime;
        long remaining = (LOCK_TIME_DURATION_MILLIS - elapsed) / 1000;
        return Math.max(0, remaining);
    }
}