package com.remit.mellonsecure.payout.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;

public record NameEnquiryRequest(
        @NotBlank(message = "Account number is required")
        @Pattern(regexp = "\\d{10}", message = "Account number must be 10 digits")
        String accountNumber,
        @NotBlank(message = "Bank code is required")
        @Pattern(regexp = "\\d{3}", message = "Bank code must be 3 digits")
        String bankCode
) {}
