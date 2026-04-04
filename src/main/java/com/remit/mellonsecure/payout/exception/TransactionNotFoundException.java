package com.remit.mellonsecure.payout.exception;

public class TransactionNotFoundException extends PayoutDomainException {
    public TransactionNotFoundException(String transactionId) {
        super("TRANSACTION_NOT_FOUND", "Transaction not found: " + transactionId);
    }
}
