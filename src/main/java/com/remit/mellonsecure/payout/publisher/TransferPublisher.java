package com.remit.mellonsecure.payout.publisher;

import com.remit.mellonsecure.payout.entity.TransferMessage;

/**
 * Port for publishing transfer requests to queue.
 */
public interface TransferPublisher {

    void publish(TransferMessage message);
}
