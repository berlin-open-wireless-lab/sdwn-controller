package de.tuberlin.inet.sdwn.core.api.entity;

public interface SdwnFrequency extends SdwnEntity {

    /**
     * Get the hz in MHz.
     */
    long hz();

    /**
     * Get the maximum transmission power.
     */
    double maxTxPower();

    @Override
    default SdwnEntity.Type type() {
        return Type.SDWN_ENTITY_FREQ;
    }
}
