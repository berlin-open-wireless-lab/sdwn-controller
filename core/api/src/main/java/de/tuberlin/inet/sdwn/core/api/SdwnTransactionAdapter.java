package de.tuberlin.inet.sdwn.core.api;

public abstract class SdwnTransactionAdapter implements SdwnTransaction {

    @Override
    public void timedOut() {
    }

    @Override
    public void start(long xid) {
    }

    @Override
    public void done() {
    }

    @Override
    public void aborted() {
    }
}
