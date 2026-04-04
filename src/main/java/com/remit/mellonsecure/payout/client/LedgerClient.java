package com.remit.mellonsecure.payout.client;

import java.math.BigDecimal;

/**
 * Port for ledger/balance service.
 */
public interface LedgerClient {

    BigDecimal getBalance(String accountId);

    boolean hasSufficientBalance(String accountId, BigDecimal amount);

    void reserveFunds(String accountId, String paymentReference, BigDecimal amount);
}
