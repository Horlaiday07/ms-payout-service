package com.remit.mellonsecure.payout.controller;

import com.remit.mellonsecure.payout.dto.*;
import com.remit.mellonsecure.payout.security.MerchantContext;
import com.remit.mellonsecure.payout.service.*;
import com.remit.mellonsecure.payout.exception.IdempotencyConflictException;
import com.remit.mellonsecure.payout.exception.PayoutDomainException;
import com.remit.mellonsecure.payout.entity.NameEnquiryResult;
import com.remit.mellonsecure.payout.entity.PayoutTransaction;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/v1/payout")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Payout API", description = "Fintech payout operations")
public class PayoutApiController {

    private final NameEnquiryService nameEnquiryService;
    private final TransferService transferService;
    private final BatchTransferService batchTransferService;
    private final TransactionQueryService transactionQueryService;
    private final MerchantDashboardQueryService merchantDashboardQueryService;

    @GetMapping("/banks")
    @Operation(summary = "List banks (code + name) for dropdowns")
    public ResponseEntity<List<Map<String, String>>> listBanks() {
        List<Map<String, String>> banks = new ArrayList<>();
        banks.add(bank("058", "Guaranty Trust Bank"));
        banks.add(bank("011", "First Bank of Nigeria"));
        banks.add(bank("033", "United Bank for Africa"));
        banks.add(bank("044", "Access Bank"));
        banks.add(bank("070", "Fidelity Bank"));
        banks.add(bank("057", "Zenith Bank"));
        return ResponseEntity.ok(banks);
    }

    private static Map<String, String> bank(String code, String name) {
        Map<String, String> m = new LinkedHashMap<>();
        m.put("bankCode", code);
        m.put("bankName", name);
        return m;
    }

    @GetMapping("/merchant-float-balance")
    @Operation(summary = "Current float balance (ledger) and last 10 payout transactions")
    public ResponseEntity<PayoutMerchantDashboardDto> merchantFloatBalance(
            @RequestHeader("X-Merchant-Id") String merchantId) {
        return ResponseEntity.ok(merchantDashboardQueryService.get(merchantId));
    }

    @PostMapping(value = "/name-enquiry", consumes = MediaType.APPLICATION_JSON_VALUE)
    @Operation(summary = "Bank account name enquiry")
    public ResponseEntity<StandardPayoutResponse> nameEnquiry(
            @RequestHeader("X-Merchant-Id") String merchantId,
            @Valid @RequestBody NameEnquiryRequest request) {
        log.info("Name enquiry: merchantId={}, accountNumber={}", mask(merchantId), mask(request.accountNumber()));
        NameEnquiryResult result = nameEnquiryService.execute(merchantId, request.accountNumber(), request.bankCode());
        StandardPayoutResponse response = StandardPayoutResponse.builder()
                .responseCode(result.isSuccess() ? "00" : "99")
                .responseDescription(result.getResponseMessage())
                .merchantReference(null)
                .paymentReference(null)
                .processorReference(result.getSessionId())
                .amount(null)
                .remarks(result.isSuccess() ? result.getAccountName() : "failed")
                .build();
        return ResponseEntity.ok(response);
    }

    @PostMapping(value = "/transfer", consumes = MediaType.APPLICATION_JSON_VALUE)
    @Operation(summary = "Single payout transfer")
    public ResponseEntity<StandardPayoutResponse> transfer(
            @RequestHeader("X-Merchant-Id") String merchantId,
            @RequestHeader(value = "Idempotency-Key", required = false) String idempotencyKey,
            @Valid @RequestBody TransferRequestDto request) {
        log.info("Transfer request: merchantId={}, merchantRef={}", mask(merchantId), request.merchantReference());
        TransferService.TransferCommand command = new TransferService.TransferCommand(
                merchantId,
                request.merchantReference(),
                idempotencyKey,
                request.accountNumber(),
                request.bankCode(),
                request.amount(),
                request.narration(),
                request.accountName(),
                request.nameEnquiryRef()
        );
        try {
            PayoutTransaction tx = transferService.execute(command);
            StandardPayoutResponse response = StandardPayoutResponse.builder()
                    .responseCode("00")
                    .responseDescription("SUCCESS")
                    .merchantReference(tx.getMerchantReference())
                    .paymentReference(tx.getPaymentReference())
                    .processorReference(tx.getProcessorReference())
                    .amount(tx.getAmount())
                    .remarks("PENDING")
                    .build();
            return ResponseEntity.ok(response);
        } catch (IdempotencyConflictException e) {
            if (e.getCachedResponse() != null) {
                return ResponseEntity.ok(parseCachedResponse(e.getCachedResponse()));
            }
            return ResponseEntity.status(HttpStatus.CONFLICT).body(
                    StandardPayoutResponse.builder()
                            .responseCode("99")
                            .responseDescription(e.getMessage())
                            .build()
            );
        }
    }

