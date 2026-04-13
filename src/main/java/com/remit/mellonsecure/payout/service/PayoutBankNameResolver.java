package com.remit.mellonsecure.payout.service;

import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;

/** Resolves bank codes to names (same catalogue as {@link com.remit.mellonsecure.payout.controller.PayoutApiController#listBanks}). */
@Component
public class PayoutBankNameResolver {

    private static final Map<String, String> CODE_TO_NAME = new HashMap<>();

    static {
        put("058", "Guaranty Trust Bank");
        put("011", "First Bank of Nigeria");
        put("033", "United Bank for Africa");
        put("044", "Access Bank");
        put("070", "Fidelity Bank");
        put("057", "Zenith Bank");
        put("076", "Polaris Bank");
        put("221", "Stanbic IBTC Bank");
        put("232", "Sterling Bank");
        put("032", "Union Bank");
        put("035", "Wema Bank");
    }

    private static void put(String code, String name) {
        CODE_TO_NAME.put(code, name);
    }

    public String nameForCode(String bankCode) {
        if (bankCode == null || bankCode.isBlank()) {
            return "";
        }
        return CODE_TO_NAME.getOrDefault(bankCode.trim(), bankCode.trim());
    }
}
