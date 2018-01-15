package de.tuberlin.inet.sdwn.mobilitymanager.impl;

import de.tuberlin.inet.sdwn.core.api.SdwnCoreService;
import de.tuberlin.inet.sdwn.core.api.SdwnTransactionAdapter;
import de.tuberlin.inet.sdwn.core.api.SdwnTransactionStatus;
import de.tuberlin.inet.sdwn.core.api.entity.SdwnAccessPoint;
import de.tuberlin.inet.sdwn.core.api.entity.SdwnClient;
import de.tuberlin.inet.sdwn.mobilitymanager.SdwnMobilityManager;
import org.onosproject.openflow.controller.Dpid;
import org.onosproject.openflow.controller.OpenFlowWirelessSwitch;
import org.projectfloodlight.openflow.protocol.OFMessage;
import org.projectfloodlight.openflow.protocol.OFSdwnDelClient;
import org.projectfloodlight.openflow.types.MacAddress;
import org.projectfloodlight.openflow.types.OFPort;
import org.slf4j.Logger;

import java.util.ArrayList;
import java.util.List;

import static com.google.common.base.Preconditions.checkNotNull;
import static de.tuberlin.inet.sdwn.core.api.SdwnTransactionStatus.*;
import static org.slf4j.LoggerFactory.getLogger;

public class HandoverTransactionDelClient extends SdwnTransactionAdapter {

    private final SdwnCoreService controller;
    private final SdwnMobilityManager mgr;
    private final SdwnClient client;
    private final long timeout;
    private List<SdwnAccessPoint> blacklistedAt = new ArrayList<>();

    private final Logger log = getLogger(getClass());

    public HandoverTransactionDelClient(SdwnClient client, SdwnMobilityManager mgr, SdwnCoreService controller, long timeout) {
        this.controller = controller;
        this.client = client;
        this.mgr = mgr;
        this.timeout = timeout;
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
        if (msg instanceof OFSdwnDelClient && ((OFSdwnDelClient) msg).getClient().equals(MacAddress.of(client.macAddress().toBytes()))) {
            log.info("Handover update: {} disassociated from [{}]:{}", client.macAddress(), client.ap().nic().switchID(), client.ap().name());
            client.disassoc();
            return NEXT;
        }

        return SKIP;
    }


    @Override
    public void aborted() {
        mgr.abortHandover(client);
        blacklistedAt.forEach(ap -> controller.clearClientBlacklistingAtAp(ap, client.macAddress()));
    }

    @Override
    public void timedOut() {
        mgr.abortHandover(client);
    }
}
