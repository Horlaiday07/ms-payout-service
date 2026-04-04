package com.remit.mellonsecure.payout.service;

import com.remit.mellonsecure.payout.entity.MerchantStatus;
import com.remit.mellonsecure.payout.exception.MerchantInactiveException;
import com.remit.mellonsecure.payout.exception.MerchantNotFoundException;
import com.remit.mellonsecure.payout.entity.Merchant;
import com.remit.mellonsecure.payout.entity.NameEnquiryResult;
import com.remit.mellonsecure.payout.repository.MerchantRepository;
import com.remit.mellonsecure.payout.processor.ProcessorAdapter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class NameEnquiryService {

    private final MerchantRepository merchantRepository;
    private final ProcessorAdapter processorAdapter;

    public NameEnquiryResult execute(String merchantId, String accountNumber, String bankCode) {
        Merchant merchant = merchantRepository.findById(merchantId)
                .orElseThrow(() -> new MerchantNotFoundException(merchantId));
        if (merchant.getStatus() != MerchantStatus.ACTIVE) {
            throw new MerchantInactiveException(merchantId);
        }
        return processorAdapter.performNameEnquiry(accountNumber, bankCode);
    }
}
