package de.tuberlin.inet.sdwn.core.api;

import org.onosproject.openflow.controller.Dpid;
import org.projectfloodlight.openflow.protocol.OFMessage;

/**
 * An asynchronous transaction that requires an exchange between the
 * controller and one or multiple network elements.
 * The transaction is identified by its OpenFlow transaction ID (XID).
 */
public interface SdwnTransaction {

    long timeout();

    /**
     * Callback called whenever a message belonging to the transaction has been received.
     *
     * @param dpid the Datapath ID of the switch that sent the message
     * @param msg the message received
     * @return the appropriate {@code TransactionStatus}
     */
    SdwnTransactionStatus update(Dpid dpid, OFMessage msg);

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
