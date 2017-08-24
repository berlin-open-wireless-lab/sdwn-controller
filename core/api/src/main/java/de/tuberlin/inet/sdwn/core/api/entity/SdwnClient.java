package de.tuberlin.inet.sdwn.core.api.entity;

import de.tuberlin.inet.sdwn.core.api.Ieee80211Capability;
import org.onlab.packet.MacAddress;
import org.projectfloodlight.openflow.protocol.OFIeee80211HtCap;
import org.projectfloodlight.openflow.protocol.OFIeee80211VhtCap;

import java.util.List;
import java.util.Set;

public interface SdwnClient extends SdwnEntity {

    /**
     * Get the client's IEEE 802.11 capabilities.
     */
    Set<Ieee80211Capability> capabilities();

    /**
     * Get the client's MAC address.
     */
    MacAddress macAddress();

    /**
     * Get the client's supported transmission rates.
     */
    List<SdwnTransmissionRate> rates();

    /**
     * Get the client's {@code SdwnAccessPoint}.
     */
    SdwnAccessPoint ap();

    /**
     * Get the client's association ID.
     */
    int assocId();

    /**
     * Get the client's HT capabilities.
     */
    OFIeee80211HtCap htCapabilities();

    /**
     * Get the client's VHT capabilities.
     */
    OFIeee80211VhtCap vhtCapabilities();

    /**
     * Get the client's cryptographic keys.
     */
    SdwnClientCryptoKeys keys();

    /**
     * Set the client's capabilities.
     */
    void setCapabilities(int capabilities);

    /**
     * Set the client's capabilities.
     */
    void setCapabilities(Set<Ieee80211Capability> caps);

    /**
     * Set the client's HT capabilities.
     */
    void setHtCapabilities(OFIeee80211HtCap htCap);

    /**
     * Set the client's VHT capabilities.
     */
    void setVhtCapabilities(OFIeee80211VhtCap vhtCap);

    /**
     * Set the references within this {@code SdwnClient} and the given {@code SdwnAccessPoint}
     * to represent the association.
     */
    void assoc(SdwnAccessPoint ap);

    /**
     * Remove all reference between the {@code SdwnClient} and its {@code SdwnAccessPoint}.
     */
    void disassoc();

    @Override
    default SdwnEntity.Type type() {
        return Type.SDWN_ENTITY_CLIENT;
    }
}
