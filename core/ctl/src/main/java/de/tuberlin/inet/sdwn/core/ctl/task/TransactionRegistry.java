package de.tuberlin.inet.sdwn.core.ctl.task;

import de.tuberlin.inet.sdwn.core.api.SdwnTransactionContext;
import io.netty.util.Timeout;
import io.netty.util.TimerTask;
import org.onlab.util.Timer;
import org.onosproject.openflow.controller.Dpid;
import org.projectfloodlight.openflow.protocol.OFMessage;
import org.slf4j.Logger;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.TimeUnit;

import static org.slf4j.LoggerFactory.getLogger;

public class TransactionRegistry {

    private final Logger log = getLogger(getClass());

    private final Map<Long, InternalTransactionContext> transactions = new HashMap<>();
    private final Set<SdwnTransactionContext> allMsgHandlers = new HashSet<>();

    public boolean ongoing(long xid) {
        return transactions.containsKey(xid);
    }


    public void registerTransaction(SdwnTransactionContext t, long timeout) {
        if (t.xid() == SdwnTransactionContext.NO_XID) {
            log.error("Cannot register transaction context {}: no XID given", t);
            return;
        }

        synchronized (transactions) {
            if (t.xid() == SdwnTransactionContext.ANY_XID) {
                allMsgHandlers.add(t);
                return;
            }

            transactions.put(t.xid(), new InternalTransactionContext(t, timeout));
        }
    }

    public void removeTransaction(SdwnTransactionContext t) {
        synchronized (transactions) {
            transactions.remove(t.xid()).timeout.cancel();
        }
    }

    public boolean runEventHandlers(Dpid dpid, OFMessage ev) {
        synchronized (transactions) {

            allMsgHandlers.forEach(t -> t.update(dpid, ev));

            InternalTransactionContext ctx = transactions.get(ev.getXid());
            if (ctx == null)
                return false;

            return !ctx.doUpdate(dpid, ev).equals(SdwnTransactionContext.TransactionStatus.SKIP);
        }
    }

    /**
     * Wraps an instance of {@code SdwnTransactionContext} and adds a timeout. All Callbacks to the transaction
     * are invoked through an instance of this class.
     */
    private class InternalTransactionContext {

        private SdwnTransactionContext task;
        private Timeout timeout;
        private long timeoutVal;


        InternalTransactionContext(SdwnTransactionContext t, long timeout) {
            task = t;
            timeoutVal = timeout;
            this.timeout = Timer.getTimer().newTimeout(new TransactionTimeout(t), timeout, TimeUnit.MILLISECONDS);
        }

        SdwnTransactionContext.TransactionStatus doUpdate(Dpid dpid, OFMessage msg) {
            SdwnTransactionContext.TransactionStatus status = task.update(dpid, msg);

            switch (status) {
                case DONE:
                    timeout.cancel();
                    removeTransaction(task);
                    break;
                case CONTINUE:
                    timeout.timer().newTimeout(new TransactionTimeout(task), timeoutVal, TimeUnit.MILLISECONDS);
                    break;
                case NEXT:
                    removeTransaction(task);
                    if (task.hasFollowupTask()) {
                        registerTransaction(task.followupTask(), timeoutVal);
                    }
                    break;

            }
            return status;
        }

        @Override
        public boolean equals(Object obj) {
            return (obj instanceof InternalTransactionContext) &&
                    ((InternalTransactionContext) obj).task == this;
        }
    }


    private class TransactionTimeout implements TimerTask {

        private SdwnTransactionContext task;

        TransactionTimeout(SdwnTransactionContext t) {
            task = t;
        }

        @Override
        public void run(Timeout timeout) throws Exception {
            log.info("Task {} timed out...", task);
            synchronized (transactions) {
                transactions.remove(task.xid());
                task.timeout();
            }
        }
    }
}
