package de.tuberlin.inet.sdwn.core.ctl.task;

import de.tuberlin.inet.sdwn.core.api.DefaultSdwnTransaction;
import de.tuberlin.inet.sdwn.core.api.entity.SdwnAccessPoint;
import de.tuberlin.inet.sdwn.core.api.entity.SdwnClient;
import org.onlab.packet.MacAddress;
import org.onosproject.openflow.controller.Dpid;
import org.projectfloodlight.openflow.protocol.OFMessage;
import org.projectfloodlight.openflow.protocol.OFSdwnAddClient;

import static de.tuberlin.inet.sdwn.core.api.SdwnTransactionTask.TransactionStatus.DONE;
import static de.tuberlin.inet.sdwn.core.api.SdwnTransactionTask.TransactionStatus.SKIP;

public class AddClientTask extends DefaultSdwnTransaction {

    private SdwnClient client;
    private SdwnAccessPoint ap;

    public AddClientTask(long xid, SdwnClient client, SdwnAccessPoint ap) {
        super(xid);
        this.client = client;
        this.ap = ap;
    }

    @Override
    public TransactionStatus update(Dpid dpid, OFMessage msg) {
        if (!(msg instanceof OFSdwnAddClient)) {
            return SKIP;
        }

        OFSdwnAddClient reply = (OFSdwnAddClient) msg;
        if (MacAddress.valueOf(reply.getClient().getBytes()).equals(client.macAddress()) &&
                reply.getAp().getPortNumber() ==  ap.portNumber()) {
            client.assoc(ap);
            ap.addClient(client);
        }

        return DONE;
    }
}
