package com.remit.mellonsecure.payout.domain.model;

import com.remit.mellonsecure.payout.domain.enums.BatchStatus;
import lombok.Builder;
import lombok.Value;

import java.time.Instant;

@Value
@Builder
public class PayoutBatch {
    String id;
    String batchReference;
    String merchantId;
    BatchStatus status;
    Integer totalCount;
    Integer successCount;
    Integer failedCount;
    Instant createdAt;
    Instant updatedAt;
}
