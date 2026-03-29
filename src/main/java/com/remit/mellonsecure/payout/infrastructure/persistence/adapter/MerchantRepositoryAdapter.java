package com.remit.mellonsecure.payout.infrastructure.persistence.adapter;

import com.remit.mellonsecure.payout.domain.enums.MerchantStatus;
import com.remit.mellonsecure.payout.domain.model.Merchant;
import com.remit.mellonsecure.payout.domain.port.MerchantRepository;
import com.remit.mellonsecure.payout.infrastructure.persistence.entity.MerchantEntity;
import com.remit.mellonsecure.payout.infrastructure.persistence.repository.MerchantJpaRepository;
import com.remit.mellonsecure.payout.infrastructure.persistence.repository.PayoutTransactionJpaRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Component
@RequiredArgsConstructor
public class MerchantRepositoryAdapter implements MerchantRepository {

    private final MerchantJpaRepository merchantJpaRepository;
    private final PayoutTransactionJpaRepository transactionJpaRepository;

    @Override
    public Optional<Merchant> findById(String merchantId) {
        return merchantJpaRepository.findById(merchantId)
                .map(this::toDomain);
    }

    @Override
    public Optional<Merchant> findByApiKey(String apiKey) {
        return merchantJpaRepository.findByApiKey(apiKey)
                .map(this::toDomain);
    }

    @Override
    public boolean existsByMerchantReference(String merchantId, String merchantReference) {
        return transactionJpaRepository.existsByMerchantIdAndMerchantReference(merchantId, merchantReference);
    }

    private Merchant toDomain(MerchantEntity entity) {
        List<String> ips = entity.getWhitelistedIps() != null && !entity.getWhitelistedIps().isBlank()
                ? Arrays.stream(entity.getWhitelistedIps().split(","))
                .map(String::trim)
                .filter(s -> !s.isEmpty())
                .collect(Collectors.toList())
                : Collections.emptyList();

        return Merchant.builder()
                .id(entity.getId())
                .name(entity.getName())
                .apiKey(entity.getApiKey())
                .secretKey(entity.getSecretKey())
                .webhookUrl(entity.getWebhookUrl())
                .processorId(entity.getProcessorId())
                .sourceAccountNumber(entity.getSourceAccountNumber())
                .sourceBankCode(entity.getSourceBankCode())
                .status(MerchantStatus.valueOf(entity.getStatus()))
                .whitelistedIps(ips)
                .build();
    }
}
