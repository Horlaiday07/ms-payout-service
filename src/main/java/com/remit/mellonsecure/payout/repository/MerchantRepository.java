package com.remit.mellonsecure.payout.repository;

import com.remit.mellonsecure.payout.entity.Merchant;

import java.util.Optional;

/**
 * Port for merchant persistence.
 */
public interface MerchantRepository {

    Optional<Merchant> findById(String merchantId);

    Optional<Merchant> findByApiKey(String apiKey);

    boolean existsByMerchantReference(String merchantId, String merchantReference);
}
