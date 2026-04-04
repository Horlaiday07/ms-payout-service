package com.remit.mellonsecure.payout.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.Instant;

@Entity
@Table(name = "merchants")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MerchantEntity {

    @Id
    @Column(name = "id", length = 36)
    private String id;

    @Column(name = "name", nullable = false)
    private String name;

    @Column(name = "api_key", nullable = false, unique = true, length = 64)
    private String apiKey;

    @Column(name = "secret_key", nullable = false, length = 256)
    private String secretKey;

    @Column(name = "webhook_url", length = 512)
    private String webhookUrl;

    @Column(name = "processor_id", length = 64)
    private String processorId;

    @Column(name = "source_account_number", length = 20)
    private String sourceAccountNumber;

    @Column(name = "source_bank_code", length = 10)
    private String sourceBankCode;

    @Column(name = "status", nullable = false, length = 20)
    private String status;

    @Column(name = "whitelisted_ips", columnDefinition = "TEXT")
    private String whitelistedIps;

    @Column(name = "created_at", nullable = false)
    private Instant createdAt;

    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;
}
