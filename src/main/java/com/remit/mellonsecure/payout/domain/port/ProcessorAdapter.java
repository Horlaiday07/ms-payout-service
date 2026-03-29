package com.remit.mellonsecure.payout.domain.port;

import com.remit.mellonsecure.payout.domain.model.NameEnquiryResult;
import com.remit.mellonsecure.payout.domain.model.TransferRequest;
import com.remit.mellonsecure.payout.domain.model.TransferResult;
import com.remit.mellonsecure.payout.domain.model.TransactionQueryResult;

/**
 * Port for external payment processor (NIBSS NIP, etc.)
 */
public interface ProcessorAdapter {

    NameEnquiryResult performNameEnquiry(String accountNumber, String bankCode);

    TransferResult performTransfer(TransferRequest request);

    TransactionQueryResult queryTransaction(String processorReference);
}
