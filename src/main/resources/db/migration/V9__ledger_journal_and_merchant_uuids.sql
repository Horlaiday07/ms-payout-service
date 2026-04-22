-- Payout journal link + merchant table: settlement UUID (36 chars) and ledger account ids

ALTER TABLE payout_transactions ADD COLUMN IF NOT EXISTS ledger_journal_id VARCHAR(36);

ALTER TABLE merchants ALTER COLUMN source_account_number TYPE VARCHAR(64);

ALTER TABLE merchants ADD COLUMN IF NOT EXISTS ledger_merchant_account_id VARCHAR(36);
ALTER TABLE merchants ADD COLUMN IF NOT EXISTS ledger_internal_account_id VARCHAR(36);
ALTER TABLE merchants ADD COLUMN IF NOT EXISTS ledger_settlement_account_id VARCHAR(36);

COMMENT ON COLUMN payout_transactions.ledger_journal_id IS 'ms-ledger-service PENDING journal id until POSTED/REVERSED';
