package de.tuberlin.inet.sdwn.core.api;

import com.fasterxml.jackson.databind.node.ObjectNode;
import de.tuberlin.inet.sdwn.core.api.entity.SdwnAccessPoint;
import de.tuberlin.inet.sdwn.core.api.entity.SdwnClient;
import org.onlab.packet.MacAddress;
import org.onosproject.openflow.controller.Dpid;
import org.projectfloodlight.openflow.protocol.OFMessage;

import java.util.NoSuchElementException;
import java.util.Set;

/**
 * SDWN core controller.
 */
public interface SdwnCoreService {

    /**
     * Get a set of associated clients.
     */
    Set<SdwnClient> clients();

    /**
     * Get a set of BSSIDs.
     */
    Set<MacAddress> bssids();

    /**
     * Get a set of all APs.
     */
    Set<SdwnAccessPoint> aps();

    /**
     * Get a set of APs on a given switch.
     * @throws NoSuchElementException
     */
    Set<SdwnAccessPoint> apsForSwitch(Dpid dpid) throws NoSuchElementException;

    /**
     * Get a set of the Datapath IDs of all connected switches.
     */
    Set<Dpid> switches();

    /**
     * Get a set of APs belonging to the given BSS.
     */
    Set<SdwnAccessPoint> apsForBssid(MacAddress bssid);

    /**
     * Look up an AP by its switch's Datapath ID and its name.
     */
    SdwnAccessPoint apByDpidAndName(Dpid dpid, String name);

    /**
     * Used to notify the controller of a new client at an AP.
     *
     * @param atAp the AP where the new client is associated
     * @param client the client
     */
    void newClient(SdwnAccessPoint atAp, SdwnClient client);

    /**
     * Send an Add Client message to the switch hosting {@code ap} to inject
     * {@code client}'s state.

     * @param dstAp the access point where the client state is to be injected
     * @param client the client to be added
     * @return true if adding was successfully initiated, false on error
     */
    boolean addClientToAp(SdwnAccessPoint dstAp, SdwnClient client);

    /**
     * Send an Delete Client message to the switch hosting the client's AP.
     *
     * @param mac the client's MAC address
     * @param banTime time the client will be banned from re-association (in ms)
     * @return true on success, false otherwise
     */
    boolean removeClientFromAp(MacAddress mac, long banTime);

    /**
     * Remove all state related to the given client from the controller. This
     * also deletes the related state in ONOS' host database.
     *
     * @param client the client to be removed
     */
    void removeClient(SdwnClient client);

    /**
     * Look up the client using its MAC address.
     *
     * @param mac the client's MAC address
     * @return the client
     */
    SdwnClient getClient(MacAddress mac);

    /**
     * Make an AP change its operating channel. The AP will send a Channel Switch
     * Announcement.
     *
     * @param dpid the Datapath ID of the switch where the AP is located
     * @param ifNo the AP's port number
     * @param freq the target hz
     * @param beaconCount the number of beacon frames after which the channel will be switched
     * @return true on success, false otherwise
     */
    boolean setChannel(Dpid dpid, int ifNo, int freq, int beaconCount);

    boolean registerClientAuthenticator(SdwnClientAuthenticatorService authenticator);

    void removeClientAuthenticator(SdwnClientAuthenticatorService authenticator);

    /**
     * Register an {@code SdwnSwitchListener} to hook into the controller's switch lifecycle
     * management.
     *
     * @param listener the switch listener
     * @throws IllegalArgumentException
     */
    void registerSwitchListener(SdwnSwitchListener listener) throws IllegalArgumentException;

    /**
     * Un-register the given {@code SdwnSwitchListener}.
     */
    void removeSwitchListener(SdwnSwitchListener listener);

    /**
     * Register an {@code SdwnClientListener} to hook into the controller's client
     * lifecycle management.
     *
     * @param listener the listener
     * @throws IllegalArgumentException
     */
    void registerClientListener(SdwnClientListener listener) throws IllegalArgumentException;

    /**
     * Un-register the given {@code SdwnClientListener}.
     */
    void removeClientListener(SdwnClientListener listener);

    /**
     * Register an {@code Sdwn80211MgmtFrameListener} to receive notifications
     * about incoming 802.11 management frames.
     *
     * @param listener the listener
     * @throws IllegalArgumentException
     */
    void register80211MgtmFrameListener(Sdwn80211MgmtFrameListener listener, int priority) throws IllegalArgumentException;

    /**
     * Un-register the given {@code Sdwn80211MgmtFrameListener}.
     */
    void remove80211MgmtFrameListener(Sdwn80211MgmtFrameListener listener);


    /**
     * Start the given transaction.
     *
     * @param t the transaction
     * @return the XID
     */
    long startTransaction(SdwnTransactionContext t, long timeout);

    SdwnClient createClientFromJson(ObjectNode node);

    /**
     * Get the Datapath ID of the related OpenFlow siwitch for the given switch.
     */
    Dpid getRelatedOfSwitch(Dpid dpid);

    /**
     * Send a message to a switch.
     *
     * @param dpid Datapath ID of the destination switch
     * @param msg OpenFlow message to send
     * @return true on success, false on failure
     */
    boolean sendMsg(Dpid dpid, OFMessage msg);
}
