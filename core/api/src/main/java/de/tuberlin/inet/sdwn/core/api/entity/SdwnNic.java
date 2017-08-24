package de.tuberlin.inet.sdwn.core.api.entity;

import org.onlab.packet.MacAddress;
import org.onosproject.openflow.controller.Dpid;

import java.util.List;

public interface SdwnNic extends SdwnEntity {

    Dpid switchID();

    long index();

    MacAddress mac();

    List<SdwnAccessPoint> aps();

    void addAP(SdwnAccessPoint ap);

    void removeAP(String name);

    List<SdwnFrequencyBand> bands();

    boolean supportsFrequency(int freq);

    @Override
    default SdwnEntity.Type type() {
        return Type.SDWN_ENTITY_NIC;
    }
}
