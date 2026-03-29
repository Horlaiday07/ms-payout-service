package com.remit.mellonsecure.payout.domain.model;

import lombok.Builder;
import lombok.Value;

import java.math.BigDecimal;

@Value
@Builder
public class WebhookPayload {
    String merchantId;
    String paymentReference;
    String merchantReference;
    String processorReference;
    String status;
    String responseCode;
    String responseDescription;
    BigDecimal amount;
    String remarks;
    String webhookUrl;
}
