package com.remit.mellonsecure.payout.repository;

/**
 * Port for generating unique IDs (Snowflake or Redis atomic counter).
 */
public interface IdGenerator {

    String generatePaymentReference();

    String generateBatchReference();

    long nextId();
}
