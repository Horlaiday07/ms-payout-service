package com.remit.mellonsecure.payout.exception;

public class MerchantInactiveException extends PayoutDomainException {
    public MerchantInactiveException(String merchantId) {
        super("MERCHANT_INACTIVE", "Merchant is not active: " + merchantId);
    }
}
