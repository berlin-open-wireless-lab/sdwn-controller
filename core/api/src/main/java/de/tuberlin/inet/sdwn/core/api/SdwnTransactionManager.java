package de.tuberlin.inet.sdwn.core.api;

import org.onosproject.openflow.controller.Dpid;
import org.projectfloodlight.openflow.protocol.OFMessage;

import java.awt.font.TransformAttribute;

/**
 * Management context for {@code SdwnTransactionTask}s.
 */
public interface SdwnTransactionManager {

    /**
     * Get the SDWN controller that initiated the transaction.
     */
    SdwnCoreService controller();

    /**
     * Start a transaction.
     *
     * @param t the transaction
     */
    void startTransaction(SdwnTransactionTask t, long timeout);

    /**
     * Cancel a transaction
     *
     * @param t the transaction
     */
    void cancelTransaction(SdwnTransactionTask t);

    /**
     * Check if there is an ongoing transaction wit the given XID.
     */
    boolean ongoingTransaction(long xid);

    /**
     * To be called by the controller to trigger callbacks when a message is received.
     *
     * @param dpid the switch that sent the message
     * @param msg  the message
     * @return true if the message was handled some where, false otherwise
     */
    boolean msgReceived(Dpid dpid, OFMessage msg);
}
