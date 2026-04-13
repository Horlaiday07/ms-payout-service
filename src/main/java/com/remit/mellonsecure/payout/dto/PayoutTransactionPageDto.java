package com.remit.mellonsecure.payout.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class PayoutTransactionPageDto {
    private List<PayoutTransactionListItemDto> content;
    private long totalElements;
    private int totalPages;
    private int page;
}
