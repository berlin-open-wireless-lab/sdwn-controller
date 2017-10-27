package de.tuberlin.inet.sdwn.core.api;

import org.onosproject.openflow.controller.Dpid;
import org.projectfloodlight.openflow.protocol.OFMessage;

/**
 * Context for an asynchronous transaction that requires an exchange between the
 * controller and one or multiple network elements.
 * The transaction is identified by its OpenFlow transaction ID (XID) and may
 * specify a timeout.
 */
public interface SdwnTransactionContext {

    /**
     * Callback status code returned by the {@code update} method after a message has
     * been received.
     * - DONE means that the transaction has finished
     * - NEXT means that the transaction has finished and its follow-up task should be started
     * - CONTINUE means that the callback has processed the message but the transaction has not yet finished
     * - SKIP means that the callback has not processed the message
     */
    enum TransactionStatus {
        DONE,
        NEXT,
        CONTINUE,
        SKIP,
    }


    /**
     * Constant matching all XIDs.
     */
    long ANY_XID = Long.MAX_VALUE;

    /**
     * Constant matching no XIDs. This is only intended as an initial value to
     * be overwritten when the transaction starts.
     */
    long NO_XID = Long.MIN_VALUE;

    /**
     * Getter for the transaction ID.
     */
    long xid();

    /**
     * Setter for the transaction ID.
     */
    void setXid(long xid);

    /**
     * Set the {@code SdwnTransactionManager} that is responsible for managing the transaction.
     */
    void setManager(SdwnTransactionManager m);

    /**
     * Callback method that is invoked by the transaction's {@code SdwnTransactionManager}
     * whenever a message matching the transaction's XID is received.
     *
     * @param dpid Datapath ID of the switch that sent the message.
     * @param msg the received message
     * @return transaction status code
     */
    TransactionStatus update(Dpid dpid, OFMessage msg);

    /**
     * Check whether this task has a followup task.
     */
    boolean hasFollowupTask();

    /**
     * Set the followup task.
     */
    SdwnTransactionContext setFollowupTask(SdwnTransactionContext t);

    /**
     * Return the followup task.
     */
    SdwnTransactionContext followupTask();

    /**
     * Callback invoked by the {@code SdwnTransactionManager} after a transaction has timed out.
     */
    void timeout();

    /**
     * Callback invoked by the {@code SdwnTransactionManager} after a transaction has finished.
     */
    void done();
}
