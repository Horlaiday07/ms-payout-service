package com.remit.mellonsecure.payout.domain.model;

import com.remit.mellonsecure.payout.domain.enums.TransactionStatus;
import lombok.Builder;
import lombok.Value;

import java.math.BigDecimal;
import java.time.Instant;

@Value
@Builder
public class TransactionQueryResult {
    String processorReference;
    String paymentReference;
    TransactionStatus status;
    String responseCode;
    String responseMessage;
    BigDecimal amount;
    String remarks;
    Instant completedAt;
    boolean success;
}
