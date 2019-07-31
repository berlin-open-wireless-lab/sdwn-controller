package de.tuberlin.inet.sdwn.core.ctl;

import com.google.common.base.MoreObjects;
import de.tuberlin.inet.sdwn.core.api.SdwnTransaction;
import de.tuberlin.inet.sdwn.core.api.SdwnTransactionChain;
import io.netty.util.HashedWheelTimer;
import io.netty.util.Timeout;
import io.netty.util.Timer;
import io.netty.util.TimerTask;
import org.onosproject.openflow.controller.Dpid;
import org.projectfloodlight.openflow.protocol.OFMessage;
import org.projectfloodlight.openflow.protocol.XidGenerator;
import org.slf4j.Logger;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;

import static com.google.common.base.Preconditions.checkNotNull;
import static org.slf4j.LoggerFactory.getLogger;

public class TransactionManager {

    private final Logger log = getLogger(getClass());

    private final XidGenerator xidGen;
    private final Map<Long, TransactionContext> transactions = new ConcurrentHashMap<>();

    public TransactionManager(XidGenerator xidGen) {
        this.xidGen = xidGen;
    }

    long startTransaction(SdwnTransaction t) {
        checkNotNull(t);

        TransactionContext ctx = new TransactionContext(t);
        transactions.put(ctx.xid, ctx);
        ctx.transaction.start(ctx.xid);
        ctx.timer.newTimeout(new TransactionTimeoutTask(ctx), ctx.transaction.timeout(), TimeUnit.MILLISECONDS);

        log.info("Started transaction {}", ctx);
        return ctx.xid;
    }

    void startTransaction(SdwnTransaction t, long xid) {
        checkNotNull(t);

        TransactionContext ctx = new TransactionContext(t, xid);
        transactions.replace(xid, ctx);
        ctx.transaction.start(xid);
        ctx.timer.newTimeout(new TransactionTimeoutTask(ctx), ctx.transaction.timeout(), TimeUnit.MILLISECONDS);

        log.info("Started transaction {}", ctx);
    }

    long startTransactionChain(SdwnTransactionChain chain) {
        checkNotNull(chain);

        TransactionContext ctx = new TransactionContext(chain.next());
        transactions.put(ctx.xid, ctx);
        ctx.transaction.start(ctx.xid);
        ctx.timer.newTimeout(new TransactionTimeoutTask(ctx), ctx.transaction.timeout(), TimeUnit.MILLISECONDS);

        log.info("Started transaction {} as part of a transaction chain", ctx);

        return ctx.xid;
    }

    void msgReceived(Dpid dpid, OFMessage msg) {
        TransactionContext ctx;
        if ((ctx = transactions.get(msg.getXid())) != null) {
            switch (ctx.transaction.update(dpid, msg)) {
                case SKIP: /* fall through */
                case CONTINUE:
                    ctx.timer.newTimeout(new TransactionTimeoutTask(ctx), ctx.transaction.timeout(), TimeUnit.MILLISECONDS);
                    break;
                case NEXT:
                    ctx.timer.stop();
                    if (ctx.chain == null || ctx.chain.next() == null) {
                        log.warn("Transaction returned NEXT although not part of a chain");
                        transactions.remove(ctx.xid);
                        break;
                    }

                    TransactionContext nextCtx = new TransactionContext(ctx.chain.next(), ctx.xid, ctx.chain);
                    transactions.replace(nextCtx.xid, nextCtx);
                    nextCtx.transaction.start(nextCtx.xid);
                    nextCtx.timer.newTimeout(new TransactionTimeoutTask(nextCtx), nextCtx.transaction.timeout(), TimeUnit.MILLISECONDS);

                    log.info("Started next transaction in transaction chain: {}", nextCtx);

                    break;
                case DONE:
                    ctx.timer.stop();
                    transactions.remove(ctx.xid);
                    ctx.transaction.done();

                    log.info("Transaction done {}", ctx);
                    break;
            }
        }
    }

    boolean isOngoing(long xid) {
        return transactions.containsKey(xid);
    }

    void abortTransaction(long xid) {
        if (!isOngoing(xid)) {
            return;
        }

        TransactionContext ctx = transactions.remove(xid);
        ctx.timer.stop();

        log.info("Aborted transaction {}", ctx);
        ctx.transaction.aborted();
    }

    /**
     * Wrapper for an {@code SdwnTransaction} instance with transaction ID and timeoutVal.
     */
    private class TransactionContext {
        final SdwnTransaction transaction;
        Timer timer = new HashedWheelTimer();
        final long xid;
        final SdwnTransactionChain chain;

        TransactionContext(SdwnTransaction t, long xid, SdwnTransactionChain chain) {
            transaction = t;
            this.xid = xid;
            this.chain = chain;
        }

        TransactionContext(SdwnTransaction t, long xid) {
            this(t, xid, null);
        }

        TransactionContext(SdwnTransaction t) {
            this(t, xidGen.nextXid(), null);
        }

        @Override
        public String toString() {
            return MoreObjects.toStringHelper(transaction.getClass()).add("XID", xid).toString();
        }
    }

    private class TransactionTimeoutTask implements TimerTask {

        private final TransactionContext context;

        TransactionTimeoutTask(TransactionContext ctx) {
            context = ctx;
        }

        @Override
        public void run(Timeout timeout) throws Exception {
            transactions.remove(context.xid);
            context.transaction.timedOut();
        }
    }
}
