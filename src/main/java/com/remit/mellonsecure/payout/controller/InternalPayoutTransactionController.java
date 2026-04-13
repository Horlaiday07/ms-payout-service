package com.remit.mellonsecure.payout.controller;

import com.remit.mellonsecure.payout.dto.PayoutTransactionPageDto;
import com.remit.mellonsecure.payout.service.PayoutTransactionListService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;

/**
 * Internal REST API for payment dashboard (secured by {@code X-Payout-Internal-Key}).
 * Lists rows from {@code payout_transactions} with filters and pagination.
 */
@RestController
@RequestMapping("/api/internal/payout-transactions")
@RequiredArgsConstructor
@Tag(name = "Internal payout transactions", description = "Payment dashboard only (X-Payout-Internal-Key)")
public class InternalPayoutTransactionController {

    private final PayoutTransactionListService payoutTransactionListService;

    @GetMapping
    @Operation(summary = "Search payout transactions (filters + pagination)")
    public ResponseEntity<PayoutTransactionPageDto> list(
            @RequestParam(required = false) String merchantCode,
            @RequestParam(defaultValue = "false") boolean merchantCodeExact,
            @RequestParam(required = false) String paymentReference,
            @RequestParam(required = false) String merchantReference,
            @RequestParam(required = false) String accountNumber,
            @RequestParam(required = false) String bankName,
            @RequestParam(required = false) String status,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fromDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate toDate,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        return ResponseEntity.ok(payoutTransactionListService.search(
                merchantCode,
                merchantCodeExact,
                paymentReference,
                merchantReference,
                accountNumber,
                bankName,
                status,
                fromDate,
                toDate,
                page,
                size));
    }
}
