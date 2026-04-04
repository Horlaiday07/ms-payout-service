package com.remit.mellonsecure.payout.repository;

import com.remit.mellonsecure.payout.entity.PayoutTransaction;

import java.util.Optional;

/**
 * Port for payout transaction persistence.
 */
public interface TransactionRepository {

    PayoutTransaction save(PayoutTransaction transaction);

    Optional<PayoutTransaction> findByPaymentReference(String paymentReference);

    Optional<PayoutTransaction> findByTransactionId(String transactionId);

    Optional<PayoutTransaction> findByMerchantIdAndMerchantReference(String merchantId, String merchantReference);

    void updateStatus(String transactionId, String status, String processorResponse);

    void updateStatus(String transactionId, String status, String processorResponse, String processorReference);
}
