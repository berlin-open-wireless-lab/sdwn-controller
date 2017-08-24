package de.tuberlin.inet.sdwn.core.ctl.task;

import de.tuberlin.inet.sdwn.core.api.entity.SdwnAccessPoint;
import de.tuberlin.inet.sdwn.core.api.DefaultSdwnTransaction;
import de.tuberlin.inet.sdwn.core.api.entity.SdwnClient;
import org.onosproject.openflow.controller.Dpid;
import org.projectfloodlight.openflow.protocol.OFMessage;
import org.projectfloodlight.openflow.protocol.OFSdwnAddClient;
import org.projectfloodlight.openflow.protocol.OFSdwnDelClient;
import org.projectfloodlight.openflow.types.MacAddress;

import static de.tuberlin.inet.sdwn.core.api.SdwnTransactionTask.TransactionStatus.CONTINUE;
import static de.tuberlin.inet.sdwn.core.api.SdwnTransactionTask.TransactionStatus.DONE;
import static de.tuberlin.inet.sdwn.core.api.SdwnTransactionTask.TransactionStatus.SKIP;

public class HandoverTask extends DefaultSdwnTransaction {

    private enum State {
        STATE_DEL_STA,
        STATE_ADD_STA
    }

    private State state;

    private final SdwnAccessPoint toAP;
    private final SdwnClient client;

    public HandoverTask(long timeout, SdwnAccessPoint ap, SdwnClient client) {
        super(timeout);
        this.client = client;
        this.toAP = ap;
    }

    public HandoverTask(long xid, long timeout, SdwnAccessPoint ap, SdwnClient client) {
        super(xid, timeout);
        this.client = client;
        this.toAP = ap;
    }

    @Override
    public TransactionStatus update(Dpid dpid, OFMessage msg) {
        if (   !(state.equals(State.STATE_DEL_STA) && msg instanceof OFSdwnDelClient) ||
                !(state.equals(State.STATE_ADD_STA) && msg instanceof OFSdwnAddClient)) {
            return SKIP;
        }

        switch (state) {
            case STATE_DEL_STA:
                if (((OFSdwnDelClient) msg).getClient().equals(MacAddress.of(client.macAddress().toBytes()))) {
                    transactionManager.controller().addClientToAp(toAP, client);
                    state = State.STATE_ADD_STA;
                }
                break;
            case STATE_ADD_STA:
                OFSdwnAddClient addClientMsg = (OFSdwnAddClient) msg;
                if (addClientMsg.getClient().equals(MacAddress.of(client.macAddress().toBytes())) &&
                        addClientMsg.getAp().getPortNumber() == toAP.portNumber()) {
                    return DONE;
                }
                break;
        }

        return CONTINUE;
    }
}
