package de.tuberlin.inet.sdwn.core.api;

import de.tuberlin.inet.sdwn.core.api.entity.SdwnAccessPoint;
import org.onlab.packet.MacAddress;

public interface SdwnApListener {

    void apCreated(SdwnAccessPoint ap);

    void apDestroyed(MacAddress bssid);
}
