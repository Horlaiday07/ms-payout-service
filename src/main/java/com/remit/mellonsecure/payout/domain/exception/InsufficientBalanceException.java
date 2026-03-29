package com.remit.mellonsecure.payout.domain.exception;

import java.math.BigDecimal;

public class InsufficientBalanceException extends PayoutDomainException {
    public InsufficientBalanceException(String merchantId, BigDecimal required) {
        super("INSUFFICIENT_BALANCE", "Merchant " + merchantId + " has insufficient balance for amount: " + required);
    }
}
