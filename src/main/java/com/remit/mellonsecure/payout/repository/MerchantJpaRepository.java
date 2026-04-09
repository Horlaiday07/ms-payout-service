package com.remit.mellonsecure.payout.repository;

import com.remit.mellonsecure.payout.entity.MerchantEntity;
import org.springframework.data.jpa.repository.JpaRepository;

public interface MerchantJpaRepository extends JpaRepository<MerchantEntity, String> {
}
