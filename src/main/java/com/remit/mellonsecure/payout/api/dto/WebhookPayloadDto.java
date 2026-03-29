package com.remit.mellonsecure.payout.api.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Builder;
import lombok.Value;

import java.math.BigDecimal;

/**
 * Payload sent to merchant webhook (excludes internal fields like merchantId).
 */
@Value
@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
public class WebhookPayloadDto {
    String responseCode;
    String responseDescription;
    String merchantReference;
    String paymentReference;
    String processorReference;
    BigDecimal amount;
    String remarks;
    String status;
}
