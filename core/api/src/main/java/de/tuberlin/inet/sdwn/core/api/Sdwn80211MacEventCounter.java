package de.tuberlin.inet.sdwn.core.api;

public interface Sdwn80211MacEventCounter {

    long probesReceived();

    long authRequestsReceived();

    long assocRequestsReceived();

    void reset();
}
