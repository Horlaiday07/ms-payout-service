package com.remit.mellonsecure.payout.repository;

import com.remit.mellonsecure.payout.entity.PayoutBatch;

import java.util.Optional;

/**
 * Port for payout batch persistence.
 */
public interface BatchRepository {

    PayoutBatch save(PayoutBatch batch);

    Optional<PayoutBatch> findById(String batchId);

    Optional<PayoutBatch> findByBatchReference(String batchReference);
}
