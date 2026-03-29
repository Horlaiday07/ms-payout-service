package com.remit.mellonsecure.payout.domain.model;

import lombok.Builder;
import lombok.Value;

import java.math.BigDecimal;

@Value
@Builder
public class TransferRequest {
    String paymentReference;
    String nameEnquiryRef;
    String accountNumber;
    String bankCode;
    String accountName;
    BigDecimal amount;
    String narration;
    String sourceAccount;
}
