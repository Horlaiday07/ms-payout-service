package com.remit.mellonsecure.payout.entity;

import lombok.Builder;
import lombok.Value;

@Value
@Builder
public class NameEnquiryResult {
    String accountNumber;
    String bankCode;
    String accountName;
    String sessionId;
    boolean success;
    String responseCode;
    String responseMessage;
}
