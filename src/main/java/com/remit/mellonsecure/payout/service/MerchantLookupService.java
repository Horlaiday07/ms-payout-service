package com.remit.mellonsecure.payout.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.remit.mellonsecure.payout.cache.PayoutMerchantCacheReader;
import com.remit.mellonsecure.payout.dto.cache.PayoutMerchantCachePayload;
import com.remit.mellonsecure.payout.entity.Merchant;
import com.remit.mellonsecure.payout.entity.MerchantStatus;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientResponseException;

import java.time.Duration;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * Resolves merchants by business id: Redis (same keys as payment dashboard), then payment dashboard
 * internal sync API ({@code X-Payout-Sync-Key}, no user JWT). No payout merchants table reads.
 */
@Service
@Slf4j
public class MerchantLookupService {

    private static final Duration HTTP_TIMEOUT = Duration.ofSeconds(10);
    private static final String SYNC_HEADER = "X-Payout-Sync-Key";

    private final ObjectProvider<PayoutMerchantCacheReader> cacheReaderProvider;
    private final WebClient.Builder webClientBuilder;
    private final ObjectMapper objectMapper;
    private final StringRedisTemplate stringRedisTemplate;
    private final MerchantEntitySyncService merchantEntitySyncService;
    private final String paymentDashboardBaseUrl;
    private final String syncApiKey;
    private final String keyPrefix;

    public MerchantLookupService(
            ObjectProvider<PayoutMerchantCacheReader> cacheReaderProvider,
            WebClient.Builder webClientBuilder,
            ObjectMapper objectMapper,
            StringRedisTemplate stringRedisTemplate,
            MerchantEntitySyncService merchantEntitySyncService,
            @Value("${payout.payment-dashboard.base-url:}") String paymentDashboardBaseUrl,
            @Value("${payout.payment-dashboard.sync-api-key:}") String syncApiKey,
            @Value("${payout.merchant-cache.key-prefix:payout:merchant:}") String keyPrefixRaw) {
        this.cacheReaderProvider = cacheReaderProvider;
        this.webClientBuilder = webClientBuilder;
        this.objectMapper = objectMapper;
        this.stringRedisTemplate = stringRedisTemplate;
        this.merchantEntitySyncService = merchantEntitySyncService;
        this.paymentDashboardBaseUrl = paymentDashboardBaseUrl != null ? paymentDashboardBaseUrl.trim() : "";
        this.syncApiKey = syncApiKey != null ? syncApiKey.trim() : "";
        this.keyPrefix = keyPrefixRaw.endsWith(":") ? keyPrefixRaw : keyPrefixRaw + ":";
    }

    /**
     * Redis → payment dashboard sync GET → local Redis mirror. No payout DB.
     */
    public Optional<Merchant> findByMerchantIdFromCacheOrSync(String merchantId) {
        if (!StringUtils.hasText(merchantId)) {
            return Optional.empty();
        }
        String id = merchantId.trim();

        PayoutMerchantCacheReader reader = cacheReaderProvider.getIfAvailable();
        if (reader != null) {
            Optional<Merchant> fromRedis = reader.findByMerchantCode(id).map(this::toDomain);
            if (fromRedis.isPresent()) {
                merchantEntitySyncService.ensureRow(fromRedis.get());
                return fromRedis;
            }
        }

        if (!StringUtils.hasText(paymentDashboardBaseUrl) || !StringUtils.hasText(syncApiKey)) {
            log.warn("Merchant {} not resolved: no Redis key {}{}; configure payout.payment-dashboard.base-url and sync-api-key (same as app.payout.merchant-sync-api-key on payment app)",
                    id, keyPrefix, id);
            return Optional.empty();
        }

        try {
            String body = webClientBuilder
                    .baseUrl(trimTrailingSlash(paymentDashboardBaseUrl))
                    .build()
                    .get()
                    .uri("/api/internal/payout-merchants/by-code/{code}", id)
                    .header(SYNC_HEADER, syncApiKey)
                    .retrieve()
                    .bodyToMono(String.class)
                    .block(HTTP_TIMEOUT);
            if (StringUtils.hasText(body)) {
                try {
                    stringRedisTemplate.opsForValue().set(keyPrefix + id, body);
                } catch (Exception e) {
                    log.debug("Could not mirror merchant snapshot to Redis for {}: {}", id, e.getMessage());
                }
                PayoutMerchantCachePayload payload = objectMapper.readValue(body, PayoutMerchantCachePayload.class);
                Merchant merchant = toDomain(payload);
                merchantEntitySyncService.ensureRow(merchant);
                return Optional.of(merchant);
            }
        } catch (WebClientResponseException e) {
            if (e.getStatusCode() == HttpStatus.NOT_FOUND) {
                log.warn("Merchant {} not resolved: payment dashboard has no merchant (404 on sync)", id);
            } else {
                log.warn("Payment dashboard merchant lookup failed for {}: {} {}", id, e.getStatusCode(), e.getMessage());
            }
        } catch (Exception e) {
            log.warn("Payment dashboard merchant lookup failed for {}: {}", id, e.getMessage());
        }

        log.warn("Merchant {} not resolved after Redis miss and sync attempt", id);
        return Optional.empty();
    }

    private static String trimTrailingSlash(String url) {
        if (url == null || url.isEmpty()) {
            return "";
        }
        return url.endsWith("/") ? url.substring(0, url.length() - 1) : url;
    }

    private Merchant toDomain(PayoutMerchantCachePayload p) {
        List<String> ips = parseIps(p.getWhitelistedIps());
        MerchantStatus status;
        try {
            status = MerchantStatus.valueOf(p.getStatus() != null ? p.getStatus().trim().toUpperCase() : "INACTIVE");
        } catch (IllegalArgumentException e) {
            status = MerchantStatus.INACTIVE;
        }
        String mid = p.getMerchantCode() != null ? p.getMerchantCode() : "";
        return Merchant.builder()
                .id(mid)
                .merchantCode(mid)
                .name(p.getName() != null ? p.getName() : "")
                .apiKey(p.getApiKey() != null ? p.getApiKey() : "")
                .secretKey(p.getApiSecretPlain() != null ? p.getApiSecretPlain() : "")
                .webhookUrl(p.getWebhookUrl())
                .processorId(p.getProcessorId())
                .sourceAccountNumber(p.getSourceAccountNumber())
                .sourceBankCode(p.getSourceBankCode())
                .status(status)
                .whitelistedIps(ips)
                .build();
    }

    private static List<String> parseIps(String raw) {
        if (raw == null || raw.isBlank()) {
            return Collections.emptyList();
        }
        return Arrays.stream(raw.split(","))
                .map(String::trim)
                .filter(s -> !s.isEmpty())
                .collect(Collectors.toList());
    }
}
