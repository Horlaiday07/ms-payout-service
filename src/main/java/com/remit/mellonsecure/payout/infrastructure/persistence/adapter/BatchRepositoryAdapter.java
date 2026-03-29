package com.remit.mellonsecure.payout.infrastructure.persistence.adapter;

import com.remit.mellonsecure.payout.domain.enums.BatchStatus;
import com.remit.mellonsecure.payout.domain.model.PayoutBatch;
import com.remit.mellonsecure.payout.domain.port.BatchRepository;
import com.remit.mellonsecure.payout.infrastructure.persistence.entity.PayoutBatchEntity;
import com.remit.mellonsecure.payout.infrastructure.persistence.repository.PayoutBatchJpaRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.Optional;

@Component
@RequiredArgsConstructor
public class BatchRepositoryAdapter implements BatchRepository {

    private final PayoutBatchJpaRepository jpaRepository;

    @Override
    public PayoutBatch save(PayoutBatch batch) {
        PayoutBatchEntity entity = toEntity(batch);
        PayoutBatchEntity saved = jpaRepository.save(entity);
        return toDomain(saved);
    }

    @Override
    public Optional<PayoutBatch> findById(String batchId) {
        return jpaRepository.findById(batchId).map(this::toDomain);
    }

    @Override
    public Optional<PayoutBatch> findByBatchReference(String batchReference) {
        return jpaRepository.findByBatchReference(batchReference).map(this::toDomain);
    }

    private PayoutBatchEntity toEntity(PayoutBatch domain) {
        return PayoutBatchEntity.builder()
                .id(domain.getId())
                .batchReference(domain.getBatchReference())
                .merchantId(domain.getMerchantId())
                .status(domain.getStatus().name())
                .totalCount(domain.getTotalCount() != null ? domain.getTotalCount() : 0)
                .successCount(domain.getSuccessCount() != null ? domain.getSuccessCount() : 0)
                .failedCount(domain.getFailedCount() != null ? domain.getFailedCount() : 0)
                .createdAt(domain.getCreatedAt())
                .updatedAt(domain.getUpdatedAt())
                .build();
    }

    private PayoutBatch toDomain(PayoutBatchEntity entity) {
        return PayoutBatch.builder()
                .id(entity.getId())
                .batchReference(entity.getBatchReference())
                .merchantId(entity.getMerchantId())
                .status(BatchStatus.valueOf(entity.getStatus()))
                .totalCount(entity.getTotalCount())
                .successCount(entity.getSuccessCount())
                .failedCount(entity.getFailedCount())
                .createdAt(entity.getCreatedAt())
                .updatedAt(entity.getUpdatedAt())
                .build();
    }
}
