package com.remit.mellonsecure.payout.repository;

import com.remit.mellonsecure.payout.entity.PayoutTransactionEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface PayoutTransactionJpaRepository extends JpaRepository<PayoutTransactionEntity, String> {

    Page<PayoutTransactionEntity> findByMerchantIdOrderByCreatedAtDesc(String merchantId, Pageable pageable);

    Optional<PayoutTransactionEntity> findByPaymentReference(String paymentReference);

    Optional<PayoutTransactionEntity> findByMerchantIdAndMerchantReference(String merchantId, String merchantReference);

    boolean existsByMerchantIdAndMerchantReference(String merchantId, String merchantReference);

    @Modifying
    @Query("UPDATE PayoutTransactionEntity t SET t.status = :status, t.processorResponse = :processorResponse, t.updatedAt = CURRENT_TIMESTAMP WHERE t.id = :transactionId")
    void updateStatus(@Param("transactionId") String transactionId,
                     @Param("status") String status,
                     @Param("processorResponse") String processorResponse);

    @Modifying
    @Query("UPDATE PayoutTransactionEntity t SET t.status = :status, t.processorResponse = :processorResponse, t.processorReference = :processorReference, t.updatedAt = CURRENT_TIMESTAMP WHERE t.id = :transactionId")
    void updateStatusWithProcessorRef(@Param("transactionId") String transactionId,
                                      @Param("status") String status,
                                      @Param("processorResponse") String processorResponse,
                                      @Param("processorReference") String processorReference);
}
