package com.remit.mellonsecure.payout.config;

import com.remit.mellonsecure.payout.dto.ErrorResponse;
import com.remit.mellonsecure.payout.dto.StandardPayoutResponse;
import com.remit.mellonsecure.payout.exception.*;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.time.Instant;
import java.util.stream.Collectors;

@RestControllerAdvice
@Slf4j
public class GlobalExceptionHandler {

    @ExceptionHandler(MerchantNotFoundException.class)
    public ResponseEntity<StandardPayoutResponse> handleMerchantNotFound(MerchantNotFoundException ex) {
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                .body(StandardPayoutResponse.builder()
                        .responseCode("401")
                        .responseDescription(ex.getMessage())
                        .build());
    }

    @ExceptionHandler(MerchantInactiveException.class)
    public ResponseEntity<StandardPayoutResponse> handleMerchantInactive(MerchantInactiveException ex) {
        return ResponseEntity.status(HttpStatus.FORBIDDEN)
                .body(StandardPayoutResponse.builder()
                        .responseCode("403")
                        .responseDescription(ex.getMessage())
                        .build());
    }

    @ExceptionHandler(SignatureValidationException.class)
    public ResponseEntity<StandardPayoutResponse> handleSignatureInvalid(SignatureValidationException ex) {
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                .body(StandardPayoutResponse.builder()
                        .responseCode("401")
                        .responseDescription(ex.getMessage())
                        .build());
    }

    @ExceptionHandler(InsufficientBalanceException.class)
    public ResponseEntity<StandardPayoutResponse> handleInsufficientBalance(InsufficientBalanceException ex) {
        return ResponseEntity.badRequest()
                .body(StandardPayoutResponse.builder()
                        .responseCode("51")
                        .responseDescription(ex.getMessage())
                        .build());
    }

    @ExceptionHandler(TransactionNotFoundException.class)
    public ResponseEntity<StandardPayoutResponse> handleTransactionNotFound(TransactionNotFoundException ex) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(StandardPayoutResponse.builder()
                        .responseCode("404")
                        .responseDescription(ex.getMessage())
                        .build());
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<StandardPayoutResponse> handleValidation(MethodArgumentNotValidException ex) {
        String message = ex.getBindingResult().getFieldErrors().stream()
                .map(e -> e.getField() + ": " + e.getDefaultMessage())
                .collect(Collectors.joining(", "));
        return ResponseEntity.badRequest()
                .body(StandardPayoutResponse.builder()
                        .responseCode("30")
                        .responseDescription("Validation failed: " + message)
                        .build());
    }
}
