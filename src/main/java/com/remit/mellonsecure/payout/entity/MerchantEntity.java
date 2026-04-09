package com.remit.mellonsecure.payout.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;

import java.time.Instant;

/**
 * JPA mirror of {@code merchants} for FK targets ({@code payout_transactions.merchant_id}).
 * Rows are upserted when a merchant is resolved from Redis / payment dashboard.
 */
@Entity
@Table(name = "merchants")
@Getter
@Setter
public class MerchantEntity {

    @Id
    @Column(length = 36)
    private String id;

    @Column(name = "merchant_code", nullable = false, length = 64)
    private String merchantCode;

    @Column(nullable = false)
    private String name;

    @Column(name = "api_key", nullable = false, length = 64)
    private String apiKey;

    @Column(name = "secret_key", nullable = false, length = 512)
    private String secretKey;

    @Column(name = "webhook_url", length = 512)
    private String webhookUrl;

    @Column(name = "processor_id", length = 64)
    private String processorId;

    @Column(name = "source_account_number", length = 20)
    private String sourceAccountNumber;

    @Column(name = "source_bank_code", length = 10)
    private String sourceBankCode;

    @Column(nullable = false, length = 20)
    private String status;

    @Column(name = "whitelisted_ips", columnDefinition = "TEXT")
    private String whitelistedIps;

    @Column(name = "created_at", nullable = false)
    private Instant createdAt;

    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;
}
