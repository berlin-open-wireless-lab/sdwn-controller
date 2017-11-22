package de.tuberlin.inet.sdwn.mobilitymanager.impl;

import de.tuberlin.inet.sdwn.core.api.SdwnCoreService;
import de.tuberlin.inet.sdwn.core.api.entity.SdwnAccessPoint;
import de.tuberlin.inet.sdwn.core.api.DefaultSdwnTransactionContext;
import de.tuberlin.inet.sdwn.core.api.entity.SdwnClient;
import de.tuberlin.inet.sdwn.mobilitymanager.SdwnMobilityManager;
import org.onosproject.openflow.controller.Dpid;
import org.onosproject.openflow.controller.OpenFlowWirelessSwitch;
import org.projectfloodlight.openflow.protocol.OFMessage;
import org.projectfloodlight.openflow.protocol.OFSdwnAddClient;
import org.projectfloodlight.openflow.protocol.OFSdwnDelClient;
import org.projectfloodlight.openflow.types.MacAddress;
import org.projectfloodlight.openflow.types.OFPort;

import java.util.ArrayList;
import java.util.List;

import static com.google.common.base.Preconditions.checkNotNull;
import static de.tuberlin.inet.sdwn.core.api.SdwnTransactionContext.TransactionStatus.CONTINUE;
import static de.tuberlin.inet.sdwn.core.api.SdwnTransactionContext.TransactionStatus.DONE;
import static de.tuberlin.inet.sdwn.core.api.SdwnTransactionContext.TransactionStatus.SKIP;


public class HandoverTransactionContext extends DefaultSdwnTransactionContext {

    private enum State {
        STATE_DEL_CLIENT,
        STATE_ADD_CLIENT
    }

    private State state;

    private final SdwnMobilityManager mgr;
    private final SdwnAccessPoint dst;
    private final SdwnClient client;
    private List<SdwnAccessPoint> blacklistedAt = new ArrayList<>();

    public HandoverTransactionContext(SdwnAccessPoint ap, SdwnClient client, SdwnMobilityManager mgr) {
        this.client = client;
        this.dst = ap;
        this.mgr = mgr;
    }

    public SdwnAccessPoint dst() {
        return dst;
    }

    public SdwnClient client() {
        return client;
    }

    @Override
    public void start() {
        SdwnCoreService controller = manager.controller();
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
    public TransactionStatus update(Dpid dpid, OFMessage msg) {
        if (!(state.equals(State.STATE_DEL_CLIENT) && msg instanceof OFSdwnDelClient) ||
                !(state.equals(State.STATE_ADD_CLIENT) && msg instanceof OFSdwnAddClient)) {
            return SKIP;
        }

        switch (state) {
            case STATE_DEL_CLIENT:
                if (((OFSdwnDelClient) msg).getClient().equals(MacAddress.of(client.macAddress().toBytes()))) {
                    state = State.STATE_ADD_CLIENT;
                    log.info("Handover update: {} disassociated from [{}]:{}", client.macAddress(), client.ap().nic().switchID(), client.ap().name());
                    client.disassoc();
                }
                break;
            case STATE_ADD_CLIENT:
                OFSdwnAddClient addClientMsg = (OFSdwnAddClient) msg;
                if (addClientMsg.getClient().equals(MacAddress.of(client.macAddress().toBytes())) &&
                        addClientMsg.getAp().getPortNumber() == dst.portNumber()) {
                    log.info("Handover finished: {} is now associated with [{}]:{}", client.macAddress(), dst.nic().switchID(), dst.name());
                    client.assoc(dst);
                    return DONE;
                }
                break;
        }

        return CONTINUE;
    }

    @Override
    public void cancel() {
        log.error("Handover failed: {} -> [{]}:{}: cancelled", client.macAddress(), dst.nic().switchID(), dst.name());
        mgr.abortHandover(client);
        blacklistedAt.forEach(ap -> manager.controller().clearClientBlacklistingAtAp(ap, client.macAddress()));
    }

    @Override
    public void timeout() {
        log.error("Handover failed: {} -> [{}]:{}: timeout", client.macAddress(), dst.nic().switchID(), dst.name());
        mgr.abortHandover(client);
    }
}
