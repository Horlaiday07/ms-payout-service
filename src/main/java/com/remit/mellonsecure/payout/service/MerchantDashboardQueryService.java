package com.remit.mellonsecure.payout.service;

import com.remit.mellonsecure.payout.dto.PayoutMerchantDashboardDto;
import com.remit.mellonsecure.payout.exception.MerchantNotFoundException;
import com.remit.mellonsecure.payout.entity.Merchant;
import com.remit.mellonsecure.payout.client.LedgerClient;
import com.remit.mellonsecure.payout.entity.PayoutTransactionEntity;
import com.remit.mellonsecure.payout.repository.PayoutTransactionJpaRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class MerchantDashboardQueryService {

    private final MerchantLookupService merchantLookupService;
    private final LedgerClient ledgerClient;
    private final PayoutTransactionJpaRepository transactionJpaRepository;

    public PayoutMerchantDashboardDto get(String merchantId) {
        Merchant merchant = merchantLookupService.findByMerchantIdFromCacheOrSync(merchantId)
                .orElseThrow(() -> new MerchantNotFoundException(merchantId));

        BigDecimal balance = BigDecimal.ZERO;
        if (merchant.getSourceAccountNumber() != null && !merchant.getSourceAccountNumber().isBlank()) {
            balance = ledgerClient.getBalance(merchant.getSourceAccountNumber());
        }

        var page = transactionJpaRepository.findByMerchantIdOrderByCreatedAtDesc(merchantId, PageRequest.of(0, 10));
        List<PayoutMerchantDashboardDto.RecentPayoutTransactionDto> rows = page.getContent().stream()
                .map(this::toRecent)
                .collect(Collectors.toList());

        return PayoutMerchantDashboardDto.builder()
                .currentFloatBalance(balance)
                .lastTransactions(rows)
                .build();
    }

    private PayoutMerchantDashboardDto.RecentPayoutTransactionDto toRecent(PayoutTransactionEntity e) {
        return PayoutMerchantDashboardDto.RecentPayoutTransactionDto.builder()
                .transactionId(e.getId())
                .paymentReference(e.getPaymentReference())
                .merchantReference(e.getMerchantReference())
                .amount(e.getAmount())
                .status(e.getStatus())
                .accountNumber(e.getAccountNumber())
                .bankCode(e.getBankCode())
                .createdAt(e.getCreatedAt())
                .build();
    }
}
