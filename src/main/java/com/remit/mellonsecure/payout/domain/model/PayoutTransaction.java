package com.remit.mellonsecure.payout.domain.model;

import com.remit.mellonsecure.payout.domain.enums.TransactionStatus;
import lombok.Builder;
import lombok.Value;

import java.math.BigDecimal;
import java.time.Instant;

@Value
@Builder
public class PayoutTransaction {
    String id;
    String paymentReference;
    String merchantReference;
    String processorReference;
    String merchantId;
    BigDecimal amount;
    String accountNumber;
    String bankCode;
    String accountName;
    TransactionStatus status;
    String merchantPayload;
    String processorResponse;
    Instant createdAt;
    Instant updatedAt;
}
