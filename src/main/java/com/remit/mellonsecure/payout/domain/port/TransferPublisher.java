package com.remit.mellonsecure.payout.domain.port;

import com.remit.mellonsecure.payout.domain.model.TransferMessage;

/**
 * Port for publishing transfer requests to queue.
 */
public interface TransferPublisher {

    void publish(TransferMessage message);
}
