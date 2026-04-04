package com.remit.mellonsecure.payout.entity;

import lombok.Builder;
import lombok.Value;

import java.math.BigDecimal;

@Value
@Builder
public class TransferMessage {
    String transactionId;
    String paymentReference;
    String merchantReference;
    String merchantId;
    String accountNumber;
    String bankCode;
    String accountName;
    String nameEnquiryRef;
    BigDecimal amount;
    String narration;
    String sourceAccount;
}
