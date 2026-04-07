-- Payment dashboard seeds business id MERCH001 (backend V6). V7 copied id into merchant_code as
-- merchant-001, so X-Merchant-Id: MERCH001 never matched. Align the dev seed row for local integration.
UPDATE merchants
SET merchant_code = 'MERCH001'
WHERE id = 'merchant-001' AND merchant_code = 'merchant-001';
