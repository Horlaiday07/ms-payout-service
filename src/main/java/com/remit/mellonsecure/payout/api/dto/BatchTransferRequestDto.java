package com.remit.mellonsecure.payout.api.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;

import java.util.List;

public record BatchTransferRequestDto(
        @NotEmpty(message = "Transfers list cannot be empty")
        @Valid
        List<TransferRequestDto> transfers
) {}
