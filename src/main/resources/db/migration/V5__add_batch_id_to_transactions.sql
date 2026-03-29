ALTER TABLE payout_transactions ADD COLUMN batch_id VARCHAR(36);
ALTER TABLE payout_transactions ADD CONSTRAINT fk_transaction_batch FOREIGN KEY (batch_id) REFERENCES payout_batches(id);
CREATE INDEX idx_payout_trans_batch_id ON payout_transactions(batch_id);
