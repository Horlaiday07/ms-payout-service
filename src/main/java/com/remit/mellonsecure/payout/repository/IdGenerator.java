package com.remit.mellonsecure.payout.repository;

/**
 * Port for generating unique IDs (Snowflake or Redis atomic counter).
 */
public interface IdGenerator {

    String generatePaymentReference();

    String generateBatchReference();

    /**
     * NIBSS-style numeric id for name enquiry / NIP (e.g. 100005…30 digits).
     */
    String generateNipTransactionId();

    long nextId();
}
