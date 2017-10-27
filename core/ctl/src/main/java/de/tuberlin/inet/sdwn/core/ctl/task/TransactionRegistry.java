package de.tuberlin.inet.sdwn.core.ctl.task;

import de.tuberlin.inet.sdwn.core.api.SdwnTransactionTask;
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

import static java.lang.System.currentTimeMillis;
import static org.slf4j.LoggerFactory.getLogger;

public class TransactionRegistry {

    private final Map<Long, TransactionContext> transactions = new HashMap<>();
    private final Set<SdwnTransactionTask> allMsgHandlers = new HashSet<>();

    public boolean ongoing(long xid) {
        return transactions.containsKey(xid);
    }

    public void registerTransaction(SdwnTransactionTask t, long timeout) {
        if (t.xid() == SdwnTransactionTask.NO_XID) {
            return;
        }

        synchronized (transactions) {

            if (t.xid() == SdwnTransactionTask.ANY_XID) {
                allMsgHandlers.add(t);
                return;
            }

            transactions.put(t.xid(), new TransactionContext(t, timeout));
        }
    }

    public void removeTransaction(SdwnTransactionTask t) {
        synchronized (transactions) {
            transactions.remove(t.xid());
        }
    }

    public boolean runEventHandlers(Dpid dpid, OFMessage ev) {
        synchronized (transactions) {

            allMsgHandlers.forEach(t -> t.update(dpid, ev));

            TransactionContext ctx = transactions.get(ev.getXid());
            if (ctx == null)
                return false;

            return !ctx.doUpdate(dpid, ev).equals(SdwnTransactionTask.TransactionStatus.SKIP);
        }
    }

    private class TransactionContext {

        private SdwnTransactionTask task;
        private Timeout timeout;
        private long timeoutVal;


        TransactionContext(SdwnTransactionTask t, long timeout) {
            task = t;
            timeoutVal = timeout;
            this.timeout = Timer.getTimer().newTimeout(new TransactionTimeout(t), timeout, TimeUnit.MILLISECONDS);
        }

        SdwnTransactionTask.TransactionStatus doUpdate(Dpid dpid, OFMessage msg) {
            SdwnTransactionTask.TransactionStatus status = task.update(dpid, msg);

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
            return (obj instanceof TransactionContext) &&
                    ((TransactionContext) obj).task == this;
        }
    }


    private class TransactionTimeout implements TimerTask {

        private SdwnTransactionTask task;

        TransactionTimeout(SdwnTransactionTask t) {
            task = t;
        }

        @Override
        public void run(Timeout timeout) throws Exception {
            synchronized (transactions) {
                transactions.remove(task.xid());
                task.timeout();
            }
        }
    }
}
