-- Business merchant code (separate from internal id used as X-Merchant-Id)
ALTER TABLE merchants ADD COLUMN IF NOT EXISTS merchant_code VARCHAR(64);

UPDATE merchants SET merchant_code = id WHERE merchant_code IS NULL;

ALTER TABLE merchants ALTER COLUMN merchant_code SET NOT NULL;

CREATE UNIQUE INDEX IF NOT EXISTS idx_merchants_merchant_code ON merchants(merchant_code);

-- Encrypted secrets can exceed 256 chars
ALTER TABLE merchants ALTER COLUMN secret_key TYPE VARCHAR(512);