    @PostMapping(value = "/batch-transfer", consumes = MediaType.APPLICATION_JSON_VALUE)
    @Operation(summary = "Batch payout transfer")
    public ResponseEntity<BatchTransferResponse> batchTransfer(
            @RequestHeader("X-Merchant-Id") String merchantId,
            @Valid @RequestBody BatchTransferRequestDto request) {
        log.info("Batch transfer: merchantId={}, count={}", mask(merchantId), request.transfers().size());
        List<BatchTransferService.TransferCommand> commands = request.transfers().stream()
                .map(t -> new BatchTransferService.TransferCommand(
                        t.merchantReference(),
                        t.accountNumber(),
                        t.bankCode(),
                        t.amount(),
                        t.narration()
                ))
                .collect(Collectors.toList());
        BatchTransferService.BatchTransferResult result = batchTransferService.execute(merchantId, commands);
        List<StandardPayoutResponse> items = result.transactions().stream()
                .map(tx -> StandardPayoutResponse.builder()
                        .responseCode("00")
                        .responseDescription("SUCCESS")
                        .merchantReference(tx.getMerchantReference())
                        .paymentReference(tx.getPaymentReference())
                        .processorReference(tx.getProcessorReference())
                        .amount(tx.getAmount())
                        .remarks("PENDING")
                        .build())
                .collect(Collectors.toList());
        return ResponseEntity.ok(new BatchTransferResponse(result.batchReference(), items));
    }

    @GetMapping("/transactions/{transactionId}")
    @Operation(summary = "Query transaction status")
    public ResponseEntity<StandardPayoutResponse> getTransaction(
            @RequestHeader("X-Merchant-Id") String merchantId,
            @PathVariable String transactionId) {
        log.info("Transaction query: merchantId={}, transactionId={}", mask(merchantId), transactionId);
        PayoutTransaction tx = transactionQueryService.execute(merchantId, transactionId);
        return ResponseEntity.ok(StandardPayoutResponse.builder()
                .responseCode("00")
                .responseDescription("SUCCESS")
                .merchantReference(tx.getMerchantReference())
                .paymentReference(tx.getPaymentReference())
                .processorReference(tx.getProcessorReference())
                .amount(tx.getAmount())
                .remarks(tx.getStatus().name())
                .build());
    }

    @ExceptionHandler(PayoutDomainException.class)
    public ResponseEntity<StandardPayoutResponse> handleDomainException(PayoutDomainException ex) {
        log.warn("Domain exception: {}", ex.getMessage());
        return ResponseEntity.badRequest()
                .body(StandardPayoutResponse.builder()
                        .responseCode(ex.getCode())
                        .responseDescription(ex.getMessage())
                        .build());
    }

    private StandardPayoutResponse parseCachedResponse(String json) {
        try {
            return new com.fasterxml.jackson.databind.ObjectMapper().readValue(json, StandardPayoutResponse.class);
        } catch (Exception e) {
            return StandardPayoutResponse.builder()
                    .responseCode("00")
                    .responseDescription("SUCCESS")
                    .build();
        }
    }

    private String mask(String value) {
        if (value == null || value.length() <= 4) return "****";
        return value.substring(0, 2) + "***";
    }

    public record BatchTransferResponse(String batchReference, List<StandardPayoutResponse> transactions) {}
}
