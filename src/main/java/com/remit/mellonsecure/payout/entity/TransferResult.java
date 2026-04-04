package com.remit.mellonsecure.payout.entity;

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
