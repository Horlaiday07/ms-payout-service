package com.remit.mellonsecure.payout.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;

import java.math.BigDecimal;

public record TransferRequestDto(
        @NotBlank(message = "Merchant reference is required")
        String merchantReference,
        @NotBlank(message = "Account number is required")
        @Pattern(regexp = "\\d{10}", message = "Account number must be 10 digits")
        String accountNumber,
        @NotBlank(message = "Bank code is required")
        @Pattern(regexp = "\\d{3}", message = "Bank code must be 3 digits")
        String bankCode,
        @NotNull(message = "Amount is required")
        @DecimalMin(value = "0.01", message = "Amount must be greater than zero")
        BigDecimal amount,
        String narration
) {}
