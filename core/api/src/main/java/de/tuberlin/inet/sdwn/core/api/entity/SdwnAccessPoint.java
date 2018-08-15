package de.tuberlin.inet.sdwn.core.api.entity;

import org.onlab.packet.MacAddress;

import java.util.List;

public interface SdwnAccessPoint extends SdwnEntity {

    /**
     * Get the AP's name.
     */
    String name();

    /**
     * Get the NIC that is hosting the AP.
     */
    SdwnNic nic();

    /**
     * Get the AP's advertised SSID.
     */
    String ssid();

    /**
     * Get the AP's BSSID.
     */
    MacAddress bssid();

    /**
     * Get the AP's operating hz.
     */
    SdwnFrequency frequency();

    /**
     * Get a list of this AP's clients.
     */
    List<SdwnClient> clients();

    /**
     * Add the given client to the AP.
     */
    void addClient(SdwnClient c);

    /**
     * Remove the given client from the AP.
     */
    void removeClient(SdwnClient c);

    /**
     * Get the AP's port number (interface number).
     */
    int portNumber();

    /**
     * Set the AP's operating hz.
     */
    void setFrequency(SdwnFrequency freq);

    /**
     * Return {@code true} if the client with the given MAC address is associated
     * with the AP, false otherwise.
     */
    boolean clientIsAssociated(MacAddress mac);

    /**
     * Blacklist a client at the AP for a given time.
     * @param mac client MAC addres
     */
    void blacklistClient(MacAddress mac);

    /**
     * Return {@code true} if the given client is blacklisted at the AP.
     */
    boolean clientIsBlacklisted(MacAddress mac);

    /**
     * Remove a client from the APs blacklist.
     */
    void clearClientBlacklisting(MacAddress mac);

    @Override
    default Type type() {
        return Type.SDWN_ENTITY_ACCESSPOINT;
    }
}
