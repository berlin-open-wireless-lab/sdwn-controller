package de.tuberlin.inet.sdwn.core.api;

import de.tuberlin.inet.sdwn.core.api.entity.SdwnAccessPoint;
import org.onlab.packet.MacAddress;

public interface SdwnClientListener {

    void staAuthenticated(MacAddress staMac, SdwnAccessPoint atAP);

    void staDeauthenticated(MacAddress staMac, SdwnAccessPoint fromAp);

    void staAssociated(MacAddress staMac, SdwnAccessPoint atAP);

    void staDisassociated(MacAddress staMac, SdwnAccessPoint fromAP);
}
