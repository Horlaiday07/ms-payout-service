package com.remit.mellonsecure.payout.cache;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.remit.mellonsecure.payout.dto.cache.PayoutMerchantCachePayload;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;

import java.util.Optional;

/**
 * Reads merchant snapshots from Redis (same keys as payment dashboard: {@code {prefix}{merchantId}}).
 */
@Component
@ConditionalOnProperty(name = "payout.merchant-cache.reader-enabled", havingValue = "true", matchIfMissing = true)
@Slf4j
public class PayoutMerchantCacheReader {

    private final StringRedisTemplate redisTemplate;
    private final ObjectMapper objectMapper;
    private final String keyPrefix;

    public PayoutMerchantCacheReader(
            StringRedisTemplate redisTemplate,
            ObjectMapper objectMapper,
            @Value("${payout.merchant-cache.key-prefix:payout:merchant:}") String keyPrefix) {
        this.redisTemplate = redisTemplate;
        this.objectMapper = objectMapper;
        this.keyPrefix = keyPrefix.endsWith(":") ? keyPrefix : keyPrefix + ":";
    }

    public Optional<PayoutMerchantCachePayload> findByMerchantCode(String merchantCode) {
        if (merchantCode == null || merchantCode.isBlank()) {
            return Optional.empty();
        }
        return readJson(keyPrefix + merchantCode.trim());
    }

    private Optional<PayoutMerchantCachePayload> readJson(String redisKey) {
        try {
            String json = redisTemplate.opsForValue().get(redisKey);
            if (json == null || json.isBlank()) {
                return Optional.empty();
            }
            return Optional.of(objectMapper.readValue(json, PayoutMerchantCachePayload.class));
        } catch (Exception e) {
            log.debug("Redis merchant cache miss or parse error for key {}: {}", redisKey, e.getMessage());
            return Optional.empty();
        }
    }
}
