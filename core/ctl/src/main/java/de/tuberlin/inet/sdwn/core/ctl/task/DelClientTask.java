package de.tuberlin.inet.sdwn.core.ctl.task;

import de.tuberlin.inet.sdwn.core.api.DefaultSdwnTransaction;
import de.tuberlin.inet.sdwn.core.api.entity.SdwnAccessPoint;
import de.tuberlin.inet.sdwn.core.api.entity.SdwnClient;
import org.onlab.packet.MacAddress;
import org.onosproject.openflow.controller.Dpid;
import org.projectfloodlight.openflow.protocol.OFMessage;
import org.projectfloodlight.openflow.protocol.OFSdwnDelClient;

import static de.tuberlin.inet.sdwn.core.api.SdwnTransactionTask.TransactionStatus.SKIP;

public class DelClientTask extends DefaultSdwnTransaction {

    private final SdwnClient client;
    private final SdwnAccessPoint ap;

    public DelClientTask(long xid, long timeout, SdwnClient client, SdwnAccessPoint ap) {
        super(xid, timeout);
        this.client = client;
        this.ap = ap;
    }

    @Override
    public TransactionStatus update(Dpid dpid, OFMessage msg) {

        if (!(msg instanceof OFSdwnDelClient)) {
            return SKIP;
        }

        OFSdwnDelClient delClientMsg = (OFSdwnDelClient) msg;

        if (delClientMsg.getAp().getPortNumber() == ap.portNumber() &&
                client.macAddress().equals(MacAddress.valueOf(delClientMsg.getClient().getBytes()))) {

            transactionManager.controller().removeClient(client);
        }

        return null;
    }
}
