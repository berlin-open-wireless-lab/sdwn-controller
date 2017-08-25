package de.tuberlin.inet.sdwn.core.api;

import io.netty.util.Timeout;
import io.netty.util.TimerTask;
import org.onlab.util.Timer;
import org.slf4j.Logger;

import java.util.concurrent.TimeUnit;

import static org.slf4j.LoggerFactory.getLogger;

public abstract class DefaultSdwnTransaction implements SdwnTransactionTask {

    protected long xid;
    protected SdwnTransactionManager transactionManager;
    protected Timeout timeout;
    protected SdwnTransactionTask followupTask;

    protected final Logger log = getLogger(getClass());

    public DefaultSdwnTransaction(long timeout) {
        xid = NO_XID;
        this.timeout = Timer.getTimer().newTimeout(new TransactionTimeout(this), timeout, TimeUnit.MILLISECONDS);
    }

    public DefaultSdwnTransaction(long xid, long timeout) {
        this(timeout);
        this.xid = xid;
    }

    public DefaultSdwnTransaction(long timeout, SdwnTransactionTask task) {
        this(timeout);
        followupTask = task;
    }

    public DefaultSdwnTransaction(long xid, long timeout, SdwnTransactionTask task) {
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
    public SdwnTransactionTask setFollowupTask(SdwnTransactionTask t) {
        this.followupTask = t;
        return this;
    }

    @Override
    public SdwnTransactionTask followupTask() {
        return this.followupTask;
    }

    private class TransactionTimeout implements TimerTask {

        private SdwnTransactionTask task;

        TransactionTimeout(SdwnTransactionTask t) {
            task = t;
        }

        @Override
        public void run(Timeout timeout) throws Exception {
            transactionManager.cancelTransaction(task);
            task.timeout();
        }
    }
}
