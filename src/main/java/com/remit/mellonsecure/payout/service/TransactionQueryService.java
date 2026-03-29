package com.remit.mellonsecure.payout.service;

import com.remit.mellonsecure.payout.domain.exception.MerchantNotFoundException;
import com.remit.mellonsecure.payout.domain.exception.TransactionNotFoundException;
import com.remit.mellonsecure.payout.domain.model.PayoutTransaction;
import com.remit.mellonsecure.payout.domain.port.MerchantRepository;
import com.remit.mellonsecure.payout.domain.port.TransactionRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class TransactionQueryService {

    private final TransactionRepository transactionRepository;
    private final MerchantRepository merchantRepository;

    public PayoutTransaction execute(String merchantId, String transactionId) {
        merchantRepository.findById(merchantId)
                .orElseThrow(() -> new MerchantNotFoundException(merchantId));

        return transactionRepository.findByTransactionId(transactionId)
                .or(() -> transactionRepository.findByPaymentReference(transactionId))
                .filter(tx -> tx.getMerchantId().equals(merchantId))
                .orElseThrow(() -> new TransactionNotFoundException(transactionId));
    }
}
