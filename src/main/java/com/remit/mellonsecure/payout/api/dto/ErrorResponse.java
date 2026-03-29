package com.remit.mellonsecure.payout.api.dto;

import lombok.Builder;
import lombok.Value;

import java.time.Instant;

@Value
@Builder
public class ErrorResponse {
    String responseCode;
    String responseDescription;
    String merchantReference;
    String paymentReference;
    String processorReference;
    Instant timestamp;
}
