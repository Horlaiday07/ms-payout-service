package com.remit.mellonsecure.payout.api.security;

import com.remit.mellonsecure.payout.domain.model.Merchant;

public final class MerchantContext {

    private static final ThreadLocal<Merchant> HOLDER = new ThreadLocal<>();

    private MerchantContext() {}

    public static void set(Merchant merchant) {
        HOLDER.set(merchant);
    }

    public static Merchant get() {
        return HOLDER.get();
    }

    public static void clear() {
        HOLDER.remove();
    }
}
