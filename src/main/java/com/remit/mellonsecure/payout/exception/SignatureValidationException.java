package com.remit.mellonsecure.payout.exception;

public class SignatureValidationException extends PayoutDomainException {
    public SignatureValidationException(String message) {
        super("SIGNATURE_INVALID", message);
    }
}
