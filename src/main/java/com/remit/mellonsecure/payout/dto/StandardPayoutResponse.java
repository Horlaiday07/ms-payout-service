package com.remit.mellonsecure.payout.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Builder;
import lombok.Value;

import java.math.BigDecimal;

@Value
@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
public class StandardPayoutResponse {
    String responseCode;
    String responseDescription;
    String merchantReference;
    String paymentReference;
    String processorReference;
    BigDecimal amount;
    String remarks;
}
