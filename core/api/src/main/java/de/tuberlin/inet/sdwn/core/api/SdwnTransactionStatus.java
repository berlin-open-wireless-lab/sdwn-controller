package de.tuberlin.inet.sdwn.core.api;

/**
 * Callback status code returned by the {@code SdwnTransaction}'s {@code update} method after a message has
 * been processed.
 * - DONE means that the transaction has finished
 * - NEXT means that the transaction has finished and the next transaction in the transaction chain (if any) should be started
 * - CONTINUE means that the callback has processed the message but the transaction has not yet finished
 * - ABORT means that the transaction has finished unsuccessfully. If it is part of a transaction chain, the next transaction in the chain will not be started.
 * - SKIP means that the callback has not processed the message
 */
public enum SdwnTransactionStatus {
    DONE,
    NEXT,
    CONTINUE,
    SKIP
}
