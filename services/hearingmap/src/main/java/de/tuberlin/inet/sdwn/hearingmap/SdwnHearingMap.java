package de.tuberlin.inet.sdwn.hearingmap;

import de.tuberlin.inet.sdwn.core.api.entity.SdwnAccessPoint;
import org.onlab.packet.MacAddress;
import org.onosproject.openflow.controller.Dpid;

import java.util.Collection;
import java.util.Map;
import java.util.SortedSet;

import static java.lang.System.currentTimeMillis;

public interface SdwnHearingMap {

    Map<MacAddress, Collection<HearingMapEntry>> getState();

    void clear();

    /**
     * Return a sorted set of APs that can potentially manage the given client.
     * Note, the APs' order depends on the radio signal strength according to the
     * {@code compareTo} method in {@code HearingMapEntry}.
     *
     * @param client the client's MAC address
     * @return A set of APs that have seen the given client recently sorted by signal strength.
     */
    SortedSet<HearingMapEntry> getApCandidates(MacAddress client);

    void removeSwitch(Dpid dpid);

    static HearingMapEntry newHearingMapEntry(Dpid dpid, SdwnAccessPoint ap, long rssi, long freq) {
        return new HearingMapEntry(dpid, ap, currentTimeMillis(), (int) rssi, freq);
    }

    class HearingMapEntry implements Comparable<HearingMapEntry> {
        private final Dpid dpid;
        private final SdwnAccessPoint ap;
        private long timestamp;
        private int rssi;
        private long frequency;

        private HearingMapEntry(Dpid dpid, SdwnAccessPoint ap, long timestamp, int rssi, long frequency) {
            this.dpid = dpid;
            this.ap = ap;
            this.timestamp = timestamp;
            this.rssi = rssi;
            this.frequency = frequency;
        }

        public Dpid switchId() {
            return dpid;
        }

        public SdwnAccessPoint ap() {
            return ap;
        }

        public long lastHeard() {
            return timestamp;
        }

        public long signalStrength() {
            return rssi;
        }

        public long frequency() {
            return frequency;
        }

        public void update(long rssi, long frequency) {
            this.rssi = (int) rssi;
            this.frequency = frequency;
            this.timestamp = currentTimeMillis();
        }

        @Override
        public int hashCode() {
            return dpid.hashCode();
        }

        @Override
        public boolean equals(Object obj) {
            return (obj instanceof SdwnHearingMap.HearingMapEntry) && dpid.equals(((SdwnHearingMap.HearingMapEntry) obj).dpid);
        }

        @Override
        public int compareTo(HearingMapEntry o) {
            long diff = this.rssi - o.rssi;
            if (diff > 0) {
                return diff > Integer.MAX_VALUE ? Integer.MAX_VALUE : (int) diff;
            } else if (diff < 0) {
                return diff < Integer.MIN_VALUE ? Integer.MIN_VALUE : (int) diff;
            }

            return 0;
        }
    }
}
