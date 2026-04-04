package com.remit.mellonsecure.payout.exception;

public class IdempotencyConflictException extends PayoutDomainException {
    private final String cachedResponse;

    public IdempotencyConflictException(String key) {
        super("IDEMPOTENCY_CONFLICT", "Duplicate request detected for idempotency key: " + key);
        this.cachedResponse = null;
    }

    public IdempotencyConflictException(String key, String cachedResponse) {
        super("IDEMPOTENCY_CONFLICT", "Duplicate request detected for idempotency key: " + key);
        this.cachedResponse = cachedResponse;
    }

    public String getCachedResponse() {
        return cachedResponse;
    }
}
