CREATE TABLE webhook_events (
    id VARCHAR(36) PRIMARY KEY,
    payment_reference VARCHAR(64) NOT NULL,
    merchant_reference VARCHAR(128) NOT NULL,
    webhook_url VARCHAR(512) NOT NULL,
    status VARCHAR(20) NOT NULL DEFAULT 'PENDING',
    retry_count INT NOT NULL DEFAULT 0,
    payload TEXT,
    error_message TEXT,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    next_retry_at TIMESTAMP WITH TIME ZONE
);

CREATE INDEX idx_webhook_events_status ON webhook_events(status);
CREATE INDEX idx_webhook_events_next_retry ON webhook_events(next_retry_at) WHERE status = 'PENDING';
CREATE INDEX idx_webhook_events_payment_ref ON webhook_events(payment_reference);

COMMENT ON TABLE webhook_events IS 'Webhook delivery tracking with retry support';
