package com.remit.mellonsecure.payout.repository;

import com.remit.mellonsecure.payout.entity.WebhookEventEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface WebhookEventJpaRepository extends JpaRepository<WebhookEventEntity, String> {

    List<WebhookEventEntity> findByStatusAndNextRetryAtBefore(String status, java.time.Instant before);
}
