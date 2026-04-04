package com.remit.mellonsecure.payout.dto;

import lombok.Builder;
import lombok.Value;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;

@Value
@Builder
public class PayoutMerchantDashboardDto {
    BigDecimal currentFloatBalance;
    List<RecentPayoutTransactionDto> lastTransactions;

    @Value
    @Builder
    public static class RecentPayoutTransactionDto {
        String transactionId;
        String paymentReference;
        String merchantReference;
        BigDecimal amount;
        String status;
        String accountNumber;
        String bankCode;
        Instant createdAt;
    }
}
