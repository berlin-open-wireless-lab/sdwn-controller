package de.tuberlin.inet.sdwn.core.ctl.task;

import de.tuberlin.inet.sdwn.core.api.SdwnTransactionContext;
import de.tuberlin.inet.sdwn.core.api.entity.SdwnAccessPoint;
import de.tuberlin.inet.sdwn.core.api.entity.SdwnClient;
import de.tuberlin.inet.sdwn.core.ctl.entity.Client;
import de.tuberlin.inet.sdwn.core.api.DefaultSdwnTransactionContext;
import org.onosproject.openflow.controller.Dpid;
import org.projectfloodlight.openflow.protocol.OFMessage;
import org.projectfloodlight.openflow.protocol.OFSdwnGetClientsReply;
import org.projectfloodlight.openflow.protocol.OFStatsReplyFlags;

public class GetClientsQuery extends DefaultSdwnTransactionContext {

    private final Dpid dpid;
    private final String ap;
    private SdwnTransactionContext followUpTask;

    public GetClientsQuery(long xid, long timeout, String ap, Dpid dpid) {
        super(xid, timeout);
        this.ap = ap;
        this.dpid = dpid;
    }

    public GetClientsQuery(long xid, long timeout, String ap, Dpid dpid,
                           SdwnTransactionContext followUpTask) {
        super(xid, timeout);
        this.ap = ap;
        this.dpid = dpid;
        this.followUpTask = followUpTask;
    }

    @Override
    public TransactionStatus update(Dpid dpid, OFMessage msg) {

        if (!(msg instanceof OFSdwnGetClientsReply)) {
            return SdwnTransactionContext.TransactionStatus.SKIP;
        }

        OFSdwnGetClientsReply reply = (OFSdwnGetClientsReply) msg;

        SdwnAccessPoint ap = transactionManager.controller().apByDpidAndName(dpid, this.ap);
        if (ap == null) {
            return SdwnTransactionContext.TransactionStatus.DONE;
        }

        SdwnClient newClient = Client.fromGetClientsReply(ap, reply);
        if (newClient == null) {
            return SdwnTransactionContext.TransactionStatus.DONE;
        }

        transactionManager.controller().newClient(ap, newClient);
        boolean done = !reply.getFlags().contains(OFStatsReplyFlags.REPLY_MORE);
        if (done && followUpTask != null) {
            transactionManager.startTransaction(followUpTask);
        }

        return done ? SdwnTransactionContext.TransactionStatus.DONE : SdwnTransactionContext.TransactionStatus.CONTINUE;
    }

    @Override
    public void timeout() {
        log.info("Get Stations Query for AP {} on {} timed out. Maybe the AP does not have any clients.",
                 ap, dpid);
    }
}
