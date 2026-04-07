package com.remit.mellonsecure.payout.service;

import com.remit.mellonsecure.payout.entity.BatchStatus;
import com.remit.mellonsecure.payout.entity.MerchantStatus;
import com.remit.mellonsecure.payout.entity.TransactionStatus;
import com.remit.mellonsecure.payout.exception.MerchantInactiveException;
import com.remit.mellonsecure.payout.exception.MerchantNotFoundException;
import com.remit.mellonsecure.payout.entity.*;
import com.remit.mellonsecure.payout.client.LedgerClient;
import com.remit.mellonsecure.payout.processor.ProcessorAdapter;
import com.remit.mellonsecure.payout.publisher.TransferPublisher;
import com.remit.mellonsecure.payout.repository.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class BatchTransferService {

    private final MerchantLookupService merchantLookupService;
    private final PayoutTransactionJpaRepository payoutTransactionJpaRepository;
    private final TransactionRepository transactionRepository;
    private final BatchRepository batchRepository;
    private final LedgerClient ledgerClient;
    private final ProcessorAdapter processorAdapter;
    private final TransferPublisher transferPublisher;
    private final IdGenerator idGenerator;

    public BatchTransferResult execute(String merchantId, List<TransferCommand> transfers) {
        var merchant = merchantLookupService.findByMerchantIdFromCacheOrSync(merchantId)
                .orElseThrow(() -> new MerchantNotFoundException(merchantId));
        if (merchant.getStatus() != MerchantStatus.ACTIVE) {
            throw new MerchantInactiveException(merchantId);
        }

        String batchReference = idGenerator.generateBatchReference();
        String batchId = java.util.UUID.randomUUID().toString();

        PayoutBatch batch = PayoutBatch.builder()
                .id(batchId)
                .batchReference(batchReference)
                .merchantId(merchantId)
                .status(BatchStatus.PENDING)
                .totalCount(transfers.size())
                .successCount(0)
                .failedCount(0)
                .createdAt(Instant.now())
                .updatedAt(Instant.now())
                .build();
        batchRepository.save(batch);

        List<PayoutTransaction> created = new ArrayList<>();
        for (TransferCommand cmd : transfers) {
            try {
                if (payoutTransactionJpaRepository.existsByMerchantIdAndMerchantReference(merchantId, cmd.merchantReference())) {
                    continue;
                }
                if (!ledgerClient.hasSufficientBalance(merchant.getSourceAccountNumber(), cmd.amount())) {
                    continue;
                }
                var nameEnquiry = processorAdapter.performNameEnquiry(cmd.accountNumber(), cmd.bankCode());
                if (!nameEnquiry.isSuccess()) continue;

                String transactionId = java.util.UUID.randomUUID().toString();
                String paymentReference = idGenerator.generatePaymentReference();

                PayoutTransaction tx = PayoutTransaction.builder()
                        .id(transactionId)
                        .paymentReference(paymentReference)
                        .merchantReference(cmd.merchantReference())
                        .processorReference(null)
                        .merchantId(merchantId)
                        .amount(cmd.amount())
                        .accountNumber(cmd.accountNumber())
                        .bankCode(cmd.bankCode())
                        .accountName(nameEnquiry.getAccountName())
                        .status(TransactionStatus.PENDING)
                        .merchantPayload(null)
                        .processorResponse(null)
                        .createdAt(Instant.now())
                        .updatedAt(Instant.now())
                        .build();
                transactionRepository.save(tx);
                created.add(tx);

                TransferMessage message = TransferMessage.builder()
                        .transactionId(transactionId)
                        .paymentReference(paymentReference)
                        .merchantReference(cmd.merchantReference())
                        .merchantId(merchantId)
                        .accountNumber(cmd.accountNumber())
                        .bankCode(cmd.bankCode())
                        .accountName(nameEnquiry.getAccountName())
                        .nameEnquiryRef(nameEnquiry.getSessionId())
                        .amount(cmd.amount())
                        .narration(cmd.narration())
                        .sourceAccount(merchant.getSourceAccountNumber())
                        .build();
                transferPublisher.publish(message);
            } catch (Exception e) {
                log.warn("Batch item failed: merchantRef={}", cmd.merchantReference(), e);
            }
        }

        return new BatchTransferResult(batchReference, created.size(), created);
    }

    public record BatchTransferResult(String batchReference, int submittedCount, List<PayoutTransaction> transactions) {}

    public record TransferCommand(
            String merchantReference,
            String accountNumber,
            String bankCode,
            java.math.BigDecimal amount,
            String narration
    ) {}
}
