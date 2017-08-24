package de.tuberlin.inet.sdwn.core.api.entity;

import de.tuberlin.inet.sdwn.core.api.Ieee80211HtCapability;
import org.projectfloodlight.openflow.protocol.OFIeee80211VhtCap;

import java.util.List;

import static de.tuberlin.inet.sdwn.core.api.entity.SdwnEntity.Type.SDWN_ENTITY_BAND;

public interface SdwnFrequencyBand extends SdwnEntity, Comparable<SdwnFrequencyBand> {

    int bandNumber();

    List<SdwnFrequency> frequencies();

    List<SdwnTransmissionRate> rates();

    Ieee80211HtCapability htCapabilities();

    // TODO: replace with own abstraction type
    OFIeee80211VhtCap vhtCapabilities();

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
