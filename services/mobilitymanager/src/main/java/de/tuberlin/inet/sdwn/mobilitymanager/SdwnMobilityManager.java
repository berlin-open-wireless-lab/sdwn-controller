package de.tuberlin.inet.sdwn.mobilitymanager;

import de.tuberlin.inet.sdwn.core.api.entity.SdwnAccessPoint;
import de.tuberlin.inet.sdwn.core.api.entity.SdwnClient;

public interface SdwnMobilityManager {

    void handOver(SdwnClient c, SdwnAccessPoint dst);

    void handOver(SdwnClient c, SdwnAccessPoint dst, long timeout);
}
