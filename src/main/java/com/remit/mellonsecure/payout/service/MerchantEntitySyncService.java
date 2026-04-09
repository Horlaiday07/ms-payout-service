package com.remit.mellonsecure.payout.service;

import com.remit.mellonsecure.payout.entity.Merchant;
import com.remit.mellonsecure.payout.entity.MerchantEntity;
import com.remit.mellonsecure.payout.repository.MerchantJpaRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.time.Instant;
import java.util.stream.Collectors;

/**
 * Keeps {@code merchants} in sync with resolved {@link Merchant} snapshots so FKs on
 * {@code payout_transactions} and {@code payout_batches} succeed.
 */
@Service
@RequiredArgsConstructor
public class MerchantEntitySyncService {

    private final MerchantJpaRepository merchantJpaRepository;

    @Transactional
    public void ensureRow(Merchant m) {
        if (m == null || !StringUtils.hasText(m.getId())) {
            return;
        }
        Instant now = Instant.now();
        MerchantEntity entity = merchantJpaRepository.findById(m.getId().trim()).orElseGet(() -> {
            MerchantEntity e = new MerchantEntity();
            e.setId(m.getId().trim());
            e.setCreatedAt(now);
            return e;
        });

        entity.setMerchantCode(StringUtils.hasText(m.getMerchantCode()) ? m.getMerchantCode().trim() : entity.getId());
        entity.setName(StringUtils.hasText(m.getName()) ? m.getName().trim() : entity.getId());
        String apiKey = StringUtils.hasText(m.getApiKey()) ? m.getApiKey() : ("sync-" + entity.getId());
        if (apiKey.length() > 64) {
            apiKey = apiKey.substring(0, 64);
        }
        entity.setApiKey(apiKey);
        String secret = StringUtils.hasText(m.getSecretKey()) ? m.getSecretKey() : "-";
        if (secret.length() > 512) {
            secret = secret.substring(0, 512);
        }
        entity.setSecretKey(secret);
        entity.setWebhookUrl(m.getWebhookUrl());
        entity.setProcessorId(m.getProcessorId());
        entity.setSourceAccountNumber(m.getSourceAccountNumber());
        entity.setSourceBankCode(m.getSourceBankCode());
        entity.setStatus(m.getStatus() != null ? m.getStatus().name() : "ACTIVE");
        if (m.getWhitelistedIps() != null && !m.getWhitelistedIps().isEmpty()) {
            entity.setWhitelistedIps(m.getWhitelistedIps().stream().map(String::trim).collect(Collectors.joining(",")));
        } else if (entity.getWhitelistedIps() == null) {
            entity.setWhitelistedIps("");
        }
        entity.setUpdatedAt(now);
        if (entity.getCreatedAt() == null) {
            entity.setCreatedAt(now);
        }

        merchantJpaRepository.save(entity);
    }
}
