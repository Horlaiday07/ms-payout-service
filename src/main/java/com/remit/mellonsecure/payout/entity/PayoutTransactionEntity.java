package com.remit.mellonsecure.payout.entity;

import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.Instant;

@Entity
@Table(name = "payout_transactions")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PayoutTransactionEntity {

    @Id
    @Column(name = "id", length = 36)
    private String id;

    @Column(name = "payment_reference", nullable = false, unique = true, length = 64)
    private String paymentReference;

    @Column(name = "merchant_reference", nullable = false, length = 128)
    private String merchantReference;

    @Column(name = "processor_reference", length = 64)
    private String processorReference;

    @Column(name = "merchant_id", nullable = false, length = 36)
    private String merchantId;

    @Column(name = "batch_id", length = 36)
    private String batchId;

    @Column(name = "amount", nullable = false, precision = 19, scale = 4)
    private BigDecimal amount;

    @Column(name = "account_number", nullable = false, length = 20)
    private String accountNumber;

    @Column(name = "bank_code", nullable = false, length = 10)
    private String bankCode;

    @Column(name = "account_name", length = 255)
    private String accountName;

    @Column(name = "status", nullable = false, length = 20)
    private String status;

    @Column(name = "merchant_payload", columnDefinition = "TEXT")
    private String merchantPayload;

    @Column(name = "processor_response", columnDefinition = "TEXT")
    private String processorResponse;

    @Column(name = "ledger_journal_id", length = 36)
    private String ledgerJournalId;

    @Column(name = "created_at", nullable = false)
    private Instant createdAt;

    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;
}
