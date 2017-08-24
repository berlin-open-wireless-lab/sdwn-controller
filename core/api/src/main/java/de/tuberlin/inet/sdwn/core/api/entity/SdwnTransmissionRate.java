package de.tuberlin.inet.sdwn.core.api.entity;

public interface SdwnTransmissionRate extends SdwnEntity {

    long rate();

    @Override
    default SdwnEntity.Type type() {
        return Type.SDWN_ENTITY_RATE;
    }
}
