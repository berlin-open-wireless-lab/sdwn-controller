package de.tuberlin.inet.sdwn.core.api;

import org.slf4j.Logger;

import static org.slf4j.LoggerFactory.getLogger;

public abstract class DefaultSdwnTransactionContext implements SdwnTransactionContext {

    protected long xid;
    protected SdwnTransactionManager manager;

    protected SdwnTransactionContext followupTask;

    protected final Logger log = getLogger(getClass());

    public DefaultSdwnTransactionContext() {
        xid = NO_XID;
    }

    public DefaultSdwnTransactionContext(long xid) {
        this.xid = xid;
    }

    public DefaultSdwnTransactionContext(SdwnTransactionContext task) {
        followupTask = task;
    }

    public DefaultSdwnTransactionContext(long xid, SdwnTransactionContext task) {
        followupTask = task;
    }

    @Override
    public long xid() {
        return xid;
    }

    @Override
    public void setManager(SdwnTransactionManager m) {
        manager = m;
    }

    @Override
    public void setXid(long xid) {
        this.xid = xid;
    }

    @Override
    public void timeout() {
    }

    @Override
    public void start() {

    }

    @Override
    public void done() {
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
}
