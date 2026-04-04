CREATE TABLE merchants (
    id VARCHAR(36) PRIMARY KEY,
    name VARCHAR(255) NOT NULL,
    api_key VARCHAR(64) NOT NULL UNIQUE,
    secret_key VARCHAR(256) NOT NULL,
    webhook_url VARCHAR(512),
    processor_id VARCHAR(64),
    source_account_number VARCHAR(20),
    source_bank_code VARCHAR(10),
    status VARCHAR(20) NOT NULL DEFAULT 'ACTIVE',
    whitelisted_ips TEXT,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX idx_merchants_api_key ON merchants(api_key);
CREATE INDEX idx_merchants_status ON merchants(status);

COMMENT ON TABLE merchants IS 'Merchant accounts for payout platform';
COMMENT ON COLUMN merchants.whitelisted_ips IS 'Comma-separated list of whitelisted IP addresses';
