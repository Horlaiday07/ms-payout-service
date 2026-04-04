package com.remit.mellonsecure.payout.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.remit.mellonsecure.payout.entity.MerchantStatus;
import com.remit.mellonsecure.payout.entity.TransactionStatus;
import com.remit.mellonsecure.payout.entity.*;
import com.remit.mellonsecure.payout.exception.*;
import com.remit.mellonsecure.payout.client.LedgerClient;
import com.remit.mellonsecure.payout.processor.ProcessorAdapter;
import com.remit.mellonsecure.payout.publisher.TransferPublisher;
import com.remit.mellonsecure.payout.repository.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.Instant;

@Service
@RequiredArgsConstructor
@Slf4j
public class TransferService {

    private final MerchantRepository merchantRepository;
    private final TransactionRepository transactionRepository;
    private final LedgerClient ledgerClient;
    private final ProcessorAdapter processorAdapter;
    private final TransferPublisher transferPublisher;
    private final IdGenerator idGenerator;
    private final IdempotencyStore idempotencyStore;
    private final ObjectMapper objectMapper;

    private static final long IDEMPOTENCY_TTL_SECONDS = 86400; // 24 hours

    public PayoutTransaction execute(TransferCommand command) {
        Merchant merchant = validateMerchant(command.merchantId());
        validateCommand(command);
        String merchantRef = command.merchantReference() != null && !command.merchantReference().isBlank()
                ? command.merchantReference()
                : idGenerator.generatePaymentReference();

        if (merchantRepository.existsByMerchantReference(command.merchantId(), merchantRef)) {
            return transactionRepository.findByMerchantIdAndMerchantReference(command.merchantId(), merchantRef)
                    .orElseThrow(() -> new IdempotencyConflictException(merchantRef));
        }

        String idempotencyKey = resolveIdempotencyKey(command);
        if (idempotencyKey != null && !idempotencyKey.isBlank()) {
            String cachedResponse = idempotencyStore.get(idempotencyKey);
            if (cachedResponse != null) {
                throw new IdempotencyConflictException(idempotencyKey, cachedResponse);
            }
        }

        String paymentReference = idGenerator.generatePaymentReference();

        if (!ledgerClient.hasSufficientBalance(merchant.getSourceAccountNumber(), command.amount())) {
            throw new InsufficientBalanceException(command.merchantId(), command.amount());
        }

        NameEnquiryResult nameEnquiry = processorAdapter.performNameEnquiry(command.accountNumber(), command.bankCode());
        if (!nameEnquiry.isSuccess()) {
            throw new PayoutDomainException("NAME_ENQUIRY_FAILED", nameEnquiry.getResponseMessage());
        }

        String transactionId = java.util.UUID.randomUUID().toString();
        String payloadJson = serializePayload(command);

        PayoutTransaction transaction = PayoutTransaction.builder()
                .id(transactionId)
                .paymentReference(paymentReference)
                .merchantReference(merchantRef)
                .processorReference(null)
                .merchantId(command.merchantId())
                .amount(command.amount())
                .accountNumber(command.accountNumber())
                .bankCode(command.bankCode())
                .accountName(nameEnquiry.getAccountName())
                .status(TransactionStatus.PENDING)
                .merchantPayload(payloadJson)
                .processorResponse(null)
                .createdAt(Instant.now())
                .updatedAt(Instant.now())
                .build();

        transactionRepository.save(transaction);

        TransferMessage message = TransferMessage.builder()
                .transactionId(transactionId)
                .paymentReference(paymentReference)
                .merchantReference(merchantRef)
                .merchantId(command.merchantId())
                .accountNumber(command.accountNumber())
                .bankCode(command.bankCode())
                .accountName(nameEnquiry.getAccountName())
                .nameEnquiryRef(nameEnquiry.getSessionId())
                .amount(command.amount())
                .narration(command.narration())
                .sourceAccount(merchant.getSourceAccountNumber())
                .build();

        transferPublisher.publish(message);
        String responseJson = buildSuccessResponse(paymentReference, merchantRef, null, command.amount(), "PENDING");
        if (idempotencyKey != null && !idempotencyKey.isBlank()) {
            idempotencyStore.tryStore(idempotencyKey, responseJson, IDEMPOTENCY_TTL_SECONDS);
        }

        return transaction;
    }

    private Merchant validateMerchant(String merchantId) {
        Merchant merchant = merchantRepository.findById(merchantId)
                .orElseThrow(() -> new MerchantNotFoundException(merchantId));
        if (merchant.getStatus() != MerchantStatus.ACTIVE) {
            throw new MerchantInactiveException(merchantId);
        }
        return merchant;
    }

    private void validateCommand(TransferCommand command) {
        if (command.amount() == null || command.amount().compareTo(BigDecimal.ZERO) <= 0) {
            throw new PayoutDomainException("INVALID_AMOUNT", "Amount must be greater than zero");
        }
        if (command.bankCode() == null || command.bankCode().isBlank()) {
            throw new PayoutDomainException("INVALID_BANK_CODE", "Valid bank code is required");
        }
        if (command.accountNumber() == null || command.accountNumber().isBlank()) {
            throw new PayoutDomainException("INVALID_ACCOUNT", "Account number is required");
        }
    }

    private String resolveIdempotencyKey(TransferCommand command) {
        return command.idempotencyKey() != null && !command.idempotencyKey().isBlank()
                ? command.idempotencyKey()
                : command.merchantReference();
    }

    private String serializePayload(TransferCommand command) {
        try {
            return objectMapper.writeValueAsString(command);
        } catch (JsonProcessingException e) {
            return "{}";
        }
    }

    private String buildSuccessResponse(String paymentRef, String merchantRef, String processorRef, BigDecimal amount, String remarks) {
        try {
            return objectMapper.writeValueAsString(new StandardResponse("00", "SUCCESS", merchantRef, paymentRef, processorRef, amount, remarks));
        } catch (JsonProcessingException e) {
            return "{}";
        }
    }

    public record TransferCommand(
            String merchantId,
            String merchantReference,
            String idempotencyKey,
            String accountNumber,
            String bankCode,
            BigDecimal amount,
            String narration
    ) {}

    public record StandardResponse(
            String responseCode,
            String responseDescription,
            String merchantReference,
            String paymentReference,
            String processorReference,
            BigDecimal amount,
            String remarks
    ) {}
}
