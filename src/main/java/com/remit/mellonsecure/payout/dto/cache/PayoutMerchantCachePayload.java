package com.remit.mellonsecure.payout.dto.cache;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * JSON shape written by the payment dashboard to Redis (must stay in sync).
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonIgnoreProperties(ignoreUnknown = true)
public class PayoutMerchantCachePayload {
    private String merchantCode;
    private String name;
    private String email;
    private String status;
    private String apiKey;
    private String apiSecretPlain;
    private String webhookUrl;
    private String processorId;
    private String sourceAccountNumber;
    private String ledgerMerchantAccountId;
    private String ledgerInternalAccountId;
    private String ledgerSettlementAccountId;
    private String sourceBankCode;
    private String whitelistedIps;
}
