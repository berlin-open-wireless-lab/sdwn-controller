package de.tuberlin.inet.sdwn.core.api.entity;

public interface SdwnEntity {

    enum Type {
        SDWN_ENTITY_NIC,
        SDWN_ENTITY_ACCESSPOINT,
        SDWN_ENTITY_CLIENTKEYS,
        SDWN_ENTITY_BAND,
        SDWN_ENTITY_FREQ,
        SDWN_ENTITY_RATE,
        SDWN_ENTITY_CLIENT,
    }

    Type type();
}
