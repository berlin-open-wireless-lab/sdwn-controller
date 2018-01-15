package de.tuberlin.inet.sdwn.core.ctl.task;

import de.tuberlin.inet.sdwn.core.api.SdwnCoreService;
import de.tuberlin.inet.sdwn.core.api.SdwnTransaction;
import de.tuberlin.inet.sdwn.core.api.entity.SdwnAccessPoint;
import de.tuberlin.inet.sdwn.core.api.entity.SdwnClient;
import de.tuberlin.inet.sdwn.core.ctl.entity.Client;
import de.tuberlin.inet.sdwn.core.api.SdwnTransactionAdapter;
import org.onosproject.openflow.controller.Dpid;
import org.projectfloodlight.openflow.protocol.OFMessage;
import org.projectfloodlight.openflow.protocol.OFSdwnGetClientsReply;
import org.projectfloodlight.openflow.protocol.OFStatsReplyFlags;

public class GetClientsQuery extends SdwnTransactionAdapter {

    private final Dpid dpid;
    private final String ap;
    private SdwnCoreService controller;
    private final long timeout;

    public GetClientsQuery(String ap, Dpid dpid, SdwnCoreService controller, long timeout) {
        this.ap = ap;
        this.dpid = dpid;
        this.controller = controller;
        this.timeout = timeout;
    }

    @Override
    public long timeout() {
        return timeout;
    }

    @Override
    public TransactionStatus update(Dpid dpid, OFMessage msg) {

        if (!(msg instanceof OFSdwnGetClientsReply)) {
            return SdwnTransaction.TransactionStatus.SKIP;
        }

        OFSdwnGetClientsReply reply = (OFSdwnGetClientsReply) msg;

        SdwnAccessPoint ap = controller.apByDpidAndName(dpid, this.ap);
        if (ap == null) {
            return SdwnTransaction.TransactionStatus.DONE;
        }

        SdwnClient newClient = Client.fromGetClientsReply(ap, reply);
        if (newClient == null) {
            return SdwnTransaction.TransactionStatus.DONE;
        }

        controller.newClient(ap, newClient);
        boolean done = !reply.getFlags().contains(OFStatsReplyFlags.REPLY_MORE);
        return done ? SdwnTransaction.TransactionStatus.DONE : SdwnTransaction.TransactionStatus.CONTINUE;
    }
}
