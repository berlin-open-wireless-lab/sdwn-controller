package de.tuberlin.inet.sdwn.core.api;

import de.tuberlin.inet.sdwn.core.api.entity.SdwnAccessPoint;
import org.onlab.packet.MacAddress;

public interface Sdwn80211MgmtFrameListener {

    enum ResponseAction {
        NONE,
        GRANT,
        DENY
    }

    ResponseAction receivedProbeRequest(MacAddress clientMac, SdwnAccessPoint atAP, long xid, long rssi, long freq);

    ResponseAction receivedAuthRequest(MacAddress clientMac, SdwnAccessPoint atAP, long xid, long rssi, long freq);

    ResponseAction receivedAssocRequest(MacAddress clientMac, SdwnAccessPoint atAP, long xid, long rssi, long freq);
}
