package de.tuberlin.inet.sdwn.core.ctl.task;

import de.tuberlin.inet.sdwn.core.api.SdwnTransactionAdapter;
import de.tuberlin.inet.sdwn.core.api.SdwnTransactionStatus;
import de.tuberlin.inet.sdwn.core.api.entity.SdwnAccessPoint;
import de.tuberlin.inet.sdwn.core.api.entity.SdwnClient;
import org.onlab.packet.MacAddress;
import org.onosproject.openflow.controller.Dpid;
import org.projectfloodlight.openflow.protocol.OFMessage;
import org.projectfloodlight.openflow.protocol.OFSdwnAddClient;

import static de.tuberlin.inet.sdwn.core.api.SdwnTransactionStatus.DONE;
import static de.tuberlin.inet.sdwn.core.api.SdwnTransactionStatus.SKIP;

public class AddClientTransaction extends SdwnTransactionAdapter {

    private SdwnClient client;
    private SdwnAccessPoint ap;
    private final long timeout;

    public AddClientTransaction(SdwnClient client, SdwnAccessPoint ap, long timeout) {
        this.client = client;
        this.ap = ap;
        this.timeout = timeout;
    }

    @Override
    public long timeout() {
        return timeout;
    }


    @Override
    public SdwnTransactionStatus update(Dpid dpid, OFMessage msg) {
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