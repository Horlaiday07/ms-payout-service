package com.remit.mellonsecure.payout.infrastructure.persistence.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.Instant;

@Entity
@Table(name = "webhook_events")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class WebhookEventEntity {

    @Id
    @Column(name = "id", length = 36)
    private String id;

    @Column(name = "payment_reference", nullable = false, length = 64)
    private String paymentReference;

    @Column(name = "merchant_reference", nullable = false, length = 128)
    private String merchantReference;

    @Column(name = "webhook_url", nullable = false, length = 512)
    private String webhookUrl;

    @Column(name = "status", nullable = false, length = 20)
    private String status;

    @Column(name = "retry_count", nullable = false)
    private Integer retryCount;

    @Column(name = "payload", columnDefinition = "TEXT")
    private String payload;

    @Column(name = "error_message", columnDefinition = "TEXT")
    private String errorMessage;

    @Column(name = "created_at", nullable = false)
    private Instant createdAt;

    @Column(name = "next_retry_at")
    private Instant nextRetryAt;
}
