package com.remit.mellonsecure.payout.domain.model;

import lombok.Builder;
import lombok.Value;

import java.math.BigDecimal;

@Value
@Builder
public class TransferResult {
    String paymentReference;
    String processorReference;
    boolean success;
    String responseCode;
    String responseMessage;
    BigDecimal amount;
    String remarks;
}
