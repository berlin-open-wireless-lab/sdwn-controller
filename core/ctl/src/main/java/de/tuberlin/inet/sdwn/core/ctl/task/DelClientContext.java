package de.tuberlin.inet.sdwn.core.ctl.task;

import de.tuberlin.inet.sdwn.core.api.DefaultSdwnTransactionContext;
import de.tuberlin.inet.sdwn.core.api.entity.SdwnAccessPoint;
import de.tuberlin.inet.sdwn.core.api.entity.SdwnClient;
import org.onlab.packet.MacAddress;
import org.onosproject.openflow.controller.Dpid;
import org.projectfloodlight.openflow.protocol.OFMessage;
import org.projectfloodlight.openflow.protocol.OFSdwnDelClient;

import static de.tuberlin.inet.sdwn.core.api.SdwnTransactionContext.TransactionStatus.SKIP;

public class DelClientContext extends DefaultSdwnTransactionContext {

    private final SdwnClient client;
    private final SdwnAccessPoint ap;


    public DelClientContext(long xid, SdwnClient client, SdwnAccessPoint ap) {
        super(xid);
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
