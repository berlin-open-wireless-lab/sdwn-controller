package de.tuberlin.inet.sdwn.core.ctl.task;

import de.tuberlin.inet.sdwn.core.api.SdwnTransactionContext;
import io.netty.util.Timeout;
import io.netty.util.TimerTask;
import org.onlab.util.Timer;
import org.onosproject.openflow.controller.Dpid;
import org.projectfloodlight.openflow.protocol.OFMessage;

import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

import static java.lang.System.currentTimeMillis;

public class TransactionRegistry {

    private final long TIMEOUT;
    private Timeout timeout;

    private final Map<Long, TransactionHandler> transactions = new HashMap<>();
    private final Set<SdwnTransactionContext> allMsgHandlers = new HashSet<>();

    public TransactionRegistry(long timeout) {
        TIMEOUT = timeout;
    }

    public boolean ongoing(long xid) {
        return transactions.containsKey(xid);
    }

    public void registerTransaction(SdwnTransactionContext t) {
        if (t.xid() == SdwnTransactionContext.NO_XID) {
            return;
        }

        if (t.xid() == SdwnTransactionContext.ANY_XID) {
            allMsgHandlers.add(t);
            return;
        }

        transactions.put(t.xid(), new TransactionHandler(t));

        if (transactions.size() == 1) {
            timeout = Timer.getTimer().newTimeout(new PeriodicCleanup(), TIMEOUT, TimeUnit.SECONDS);
        }
    }

    public void unregisterTransaction(SdwnTransactionContext t) {
        transactions.remove(t.xid());
        if (transactions.isEmpty()) {
            timeout.cancel();
        }
    }

    public synchronized boolean runEventHandlers(Dpid dpid, OFMessage ev) {
        allMsgHandlers.forEach(t -> t.update(dpid, ev));

        TransactionHandler handler = transactions.get(ev.getXid());
        if (handler == null)
            return false;

        switch (handler.task.update(dpid, ev)) {
            case DONE:
                transactions.remove(ev.getXid());
                if (transactions.isEmpty()) {
                    timeout.cancel();
                }
                break;
            case CONTINUE:
                handler.timestamp = currentTimeMillis();
                break;
            case NEXT:
                transactions.remove(ev.getXid());
                if (handler.task.hasFollowupTask()) {
                    registerTransaction(handler.task.followupTask());
                }
                break;

        }
        return true;
    }

    private class TransactionHandler {
        long timestamp;
        SdwnTransactionContext task;

        TransactionHandler(SdwnTransactionContext t) {
            timestamp = currentTimeMillis();
            task = t;
        }

        @Override
        public boolean equals(Object obj) {
            return (obj instanceof TransactionHandler) &&
                    ((TransactionHandler) obj).task == this;
        }
    }

    private class PeriodicCleanup implements TimerTask {

        @Override
        public void run(Timeout timeout) throws Exception {
            synchronized (transactions) {
                long now = currentTimeMillis();
                List<Long> toRemove = transactions.keySet().stream()
                        .filter(key -> now - transactions.get(key).timestamp > TIMEOUT)
                        .collect(Collectors.toList());

                toRemove.forEach(transactions::remove);

                if (!transactions.isEmpty()) {
                    Timer.getTimer().newTimeout(new PeriodicCleanup(), TIMEOUT, TimeUnit.SECONDS);
                }
            }
        }
    }
}
