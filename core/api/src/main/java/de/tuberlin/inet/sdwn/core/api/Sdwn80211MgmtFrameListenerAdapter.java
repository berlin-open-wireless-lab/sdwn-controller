package de.tuberlin.inet.sdwn.core.api;

import de.tuberlin.inet.sdwn.core.api.entity.SdwnAccessPoint;
import org.onlab.packet.MacAddress;

public abstract class Sdwn80211MgmtFrameListenerAdapter implements Sdwn80211MgmtFrameListener {
    @Override
    public void receivedProbeRequest(MacAddress staMac, SdwnAccessPoint atAP, long xid, long rssi, long freq) {}

    @Override
    public void receivedAuthRequest(MacAddress staMac, SdwnAccessPoint atAP, long xid, long rssi, long freq) {}

    @Override
    public void receivedAssocRequest(MacAddress staMac, SdwnAccessPoint atAP, long xid, long rssi, long freq) {}
}
