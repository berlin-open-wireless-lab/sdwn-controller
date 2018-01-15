package de.tuberlin.inet.sdwn.core.api;

import org.onosproject.openflow.controller.Dpid;
import org.projectfloodlight.openflow.protocol.OFMessage;

/**
 * An asynchronous transaction that requires an exchange between the
 * controller and one or multiple network elements.
 * The transaction is identified by its OpenFlow transaction ID (XID).
 */
public interface SdwnTransaction {

    /**
     * Callback status code returned by the {@code update} method after a message has
     * been processed.
     * - DONE means that the transaction has finished
     * - NEXT means that the transaction has finished and the next transaction in the transaction chain (if any) should be started
     * - CONTINUE means that the callback has processed the message but the transaction has not yet finished
     * - ABORT means that the transaction has finished unsuccessfully. If it is part of a transaction chain, the next transaction in the chain will not be started.
     * - SKIP means that the callback has not processed the message
     */
    enum TransactionStatus {
        DONE,
        NEXT,
        CONTINUE,
        SKIP,
    }

    long timeout();

    /**
     * Callback to be called regularly whenever a message belonging to the transaction has been received.
     *
     * @param dpid the Datapath ID of the switch that sent the message
     * @param msg the message received
     * @return the appropriate {@code TransactionStatus}
     */
    TransactionStatus update(Dpid dpid, OFMessage msg);

    /**
     * Called once when the transaction is started.
     *
     * @param xid the XID that has been assigned to the transaction
     */
    void start(long xid);

    /**
     * Called when the transaction has timed out.
     */
    void timedOut();

    /**
     * Called after the transaction has been aborted.
     */
    void aborted();

    /**
     * Called once after the transaction has finished.
     */
    void done();
}
