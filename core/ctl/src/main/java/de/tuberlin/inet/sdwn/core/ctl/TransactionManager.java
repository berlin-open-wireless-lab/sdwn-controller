package de.tuberlin.inet.sdwn.core.ctl;

import de.tuberlin.inet.sdwn.core.api.SdwnCoreService;
import de.tuberlin.inet.sdwn.core.api.SdwnTransactionContext;
import de.tuberlin.inet.sdwn.core.api.SdwnTransactionManager;
import de.tuberlin.inet.sdwn.core.ctl.task.TransactionRegistry;
import org.onosproject.openflow.controller.Dpid;
import org.projectfloodlight.openflow.protocol.OFMessage;

public class TransactionManager implements SdwnTransactionManager {

    private final SdwnCoreService controller;
    private TransactionRegistry transactions;

    public TransactionManager(SdwnController controller) {
        this.transactions = new TransactionRegistry();
        this.controller = controller;
    }

    public boolean ongoingTransaction(long xid) {
        return transactions.ongoing(xid);
    }

    @Override
    public SdwnCoreService controller() {
        return controller;
    }

    @Override
    public void startTransaction(SdwnTransactionContext t, long timeout) {
        t.setManager(this);
        transactions.registerTransaction(t, timeout);
        t.start();
    }

    @Override
    public void cancelTransaction(SdwnTransactionContext t) {
        transactions.removeTransaction(t);
    }

    @Override
    public boolean msgReceived(Dpid dpid, OFMessage msg) {
        return transactions.runEventHandlers(dpid, msg);
    }
}
