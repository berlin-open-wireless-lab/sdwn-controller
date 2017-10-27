package de.tuberlin.inet.sdwn.core.ctl;

import de.tuberlin.inet.sdwn.core.api.SdwnCoreService;
import de.tuberlin.inet.sdwn.core.api.SdwnTransactionManager;
import de.tuberlin.inet.sdwn.core.api.SdwnTransactionTask;
import de.tuberlin.inet.sdwn.core.ctl.task.TransactionRegistry;
import org.onosproject.openflow.controller.Dpid;
import org.projectfloodlight.openflow.protocol.OFMessage;

public class SdwnManager implements SdwnTransactionManager {

    private final SdwnCoreService controller;
    private TransactionRegistry transactions;

    public SdwnManager(SdwnController controller, long transactionStateTimeout) {
        this.transactions = new TransactionRegistry(transactionStateTimeout);
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
    public void startTransaction(SdwnTransactionTask t) {
        t.setManager(this);
        transactions.registerTransaction(t);
    }

    @Override
    public void cancelTransaction(SdwnTransactionTask t) {
        transactions.removeTransaction(t);
    }

    @Override
    public boolean msgReceived(Dpid dpid, OFMessage msg) {
        return transactions.runEventHandlers(dpid, msg);
    }
}
