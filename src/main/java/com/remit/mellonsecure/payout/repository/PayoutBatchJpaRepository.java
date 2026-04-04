package com.remit.mellonsecure.payout.repository;

import com.remit.mellonsecure.payout.entity.PayoutBatchEntity;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PayoutBatchJpaRepository extends JpaRepository<PayoutBatchEntity, String> {

    java.util.Optional<PayoutBatchEntity> findByBatchReference(String batchReference);
}
