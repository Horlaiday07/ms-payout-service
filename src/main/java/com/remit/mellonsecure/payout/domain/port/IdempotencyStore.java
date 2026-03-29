package com.remit.mellonsecure.payout.domain.port;

/**
 * Port for idempotency check (Redis).
 */
public interface IdempotencyStore {

    boolean tryStore(String key, String response, long ttlSeconds);

    String get(String key);

    boolean exists(String key);
}
