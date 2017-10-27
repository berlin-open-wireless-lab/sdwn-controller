package de.tuberlin.inet.sdwn.core.api;

import org.slf4j.Logger;

import static org.slf4j.LoggerFactory.getLogger;

public abstract class DefaultSdwnTransaction implements SdwnTransactionTask {

    protected long xid;
    protected SdwnTransactionManager transactionManager;
    protected SdwnTransactionTask followupTask;

    protected final Logger log = getLogger(getClass());

    public DefaultSdwnTransaction() {
        xid = NO_XID;
    }

    public DefaultSdwnTransaction(long xid) {
        this.xid = xid;
    }

    public DefaultSdwnTransaction(SdwnTransactionTask task) {
        followupTask = task;
    }

    public DefaultSdwnTransaction(long xid, SdwnTransactionTask task) {
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
    public void done() {
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
}
