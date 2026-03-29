package com.remit.mellonsecure.payout.domain.exception;

public class PayoutDomainException extends RuntimeException {

    private final String code;

    public PayoutDomainException(String message) {
        super(message);
        this.code = "PAYOUT_ERROR";
    }

    public PayoutDomainException(String code, String message) {
        super(message);
        this.code = code;
    }

    public PayoutDomainException(String message, Throwable cause) {
        super(message, cause);
        this.code = "PAYOUT_ERROR";
    }

    public String getCode() {
        return code;
    }
}
