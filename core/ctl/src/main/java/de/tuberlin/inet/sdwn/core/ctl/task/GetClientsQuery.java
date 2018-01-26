package de.tuberlin.inet.sdwn.core.ctl.task;

import de.tuberlin.inet.sdwn.core.api.SdwnCoreService;
import de.tuberlin.inet.sdwn.core.api.SdwnTransactionStatus;
import de.tuberlin.inet.sdwn.core.api.entity.SdwnAccessPoint;
import de.tuberlin.inet.sdwn.core.api.entity.SdwnClient;
import de.tuberlin.inet.sdwn.core.ctl.entity.Client;
import de.tuberlin.inet.sdwn.core.api.SdwnTransactionAdapter;
import org.onosproject.openflow.controller.Dpid;
import org.projectfloodlight.openflow.protocol.OFMessage;
import org.projectfloodlight.openflow.protocol.OFSdwnGetClientsReply;
import org.projectfloodlight.openflow.protocol.OFSdwnGetClientsRequest;
import org.projectfloodlight.openflow.protocol.OFStatsReplyFlags;
import org.projectfloodlight.openflow.types.OFPort;
import org.slf4j.Logger;

import static de.tuberlin.inet.sdwn.core.api.SdwnTransactionStatus.CONTINUE;
import static de.tuberlin.inet.sdwn.core.api.SdwnTransactionStatus.DONE;
import static de.tuberlin.inet.sdwn.core.api.SdwnTransactionStatus.SKIP;
import static org.slf4j.LoggerFactory.getLogger;

public class GetClientsQuery extends SdwnTransactionAdapter {

    private final Dpid dpid;
    private final SdwnAccessPoint ap;
    private SdwnCoreService controller;
    private final long timeout;

    private final Logger log = getLogger(getClass());

    public GetClientsQuery(SdwnAccessPoint ap, Dpid dpid, SdwnCoreService controller, long timeout) {
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
    public void start(long xid) {
        OFSdwnGetClientsRequest getClientsMsg = controller.getSwitch(dpid).factory().buildSdwnGetClientsRequest()
                .setXid(xid)
                .setIfNo(OFPort.of(ap.portNumber()))
                .build();

        controller.sendMessage(dpid, getClientsMsg);
    }

    @Override
    public SdwnTransactionStatus update(Dpid dpid, OFMessage msg) {

        if (!(msg instanceof OFSdwnGetClientsReply)) {
            return SKIP;
        }

        OFSdwnGetClientsReply reply = (OFSdwnGetClientsReply) msg;
        SdwnClient newClient = Client.fromGetClientsReply(ap, reply);
        if (newClient == null) {
            return DONE;
        }

        controller.newClient(ap, newClient);
        boolean done = !reply.getFlags().contains(OFStatsReplyFlags.REPLY_MORE);
        return done ? DONE : CONTINUE;
    }

    @Override
    public void timedOut() {
        log.info("Get Clients query has timed out. This probably means that the queried AP does not have any associated clients.");
    }
}
