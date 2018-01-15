package de.tuberlin.inet.sdwn.mobilitymanager.impl;

import de.tuberlin.inet.sdwn.core.api.SdwnCoreService;
import de.tuberlin.inet.sdwn.core.api.SdwnTransactionStatus;
import de.tuberlin.inet.sdwn.core.api.entity.SdwnAccessPoint;
import de.tuberlin.inet.sdwn.core.api.SdwnTransactionAdapter;
import de.tuberlin.inet.sdwn.core.api.entity.SdwnClient;
import de.tuberlin.inet.sdwn.mobilitymanager.SdwnMobilityManager;
import org.onosproject.openflow.controller.Dpid;
import org.onosproject.openflow.controller.OpenFlowWirelessSwitch;
import org.projectfloodlight.openflow.protocol.OFMessage;
import org.projectfloodlight.openflow.protocol.OFSdwnAddClient;
import org.projectfloodlight.openflow.types.MacAddress;
import org.projectfloodlight.openflow.types.OFPort;
import org.slf4j.Logger;

import java.util.ArrayList;
import java.util.List;

import static com.google.common.base.Preconditions.checkNotNull;
import static de.tuberlin.inet.sdwn.core.api.SdwnTransactionStatus.DONE;
import static de.tuberlin.inet.sdwn.core.api.SdwnTransactionStatus.SKIP;
import static org.slf4j.LoggerFactory.getLogger;


public class HandoverTransactionAddClient extends SdwnTransactionAdapter {

    private final SdwnCoreService controller;
    private final SdwnMobilityManager mgr;
    private final SdwnAccessPoint dst;
    private final SdwnClient client;
    private final long timeout;
    private List<SdwnAccessPoint> blacklistedAt = new ArrayList<>();

    private final Logger log = getLogger(getClass());

    public HandoverTransactionAddClient(SdwnAccessPoint ap, SdwnClient client, SdwnMobilityManager mgr, SdwnCoreService controller, long timeout) {
        this.controller = controller;
        this.client = client;
        this.dst = ap;
        this.mgr = mgr;
        this.timeout = timeout;
    }

    public SdwnAccessPoint dst() {
        return dst;
    }

    public SdwnClient client() {
        return client;
    }

    @Override
    public long timeout() {
        return timeout;
    }

    @Override
    public void start(long xid) {
        OpenFlowWirelessSwitch sw = controller.getSwitch(client.ap().nic().switchID());
        checkNotNull(sw);

        // TODO:
        // blacklist the client at
        // a) all APs if the no hearing map service is available
        // b) all APs that have recently overheard the client if the hearingmap is available
        controller.aps().forEach(ap -> {
            controller.blacklistClientAtAp(ap, client.macAddress(), 10000);
            blacklistedAt.add(ap);
        });

        // start the handover by dis-associating the client at its current AP
        sw.sendMsg(sw.factory().buildSdwnDelClient()
                .setXid(xid)
                .setBanTime(10000)
                .setReason(1)
                .setDeauth((short) 1)
                .setClient(org.projectfloodlight.openflow.types.MacAddress.of(client.macAddress().toBytes()))
                .setAp(OFPort.of(client.ap().portNumber()))
                .build());
    }

    @Override
    public SdwnTransactionStatus update(Dpid dpid, OFMessage msg) {
        if (!(msg instanceof OFSdwnAddClient) || !((OFSdwnAddClient) msg).getClient().equals(MacAddress.of(client.macAddress().toBytes()))) {
            return SKIP;
        }

        OFSdwnAddClient addClientMsg = (OFSdwnAddClient) msg;

        if (addClientMsg.getAp().getPortNumber() == dst.portNumber()
                && dpid.equals(dst.nic().switchID())) {
            log.info("Handover finished: {} is now associated with [{}]:{}", client.macAddress(), dst.nic().switchID(), dst.name());
            client.assoc(dst);
        } else {
            log.error("Handover failed: {} assciated with undesired AP on {}", client.macAddress(), dpid);
        }

        return DONE;
    }

    @Override
    public void aborted() {
        log.error("Handover failed: {} -> [{]}:{}: aborted", client.macAddress(), dst.nic().switchID(), dst.name());
        mgr.abortHandover(client);
        blacklistedAt.forEach(ap -> controller.clearClientBlacklistingAtAp(ap, client.macAddress()));
    }

    @Override
    public void timedOut() {
        log.error("Handover failed: {} -> [{}]:{}: time-out", client.macAddress(), dst.nic().switchID(), dst.name());
        mgr.abortHandover(client);
    }
}
