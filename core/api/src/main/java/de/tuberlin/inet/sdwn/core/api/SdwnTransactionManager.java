package de.tuberlin.inet.sdwn.core.api;

import org.onosproject.openflow.controller.Dpid;
import org.projectfloodlight.openflow.protocol.OFMessage;

public interface SdwnTransactionManager {

    SdwnCoreService controller();

    void startTransaction(SdwnTransactionTask t);

    void cancelTransaction(SdwnTransactionTask t);

    boolean ongoingTransaction(long xid);

    boolean msgReceived(Dpid dpid, OFMessage msg);
}
