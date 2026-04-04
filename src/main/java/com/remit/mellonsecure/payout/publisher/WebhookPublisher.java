package com.remit.mellonsecure.payout.publisher;

import com.remit.mellonsecure.payout.entity.WebhookPayload;

/**
 * Port for publishing webhook delivery events.
 */
public interface WebhookPublisher {

    void publish(WebhookPayload payload);
}
