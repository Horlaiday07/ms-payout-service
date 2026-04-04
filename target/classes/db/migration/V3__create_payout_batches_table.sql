CREATE TABLE payout_batches (
    id VARCHAR(36) PRIMARY KEY,
    batch_reference VARCHAR(64) NOT NULL UNIQUE,
    merchant_id VARCHAR(36) NOT NULL,
    status VARCHAR(30) NOT NULL DEFAULT 'PENDING',
    total_count INT DEFAULT 0,
    success_count INT DEFAULT 0,
    failed_count INT DEFAULT 0,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_batch_merchant FOREIGN KEY (merchant_id) REFERENCES merchants(id)
);

CREATE INDEX idx_payout_batches_merchant_id ON payout_batches(merchant_id);
CREATE INDEX idx_payout_batches_batch_ref ON payout_batches(batch_reference);
CREATE INDEX idx_payout_batches_status ON payout_batches(status);

COMMENT ON TABLE payout_batches IS 'Batch payout records';
