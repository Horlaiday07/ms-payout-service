package com.remit.mellonsecure.payout.repository;

import com.remit.mellonsecure.payout.entity.MerchantEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface MerchantJpaRepository extends JpaRepository<MerchantEntity, String> {

    Optional<MerchantEntity> findByMerchantCode(String merchantCode);
}
