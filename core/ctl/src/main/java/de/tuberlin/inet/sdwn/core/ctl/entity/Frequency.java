package de.tuberlin.inet.sdwn.core.ctl.entity;

import com.google.common.base.MoreObjects;
import de.tuberlin.inet.sdwn.core.api.entity.SdwnFrequency;
import org.projectfloodlight.openflow.protocol.OFSdwnEntityFreq;

import static com.google.common.base.MoreObjects.toStringHelper;

public class Frequency implements SdwnFrequency {

    private final long freq;
    private final double maxTxPower;

    private Frequency(long freq, double maxTxPower) {
        this.freq = freq;
        this.maxTxPower = maxTxPower;
    }

    public static Frequency fromOF(OFSdwnEntityFreq entity) {
        return new Frequency(entity.getFreq(), entity.getMaxTxPower() * 0.01);
    }

    @Override
    public long hz() {
        return freq;
    }

    @Override
    public double maxTxPower() {
        return maxTxPower;
    }

    @Override
    public String toString() {
        MoreObjects.ToStringHelper helper = toStringHelper(this);
        return helper.add("Frequency", String.valueOf(freq) + " Hz")
                .add("Max TX power", String.valueOf(maxTxPower) + "dBm")
                .toString();
    }
}
