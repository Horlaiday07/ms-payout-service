package com.remit.mellonsecure.payout.infrastructure.persistence.repository;

import com.remit.mellonsecure.payout.infrastructure.persistence.entity.WebhookEventEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface WebhookEventJpaRepository extends JpaRepository<WebhookEventEntity, String> {

    List<WebhookEventEntity> findByStatusAndNextRetryAtBefore(String status, java.time.Instant before);
}
