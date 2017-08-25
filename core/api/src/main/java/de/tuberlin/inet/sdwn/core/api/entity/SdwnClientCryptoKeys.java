package de.tuberlin.inet.sdwn.core.api.entity;

/**
 * Client cryptographic keys abstraction.
 */
public interface SdwnClientCryptoKeys extends SdwnEntity {

    byte[] pmk();

    byte[] kck();

    byte[] kek();

    byte[] tk();

    byte[] seq();

    default SdwnEntity.Type type() {
        return Type.SDWN_ENTITY_CLIENTKEYS;
    }
}
