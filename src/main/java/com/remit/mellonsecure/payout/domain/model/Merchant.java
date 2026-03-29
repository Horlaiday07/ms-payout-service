package com.remit.mellonsecure.payout.domain.model;

import com.remit.mellonsecure.payout.domain.enums.MerchantStatus;
import lombok.Builder;
import lombok.Value;

import java.util.List;

@Value
@Builder
public class Merchant {
    String id;
    String name;
    String apiKey;
    String secretKey;
    String webhookUrl;
    String processorId;
    String sourceAccountNumber;
    String sourceBankCode;
    MerchantStatus status;
    List<String> whitelistedIps;
}
