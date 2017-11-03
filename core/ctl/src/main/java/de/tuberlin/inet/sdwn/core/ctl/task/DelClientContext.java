package de.tuberlin.inet.sdwn.core.ctl.task;

import de.tuberlin.inet.sdwn.core.api.DefaultSdwnTransactionContext;
import de.tuberlin.inet.sdwn.core.api.entity.SdwnAccessPoint;
import de.tuberlin.inet.sdwn.core.api.entity.SdwnClient;
import org.onlab.packet.MacAddress;
import org.onosproject.openflow.controller.Dpid;
import org.onosproject.openflow.controller.OpenFlowWirelessSwitch;
import org.projectfloodlight.openflow.protocol.OFMessage;
import org.projectfloodlight.openflow.protocol.OFSdwnDelClient;
import org.projectfloodlight.openflow.types.OFPort;

import static com.google.common.base.Preconditions.checkNotNull;
import static de.tuberlin.inet.sdwn.core.api.SdwnTransactionContext.TransactionStatus.SKIP;

public class DelClientContext extends DefaultSdwnTransactionContext {

    private final SdwnClient client;
    private final SdwnAccessPoint ap;
    private final long banTime;

    public DelClientContext(long xid, SdwnClient client, SdwnAccessPoint ap, long banTime) {
        super(xid);
        this.client = client;
        this.ap = ap;
        this.banTime = banTime;
    }

    @Override
    public void start() {
        OpenFlowWirelessSwitch sw = manager.controller().getSwitch(client.ap().nic().switchID());
        checkNotNull(sw);

        sw.sendMsg(sw.factory().buildSdwnDelClient()
                .setXid(xid)
                .setClient(org.projectfloodlight.openflow.types.MacAddress.of(client.macAddress().toBytes()))
                .setAp(OFPort.of(client.ap().portNumber()))
                .setReason(1)
                .setBanTime(banTime)
                .build());
    }

    @Override
    public TransactionStatus update(Dpid dpid, OFMessage msg) {

        if (!(msg instanceof OFSdwnDelClient)) {
            return SKIP;
        }

        OFSdwnDelClient delClientMsg = (OFSdwnDelClient) msg;

        if (delClientMsg.getAp().getPortNumber() == ap.portNumber() &&
                client.macAddress().equals(MacAddress.valueOf(delClientMsg.getClient().getBytes()))) {

            manager.controller().removeClient(client);
        }

        return null;
    }
}
