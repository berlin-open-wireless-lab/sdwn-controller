package de.tuberlin.inet.sdwn.core.api.entity;

/**
 * Transmission rate abstraction.
 */
// TODO: do we really need an object for this?
public interface SdwnTransmissionRate extends SdwnEntity {

    long rate();

    @Override
    default SdwnEntity.Type type() {
        return Type.SDWN_ENTITY_RATE;
    }
}
