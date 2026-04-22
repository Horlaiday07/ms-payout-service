package com.remit.mellonsecure.payout.entity;

import com.remit.mellonsecure.payout.entity.MerchantStatus;
import lombok.Builder;
import lombok.Value;

import java.util.List;

@Value
@Builder
public class Merchant {
    String id;
    /** Business code (e.g. MERCH001); same as {@link #id} when resolved from cache. */
    String merchantCode;
    String name;
    String apiKey;
    String secretKey;
    String webhookUrl;
    String processorId;
    String sourceAccountNumber;
    String ledgerMerchantAccountId;
    String ledgerInternalAccountId;
    String ledgerSettlementAccountId;
    String sourceBankCode;
    MerchantStatus status;
    List<String> whitelistedIps;
}
