package de.tuberlin.inet.sdwn.core.ctl.entity;

import com.google.common.base.MoreObjects;
import de.tuberlin.inet.sdwn.core.api.*;
import de.tuberlin.inet.sdwn.core.api.Ieee80211HtCapability;
import de.tuberlin.inet.sdwn.core.api.entity.SdwnEntityParsingException;
import de.tuberlin.inet.sdwn.core.api.entity.SdwnFrequency;
import de.tuberlin.inet.sdwn.core.api.entity.SdwnFrequencyBand;
import de.tuberlin.inet.sdwn.core.api.entity.SdwnTransmissionRate;
import org.projectfloodlight.openflow.protocol.OFIeee80211HtCap;
import org.projectfloodlight.openflow.protocol.OFIeee80211VhtCap;
import org.projectfloodlight.openflow.protocol.OFSdwnEntity;
import org.projectfloodlight.openflow.protocol.OFSdwnEntityBand;
import org.projectfloodlight.openflow.protocol.OFSdwnEntityFreq;
import org.projectfloodlight.openflow.protocol.OFSdwnEntityRate;

import java.util.List;
import java.util.stream.Collectors;

public class FrequencyBand implements SdwnFrequencyBand {

    private int bandNumber;
    private List<SdwnFrequency> freqs;
    private List<SdwnTransmissionRate> rates;
    private Ieee80211HtCapability htCap;
    // TODO: write own VHT capability type
    private OFIeee80211VhtCap vhtCap;

    private FrequencyBand(int bandNumber, List<SdwnFrequency> freqs,
                          List<SdwnTransmissionRate> rates, Ieee80211HtCapability htCap,
                          OFIeee80211VhtCap vhtCap) {
        this.bandNumber = bandNumber;
        this.freqs = freqs;
        this.rates = rates;
        this.htCap = htCap;
        this.vhtCap = vhtCap;
    }

    @Override
    public Type type() {
        return Type.SDWN_ENTITY_BAND;
    }

    public static FrequencyBand fromOF(long index,
                                       OFSdwnEntityBand ofEntity, List<OFSdwnEntity> entities)
            throws SdwnEntityParsingException {

        int bandNo = ofEntity.getBandNo();

        List<SdwnFrequency> freqs = entities.stream()
                .filter(OFSdwnEntityFreq.class::isInstance)
                .map(OFSdwnEntityFreq.class::cast)
                .filter(f -> f.getIndex() == index)
                .filter(f -> f.getBandNo() == bandNo)
                .map(Frequency::fromOF)
                .collect(Collectors.toList());

        List<SdwnTransmissionRate> rates = entities.stream()
                .filter(OFSdwnEntityRate.class::isInstance)
                .map(OFSdwnEntityRate.class::cast)
                .filter(f -> f.getIndex() == index)
                .filter(f -> f.getBandNo() == bandNo)
                .map(TransmissionRate::fromOF)
                .collect(Collectors.toList());

        return new FrequencyBand(bandNo, freqs, rates,
                                 Ieee80211HtCapability.fromOF(ofEntity.getHtCapabilities()),
                                 ofEntity.getVhtCapabilities());
    }

    public int bandNumber() {
        return bandNumber;
    }

    public List<SdwnFrequency> frequencies() {
        return freqs;
    }

    public List<SdwnTransmissionRate> rates() {
        return rates;
    }

    public de.tuberlin.inet.sdwn.core.api.Ieee80211HtCapability htCapabilities() {
        return htCap;
    }

    public OFIeee80211VhtCap vhtCapabilities() {
        return vhtCap;
    }

    public boolean containsFrequency(long freq) {
        for (SdwnFrequency f : freqs) {
            if (f.hz() == freq) {
                return true;
            }
        }
        return false;
    }

    @Override
    public SdwnFrequency getFreq(int freq) {
        return freqs.stream().filter(f -> f.hz() == freq).findFirst().orElse(null);
    }

    @Override
    public String toString() {
        MoreObjects.ToStringHelper helper = MoreObjects.toStringHelper(this);
        return helper.add("Band number", bandNumber)
                .add("Supported frequencies", freqs)
                .add("Supported rates", rates)
                .add("HT capabilities", htCap)
                .add("VHT capabilities", vhtCap)
                .toString();
    }
}
