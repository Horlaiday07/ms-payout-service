package com.remit.mellonsecure.payout.processor;

import com.remit.mellonsecure.payout.entity.NameEnquiryResult;
import com.remit.mellonsecure.payout.entity.TransferRequest;
import com.remit.mellonsecure.payout.entity.TransferResult;
import com.remit.mellonsecure.payout.entity.TransactionQueryResult;

/**
 * Port for external payment processor (NIBSS NIP, etc.)
 */
public interface ProcessorAdapter {

    NameEnquiryResult performNameEnquiry(String accountNumber, String bankCode);

    TransferResult performTransfer(TransferRequest request);

    TransactionQueryResult queryTransaction(String processorReference);
}
