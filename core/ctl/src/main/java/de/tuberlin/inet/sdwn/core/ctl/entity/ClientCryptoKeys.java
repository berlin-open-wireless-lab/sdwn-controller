package de.tuberlin.inet.sdwn.core.ctl.entity;

import com.fasterxml.jackson.databind.node.ObjectNode;
import com.google.common.collect.ImmutableList;
import de.tuberlin.inet.sdwn.core.api.entity.SdwnClientCryptoKeys;
import org.projectfloodlight.openflow.protocol.OFFactory;
import org.projectfloodlight.openflow.protocol.bsntlv.OFBsnTlvData;

import java.math.BigInteger;
import java.util.List;

public class ClientCryptoKeys implements SdwnClientCryptoKeys {

    private final byte[] pmk;
    private final byte[] kck;
    private final byte[] kek;
    private final byte[] tk;
    private final byte[] seq;

    private ClientCryptoKeys(List<OFBsnTlvData> keys) {
        pmk = keys.get(0).getValue();
        kck = keys.get(1).getValue();
        kek = keys.get(2).getValue();
        tk = keys.get(3).getValue();
        seq = keys.get(4).getValue();
    }

    private ClientCryptoKeys(String pmkStr, String kckStr, String kekStr, String tkStr, String seqStr) {
            pmk = new BigInteger(pmkStr, 16).toByteArray();
            kck = new BigInteger(kckStr, 16).toByteArray();
            kek = new BigInteger(kekStr, 16).toByteArray();
            tk = new BigInteger(tkStr, 16).toByteArray();
            seq = new BigInteger(seqStr, 16).toByteArray();
    }

    public static ClientCryptoKeys fromOf(List<OFBsnTlvData> keys) {
        if (keys.size() != 5) {
            return null;
        }

        return new ClientCryptoKeys(keys);
    }

    public static ClientCryptoKeys fromJsonObject(ObjectNode node) throws IllegalArgumentException {
        try {
            return new ClientCryptoKeys(node.get("pmk").asText(),
                                        node.get("kck").asText(),
                                        node.get("kek").asText(),
                                        node.get("tk").asText(),
                                        node.get("seq").asText());
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException(e.getMessage());
        }
    }

    public static List<OFBsnTlvData> toOF(SdwnClientCryptoKeys keys, OFFactory factory) {
        return ImmutableList.of(
                factory.bsnTlvs().data(keys.pmk()),
                factory.bsnTlvs().data(keys.kck()),
                factory.bsnTlvs().data(keys.kek()),
                factory.bsnTlvs().data(keys.tk()),
                factory.bsnTlvs().data(keys.seq()));
    }

    public byte[] pmk() {
        return pmk;
    }

    public byte[] kck() {
        return kck;
    }

    public byte[] kek() {
        return kek;
    }

    public byte[] tk() {
        return tk;
    }

    public byte[] seq() {
        return seq;
    }
}
