package com.remit.mellonsecure.payout.domain.port;

import com.remit.mellonsecure.payout.domain.model.WebhookPayload;

/**
 * Port for publishing webhook delivery events.
 */
public interface WebhookPublisher {

    void publish(WebhookPayload payload);
}
