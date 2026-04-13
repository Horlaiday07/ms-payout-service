package com.remit.mellonsecure.payout.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.Instant;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class PayoutTransactionListItemDto {
    /** Business merchant code (e.g. MERCH001). */
    private String merchantId;
    private String paymentReference;
    private Instant dateCreated;
    /** Merchant reference from the payout request (e.g. client ref). */
    private String merchantReference;
    private String accountNumber;
    private BigDecimal amount;
    private String bankName;
    private String status;
    /** Processor reference (e.g. bank ref); shown in expanded detail. */
    private String processorReference;
    /** Original merchant request payload (JSON/text). */
    private String merchantPayload;
    /** Raw processor response body (e.g. NIBSS JSON). */
    private String processorResponse;
    private Instant updatedAt;
}
