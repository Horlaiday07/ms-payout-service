package com.remit.mellonsecure.payout.infrastructure.persistence.repository;

import com.remit.mellonsecure.payout.infrastructure.persistence.entity.MerchantEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface MerchantJpaRepository extends JpaRepository<MerchantEntity, String> {

    Optional<MerchantEntity> findByApiKey(String apiKey);
}
