package com.remit.mellonsecure.payout.repository;

import com.remit.mellonsecure.payout.entity.TransactionStatus;
import com.remit.mellonsecure.payout.entity.PayoutTransaction;
import com.remit.mellonsecure.payout.repository.TransactionRepository;
import com.remit.mellonsecure.payout.entity.PayoutTransactionEntity;
import com.remit.mellonsecure.payout.repository.PayoutTransactionJpaRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Component
@RequiredArgsConstructor
public class TransactionRepositoryAdapter implements TransactionRepository {

    private final PayoutTransactionJpaRepository jpaRepository;

    @Override
    public PayoutTransaction save(PayoutTransaction transaction) {
        PayoutTransactionEntity entity = toEntity(transaction);
        PayoutTransactionEntity saved = jpaRepository.save(entity);
        return toDomain(saved);
    }

    @Override
    public Optional<PayoutTransaction> findByPaymentReference(String paymentReference) {
        return jpaRepository.findByPaymentReference(paymentReference).map(this::toDomain);
    }

    @Override
    public Optional<PayoutTransaction> findByTransactionId(String transactionId) {
        return jpaRepository.findById(transactionId).map(this::toDomain);
    }

    @Override
    public Optional<PayoutTransaction> findByMerchantIdAndMerchantReference(String merchantId, String merchantReference) {
        return jpaRepository.findByMerchantIdAndMerchantReference(merchantId, merchantReference).map(this::toDomain);
    }

    @Override
    @Transactional
    public void updateStatus(String transactionId, String status, String processorResponse) {
        jpaRepository.updateStatus(transactionId, status, processorResponse);
    }

    @Override
    @Transactional
    public void updateStatus(String transactionId, String status, String processorResponse, String processorReference) {
        jpaRepository.updateStatusWithProcessorRef(transactionId, status, processorResponse, processorReference);
    }

    private PayoutTransactionEntity toEntity(PayoutTransaction domain) {
        return PayoutTransactionEntity.builder()
                .id(domain.getId())
                .paymentReference(domain.getPaymentReference())
                .merchantReference(domain.getMerchantReference())
                .processorReference(domain.getProcessorReference())
                .merchantId(domain.getMerchantId())
                .amount(domain.getAmount())
                .accountNumber(domain.getAccountNumber())
                .bankCode(domain.getBankCode())
                .accountName(domain.getAccountName())
                .status(domain.getStatus().name())
                .merchantPayload(domain.getMerchantPayload())
                .processorResponse(domain.getProcessorResponse())
                .createdAt(domain.getCreatedAt())
                .updatedAt(domain.getUpdatedAt())
                .build();
    }

    private PayoutTransaction toDomain(PayoutTransactionEntity entity) {
        return PayoutTransaction.builder()
                .id(entity.getId())
                .paymentReference(entity.getPaymentReference())
                .merchantReference(entity.getMerchantReference())
                .processorReference(entity.getProcessorReference())
                .merchantId(entity.getMerchantId())
                .amount(entity.getAmount())
                .accountNumber(entity.getAccountNumber())
                .bankCode(entity.getBankCode())
                .accountName(entity.getAccountName())
                .status(TransactionStatus.valueOf(entity.getStatus()))
                .merchantPayload(entity.getMerchantPayload())
                .processorResponse(entity.getProcessorResponse())
                .createdAt(entity.getCreatedAt())
                .updatedAt(entity.getUpdatedAt())
                .build();
    }
}
