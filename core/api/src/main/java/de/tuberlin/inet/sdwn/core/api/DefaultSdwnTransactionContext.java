package de.tuberlin.inet.sdwn.core.api;

import io.netty.util.Timeout;
import io.netty.util.TimerTask;
import org.onlab.util.Timer;
import org.slf4j.Logger;

import java.util.concurrent.TimeUnit;

import static org.slf4j.LoggerFactory.getLogger;

public abstract class DefaultSdwnTransactionContext implements SdwnTransactionContext {

    protected long xid;
    protected SdwnTransactionManager transactionManager;
    protected Timeout timeout;
    protected SdwnTransactionContext followupTask;

    protected final Logger log = getLogger(getClass());

    public DefaultSdwnTransactionContext(long timeout) {
        xid = NO_XID;
        this.timeout = Timer.getTimer().newTimeout(new TransactionTimeout(this), timeout, TimeUnit.MILLISECONDS);
    }

    public DefaultSdwnTransactionContext(long xid, long timeout) {
        this(timeout);
        this.xid = xid;
    }

    public DefaultSdwnTransactionContext(long timeout, SdwnTransactionContext task) {
        this(timeout);
        followupTask = task;
    }

    public DefaultSdwnTransactionContext(long xid, long timeout, SdwnTransactionContext task) {
        this(xid, timeout);
        followupTask = task;
    }

    @Override
    public long xid() {
        return xid;
    }

    @Override
    public void setManager(SdwnTransactionManager m) {
        transactionManager = m;
    }

    @Override
    public void setXid(long xid) {
        this.xid = xid;
    }

    @Override
    public void timeout() {
    }

    @Override
    public boolean hasFollowupTask() {
        return this.followupTask != null;
    }

    @Override
    public SdwnTransactionContext setFollowupTask(SdwnTransactionContext t) {
        this.followupTask = t;
        return this;
    }

    @Override
    public SdwnTransactionContext followupTask() {
        return this.followupTask;
    }

    private class TransactionTimeout implements TimerTask {

        private SdwnTransactionContext task;

        TransactionTimeout(SdwnTransactionContext t) {
            task = t;
        }

        @Override
        public void run(Timeout timeout) throws Exception {
            transactionManager.cancelTransaction(task);
            task.timeout();
        }
    }
}
