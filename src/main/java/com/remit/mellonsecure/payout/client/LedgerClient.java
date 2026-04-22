package com.remit.mellonsecure.payout.client;

import java.math.BigDecimal;
import java.util.UUID;

/**
 * ms-ledger-service public API ({@code /api/public/ledger/**}) — float reserve, journals, balances.
 */
public interface LedgerClient {

    boolean isEnabled();

    BigDecimal getBalance(UUID ledgerAccountId);

    boolean hasSufficientBalance(UUID ledgerAccountId, BigDecimal amount);

    void reserveFunds(UUID merchantLedgerAccountId, String paymentReference, BigDecimal amount);

    /**
     * PAYOUT journal: DEBIT merchant, CREDIT settlement; links to reserve.
     */
    UUID createPayoutJournal(
            String paymentReference,
            UUID merchantLedgerAccountId,
            UUID settlementLedgerAccountId,
            BigDecimal amount,
            String currency);

    void postJournal(UUID journalId);

    void reverseJournal(UUID journalId);
}
