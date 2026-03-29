package com.remit.mellonsecure.payout.infrastructure.redis;

import com.remit.mellonsecure.payout.domain.port.IdempotencyStore;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;

import java.time.Duration;

@Component
@RequiredArgsConstructor
public class RedisIdempotencyStore implements IdempotencyStore {

    private final StringRedisTemplate redisTemplate;

    @Value("${payout.idempotency.key-prefix:idempotency:}")
    private String keyPrefix;

    @Value("${payout.idempotency.ttl-hours:24}")
    private int ttlHours;

    @Override
    public boolean tryStore(String key, String response, long ttlSeconds) {
        String fullKey = keyPrefix + key;
        Boolean result = redisTemplate.opsForValue().setIfAbsent(fullKey, response, Duration.ofSeconds(ttlSeconds > 0 ? ttlSeconds : ttlHours * 3600L));
        return Boolean.TRUE.equals(result);
    }

    @Override
    public String get(String key) {
        return redisTemplate.opsForValue().get(keyPrefix + key);
    }

    @Override
    public boolean exists(String key) {
        Boolean has = redisTemplate.hasKey(keyPrefix + key);
        return Boolean.TRUE.equals(has);
    }
}
