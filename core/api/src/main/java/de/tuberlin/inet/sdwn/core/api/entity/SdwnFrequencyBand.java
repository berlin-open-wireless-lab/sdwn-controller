package de.tuberlin.inet.sdwn.core.api.entity;

import de.tuberlin.inet.sdwn.core.api.Ieee80211HtCapability;
import org.projectfloodlight.openflow.protocol.OFIeee80211VhtCap;

import java.util.List;

import static de.tuberlin.inet.sdwn.core.api.entity.SdwnEntity.Type.SDWN_ENTITY_BAND;

/**
 * Frequency band abstraction.
 */
public interface SdwnFrequencyBand extends SdwnEntity, Comparable<SdwnFrequencyBand> {

    int bandNumber();

    /**
     * Get a list of frequencies contained in the frequency band.
     */
    List<SdwnFrequency> frequencies();

    /**
     * Get a list of transmission rates.
     */
    List<SdwnTransmissionRate> rates();

    /**
     * Get the HT capabilities of the frequency band.
     */
    Ieee80211HtCapability htCapabilities();

    /**
     * Get the VHT capabilities of the frequency band.
     */
    // TODO: replace with own abstraction type
    OFIeee80211VhtCap vhtCapabilities();

    /**
     * Convenience method to check whether the given frequency is contained in the frequency band.
     */
    boolean containsFrequency(long freq);

    @Override
    default SdwnEntity.Type type() {
        return SDWN_ENTITY_BAND;
    }

    @Override
    default int compareTo(SdwnFrequencyBand o) {
        return this.bandNumber() - o.bandNumber();
    }
}
