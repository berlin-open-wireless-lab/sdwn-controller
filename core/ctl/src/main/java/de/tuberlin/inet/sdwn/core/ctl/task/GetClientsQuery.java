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

    public GetClientsQuery(long xid, String ap, Dpid dpid) {
        super(xid);
        this.ap = ap;
        this.dpid = dpid;
    }

    public GetClientsQuery(long xid, String ap, Dpid dpid,
                           SdwnTransactionContext followUpTask) {
        super(xid, followUpTask);
        this.ap = ap;
        this.dpid = dpid;
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
        return done ? SdwnTransactionContext.TransactionStatus.DONE : SdwnTransactionContext.TransactionStatus.CONTINUE;
    }

    @Override
    public void timeout() {
        log.info("Get Stations Query for AP {} on {} timed out. This could just mean that the AP does not have any associated clients. There is no explicit signalling for that case, yet.",
                ap, dpid);
    }
}
