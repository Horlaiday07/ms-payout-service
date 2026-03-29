package com.remit.mellonsecure.payout.domain.exception;

public class MerchantNotFoundException extends PayoutDomainException {
    public MerchantNotFoundException(String merchantId) {
        super("MERCHANT_NOT_FOUND", "Merchant not found: " + merchantId);
    }
}
