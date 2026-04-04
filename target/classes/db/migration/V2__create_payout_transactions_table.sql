CREATE TABLE payout_transactions (
    id VARCHAR(36) PRIMARY KEY,
    payment_reference VARCHAR(64) NOT NULL UNIQUE,
    merchant_reference VARCHAR(128) NOT NULL,
    processor_reference VARCHAR(64),
    merchant_id VARCHAR(36) NOT NULL,
    amount DECIMAL(19, 4) NOT NULL,
    account_number VARCHAR(20) NOT NULL,
    bank_code VARCHAR(10) NOT NULL,
    account_name VARCHAR(255),
    status VARCHAR(20) NOT NULL DEFAULT 'PENDING',
    merchant_payload TEXT,
    processor_response TEXT,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_merchant FOREIGN KEY (merchant_id) REFERENCES merchants(id)
);

CREATE UNIQUE INDEX idx_payout_trans_merchant_ref ON payout_transactions(merchant_id, merchant_reference);
CREATE INDEX idx_payout_trans_payment_ref ON payout_transactions(payment_reference);
CREATE INDEX idx_payout_trans_merchant_id ON payout_transactions(merchant_id);
CREATE INDEX idx_payout_trans_status ON payout_transactions(status);
CREATE INDEX idx_payout_trans_created_at ON payout_transactions(created_at);

COMMENT ON TABLE payout_transactions IS 'Payout transaction records';
