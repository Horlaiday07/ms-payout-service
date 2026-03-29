package com.remit.mellonsecure.payout.infrastructure.redis;

import com.remit.mellonsecure.payout.domain.port.IdGenerator;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.util.UUID;

@Component
@RequiredArgsConstructor
public class RedisIdGenerator implements IdGenerator {

    private static final String PAYMENT_REF_PREFIX = "PAY";
    private static final String BATCH_REF_PREFIX = "BAT";
    private static final String ID_COUNTER_KEY = "payout:id:counter";

    private final StringRedisTemplate redisTemplate;

    @Override
    public String generatePaymentReference() {
        long seq = nextSequence();
        return String.format("%s%d%013d", PAYMENT_REF_PREFIX, Instant.now().getEpochSecond(), seq);
    }

    @Override
    public String generateBatchReference() {
        long seq = nextSequence();
        return String.format("%s%d%013d", BATCH_REF_PREFIX, Instant.now().getEpochSecond(), seq);
    }

    @Override
    public long nextId() {
        Long increment = redisTemplate.opsForValue().increment(ID_COUNTER_KEY);
        return increment != null ? increment : System.currentTimeMillis();
    }

    private long nextSequence() {
        Long increment = redisTemplate.opsForValue().increment(ID_COUNTER_KEY);
        return increment != null ? increment : 0L;
    }
}
