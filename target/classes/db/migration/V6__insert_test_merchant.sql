-- Seed a test merchant for development (replace in production with proper onboarding)
INSERT INTO merchants (id, name, api_key, secret_key, webhook_url, processor_id, source_account_number, source_bank_code, status, whitelisted_ips, created_at, updated_at)
VALUES (
    'merchant-001',
    'Test Merchant',
    'test-api-key-12345',
    'test-secret-key-for-hmac',
    NULL,
    'NIBSS',
    '1234567890',
    '058',
    'ACTIVE',
    '',  -- empty = allow all IPs for dev
    CURRENT_TIMESTAMP,
    CURRENT_TIMESTAMP
) ON CONFLICT (id) DO NOTHING;
