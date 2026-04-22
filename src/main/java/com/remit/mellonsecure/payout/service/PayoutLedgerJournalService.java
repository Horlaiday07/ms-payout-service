package com.remit.mellonsecure.payout.service;

import com.remit.mellonsecure.payout.client.LedgerClient;
import com.remit.mellonsecure.payout.entity.Merchant;
import com.remit.mellonsecure.payout.exception.InsufficientBalanceException;
import com.remit.mellonsecure.payout.exception.PayoutDomainException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.math.BigDecimal;
import java.util.UUID;

/**
 * Reserve + create PENDING PAYOUT journal on ms-ledger-service (merchant UUID debit, settlement UUID credit).
 */
@Service
@RequiredArgsConstructor
public class PayoutLedgerJournalService {

    private final LedgerClient ledgerClient;

    /**
     * @return journal id, or {@code null} when ledger integration is disabled
     */
    public UUID createPendingJournal(Merchant merchant, String paymentReference, BigDecimal amount, String currency) {
        if (!ledgerClient.isEnabled()) {
            return null;
        }
        String lm = merchant.getLedgerMerchantAccountId();
        String st = merchant.getLedgerSettlementAccountId();
        if (!StringUtils.hasText(lm) || !StringUtils.hasText(st)) {
            throw new PayoutDomainException("LEDGER_NOT_CONFIGURED",
                    "Merchant is missing ledgerMerchantAccountId or ledgerSettlementAccountId; sync from payment dashboard after provisioning.");
        }
        UUID merchantLeg = UUID.fromString(lm.trim());
        UUID settlementLeg = UUID.fromString(st.trim());
        String ccy = currency != null && !currency.isBlank() ? currency.trim().toUpperCase() : "NGN";

        if (!ledgerClient.hasSufficientBalance(merchantLeg, amount)) {
            throw new InsufficientBalanceException(merchant.getId(), amount);
        }
        ledgerClient.reserveFunds(merchantLeg, paymentReference, amount);
        return ledgerClient.createPayoutJournal(paymentReference, merchantLeg, settlementLeg, amount, ccy);
    }
}
