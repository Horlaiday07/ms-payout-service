package com.remit.mellonsecure.payout.domain.exception;

public class MerchantInactiveException extends PayoutDomainException {
    public MerchantInactiveException(String merchantId) {
        super("MERCHANT_INACTIVE", "Merchant is not active: " + merchantId);
    }
}
