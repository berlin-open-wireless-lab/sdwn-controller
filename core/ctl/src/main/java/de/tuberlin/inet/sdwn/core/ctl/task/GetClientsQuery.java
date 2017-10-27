package de.tuberlin.inet.sdwn.core.ctl.task;

import de.tuberlin.inet.sdwn.core.api.SdwnTransactionTask;
import de.tuberlin.inet.sdwn.core.api.entity.SdwnAccessPoint;
import de.tuberlin.inet.sdwn.core.api.entity.SdwnClient;
import de.tuberlin.inet.sdwn.core.ctl.entity.Client;
import de.tuberlin.inet.sdwn.core.api.DefaultSdwnTransaction;
import org.onosproject.openflow.controller.Dpid;
import org.projectfloodlight.openflow.protocol.OFMessage;
import org.projectfloodlight.openflow.protocol.OFSdwnGetClientsReply;
import org.projectfloodlight.openflow.protocol.OFStatsReplyFlags;

public class GetClientsQuery extends DefaultSdwnTransaction {

    private final Dpid dpid;
    private final String ap;

    public GetClientsQuery(long xid, String ap, Dpid dpid) {
        super(xid);
        this.ap = ap;
        this.dpid = dpid;
    }

    public GetClientsQuery(long xid, String ap, Dpid dpid,
                           SdwnTransactionTask followUpTask) {
        super(xid, followUpTask);
        this.ap = ap;
        this.dpid = dpid;
    }

    @Override
    public TransactionStatus update(Dpid dpid, OFMessage msg) {

        if (!(msg instanceof OFSdwnGetClientsReply)) {
            return SdwnTransactionTask.TransactionStatus.SKIP;
        }

        OFSdwnGetClientsReply reply = (OFSdwnGetClientsReply) msg;

        SdwnAccessPoint ap = transactionManager.controller().apByDpidAndName(dpid, this.ap);
        if (ap == null) {
            return SdwnTransactionTask.TransactionStatus.DONE;
        }

        SdwnClient newClient = Client.fromGetClientsReply(ap, reply);
        if (newClient == null) {
            return SdwnTransactionTask.TransactionStatus.DONE;
        }

        transactionManager.controller().newClient(ap, newClient);
        boolean done = !reply.getFlags().contains(OFStatsReplyFlags.REPLY_MORE);
        return done ? SdwnTransactionTask.TransactionStatus.DONE : SdwnTransactionTask.TransactionStatus.CONTINUE;
    }

    @Override
    public void timeout() {
        log.info("Get Stations Query for AP {} on {} timed out. Maybe the AP does not have any clients.",
                 ap, dpid);
    }
}
