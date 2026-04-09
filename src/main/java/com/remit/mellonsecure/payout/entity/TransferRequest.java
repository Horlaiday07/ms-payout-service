package com.remit.mellonsecure.payout.entity;

import lombok.Builder;
import lombok.Value;

import java.math.BigDecimal;

@Value
@Builder
public class TransferRequest {
    /** Payout transaction id (sent to NIBSS as {@code transactionId}). */
    String transactionId;
    String paymentReference;
    String nameEnquiryRef;
    String accountNumber;
    String bankCode;
    String accountName;
    BigDecimal amount;
    String narration;
    String sourceAccount;
}
