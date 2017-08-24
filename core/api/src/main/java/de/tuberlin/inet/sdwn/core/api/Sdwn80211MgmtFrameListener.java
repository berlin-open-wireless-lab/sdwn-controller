package de.tuberlin.inet.sdwn.core.api;

import de.tuberlin.inet.sdwn.core.api.entity.SdwnAccessPoint;
import org.onlab.packet.MacAddress;

public interface Sdwn80211MgmtFrameListener {

    void receivedProbeRequest(MacAddress staMac, SdwnAccessPoint atAP, long xid, long rssi, long freq);

    void receivedAuthRequest(MacAddress staMac, SdwnAccessPoint atAP, long xid, long rssi, long freq);

    void receivedAssocRequest(MacAddress staMac, SdwnAccessPoint atAP, long xid, long rssi, long freq);
}
