package de.tuberlin.inet.sdwn.mobilitymanager.impl;

import de.tuberlin.inet.sdwn.core.api.entity.SdwnAccessPoint;
import de.tuberlin.inet.sdwn.core.api.DefaultSdwnTransactionContext;
import de.tuberlin.inet.sdwn.core.api.entity.SdwnClient;
import org.onosproject.openflow.controller.Dpid;
import org.projectfloodlight.openflow.protocol.OFMessage;
import org.projectfloodlight.openflow.protocol.OFSdwnAddClient;
import org.projectfloodlight.openflow.protocol.OFSdwnDelClient;
import org.projectfloodlight.openflow.types.MacAddress;

import static de.tuberlin.inet.sdwn.core.api.SdwnTransactionContext.TransactionStatus.CONTINUE;
import static de.tuberlin.inet.sdwn.core.api.SdwnTransactionContext.TransactionStatus.DONE;
import static de.tuberlin.inet.sdwn.core.api.SdwnTransactionContext.TransactionStatus.SKIP;

/*
 * FIXME
 * When multiple handover transactions are happening simultaneously, the one with the higher priority will deny the necessary
 * assoc/auth requests. We need to track all ongoing transactions and globally grant and deny these requests.
 */

public class HandoverTransactionContext extends DefaultSdwnTransactionContext {

    private enum State {
        STATE_DEL_CLIENT,
        STATE_ADD_CLIENT
    }

    private State state;

    private final SdwnAccessPoint dst;
    private final SdwnClient client;

    public HandoverTransactionContext(SdwnAccessPoint ap, SdwnClient client) {
        this.client = client;
        this.dst = ap;
    }

    public SdwnAccessPoint dst() {
        return dst;
    }

    public SdwnClient client() {
        return client;
    }

    @Override
    public void start() {
        transactionManager.controller().removeClientFromAp(client.macAddress(), 10000);
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
                    transactionManager.controller().addClientToAp(dst, client);
                    state = State.STATE_ADD_CLIENT;
                }
                break;
            case STATE_ADD_CLIENT:
                OFSdwnAddClient addClientMsg = (OFSdwnAddClient) msg;
                if (addClientMsg.getClient().equals(MacAddress.of(client.macAddress().toBytes())) &&
                        addClientMsg.getAp().getPortNumber() == dst.portNumber()) {
                    return DONE;
                }
                break;
        }

        return CONTINUE;
    }
}
