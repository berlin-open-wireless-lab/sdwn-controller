package de.tuberlin.inet.sdwn.core.api;

import de.tuberlin.inet.sdwn.core.api.entity.SdwnAccessPoint;
import org.onlab.packet.MacAddress;

public abstract class Sdwn80211MgmtFrameListenerAdapter implements Sdwn80211MgmtFrameListener {
    @Override
    public ResponseAction receivedProbeRequest(MacAddress clientMac, SdwnAccessPoint atAP, long xid, long rssi, long freq) {
        return ResponseAction.NONE;
    }

    @Override
    public ResponseAction receivedAuthRequest(MacAddress clientMac, SdwnAccessPoint atAP, long xid, long rssi, long freq) {
        return ResponseAction.NONE;
    }

    @Override
    public ResponseAction receivedAssocRequest(MacAddress clientMac, SdwnAccessPoint atAP, long xid, long rssi, long freq) {
        return ResponseAction.NONE;
    }
}
