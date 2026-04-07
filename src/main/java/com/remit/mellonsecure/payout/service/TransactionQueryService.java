package com.remit.mellonsecure.payout.service;

import com.remit.mellonsecure.payout.exception.MerchantNotFoundException;
import com.remit.mellonsecure.payout.exception.TransactionNotFoundException;
import com.remit.mellonsecure.payout.entity.PayoutTransaction;
import com.remit.mellonsecure.payout.repository.TransactionRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class TransactionQueryService {

    private final TransactionRepository transactionRepository;
    private final MerchantLookupService merchantLookupService;

    public PayoutTransaction execute(String merchantId, String transactionId) {
        merchantLookupService.findByMerchantIdFromCacheOrSync(merchantId)
                .orElseThrow(() -> new MerchantNotFoundException(merchantId));

        return transactionRepository.findByTransactionId(transactionId)
                .or(() -> transactionRepository.findByPaymentReference(transactionId))
                .filter(tx -> tx.getMerchantId().equals(merchantId))
                .orElseThrow(() -> new TransactionNotFoundException(transactionId));
    }
}
