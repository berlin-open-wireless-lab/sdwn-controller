package de.tuberlin.inet.sdwn.core.api;

import org.onosproject.openflow.controller.Dpid;
import org.projectfloodlight.openflow.protocol.OFMessage;

public interface SdwnTransactionTask {

    enum TransactionStatus {
        DONE,
        CONTINUE,
        SKIP
    }

    long ANY_XID = Long.MAX_VALUE;
    long NO_XID = Long.MIN_VALUE;

    long xid();
    void setXid(long xid);

    void setManager(SdwnTransactionManager m);
    void setController(SdwnCoreService s);
    TransactionStatus update(Dpid dpid, OFMessage msg);
    void timeout();
}
