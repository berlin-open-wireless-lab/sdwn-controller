package de.tuberlin.inet.sdwn.core.ctl;

import com.fasterxml.jackson.databind.node.ObjectNode;
import de.tuberlin.inet.sdwn.core.api.*;
import de.tuberlin.inet.sdwn.core.api.entity.SdwnAccessPoint;
import de.tuberlin.inet.sdwn.core.api.entity.SdwnClient;
import org.onlab.packet.MacAddress;
import org.onosproject.openflow.controller.Dpid;

import java.util.NoSuchElementException;
import java.util.Set;

public class SdwnControllerAdapter implements SdwnCoreService {

    @Override
    public Set<SdwnClient> clients() {
        return null;
    }

    @Override
    public Set<MacAddress> bssids() {
        return null;
    }

    @Override
    public Set<SdwnAccessPoint> aps() {
        return null;
    }

    @Override
    public Set<SdwnAccessPoint> apsForSwitch(Dpid dpid) throws NoSuchElementException {
        return null;
    }

    @Override
    public Set<Dpid> switches() {
        return null;
    }

    @Override
    public Set<SdwnAccessPoint> apsForBssid(MacAddress bssid) {
        return null;
    }

    @Override
    public SdwnAccessPoint apByDpidAndName(Dpid dpid, String name) {
        return null;
    }

    @Override
    public void newClient(SdwnAccessPoint atAp, SdwnClient client) {

    }

    @Override
    public boolean addClientToAp(SdwnAccessPoint dstAp, SdwnClient client) {
        return false;
    }

    @Override
    public boolean removeClientFromAp(MacAddress mac, long banTime) {
        return false;
    }

    @Override
    public void removeClient(SdwnClient client) {

    }

    @Override
    public SdwnClient getClient(MacAddress mac) {
        return null;
    }

    @Override
    public boolean setChannel(Dpid dpid, int ifNo, int freq, int beaconCount) {
        return false;
    }

    @Override
    public boolean registerClientAuthenticator(SdwnClientAuthenticatorService authenticator) {
        return false;
    }

    @Override
    public void removeClientAuthenticator(SdwnClientAuthenticatorService authenticator) {

    }

    @Override
    public void registerSwitchListener(SdwnSwitchListener listener) throws IllegalArgumentException {

    }

    @Override
    public void removeSwitchListener(SdwnSwitchListener listener) {

    }

    @Override
    public void registerClientListener(SdwnClientListener listener) throws IllegalArgumentException {

    }

    @Override
    public void removeClientListener(SdwnClientListener listener) {

    }

    @Override
    public void register80211MgtmFrameListener(Sdwn80211MgmtFrameListener listener, int priority) throws IllegalArgumentException {

    }

    @Override
    public void remove80211MgmtFrameListener(Sdwn80211MgmtFrameListener listener) {

    }

    @Override
    public long startTransaction(SdwnTransactionContext t, long timeout) {
        return 0;
    }

    @Override
    public SdwnClient createClientFromJson(ObjectNode node) {
        return null;
    }

    @Override
    public Dpid getRelatedOfSwitch(Dpid dpid) {
        return null;
    }
}
