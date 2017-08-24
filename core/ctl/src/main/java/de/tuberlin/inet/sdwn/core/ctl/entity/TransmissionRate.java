package de.tuberlin.inet.sdwn.core.ctl.entity;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.JsonNodeType;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.google.common.base.MoreObjects;
import de.tuberlin.inet.sdwn.core.api.entity.SdwnEntityParsingException;
import de.tuberlin.inet.sdwn.core.api.entity.SdwnTransmissionRate;
import org.projectfloodlight.openflow.protocol.OFSdwnEntityRate;

import java.util.ArrayList;
import java.util.List;

public class TransmissionRate implements SdwnTransmissionRate {

    private long rate;

    private TransmissionRate(long rate) {
        this.rate = rate;
    }

    @Override
    public Type type() {
        return Type.SDWN_ENTITY_RATE;
    }

    public static TransmissionRate fromOF(OFSdwnEntityRate entity) {
        return new TransmissionRate(entity.getRate());
    }

    public static TransmissionRate fromByte(byte b) {
        return new TransmissionRate((long) (b & 0x7f));
    }

    public long rate() {
        return rate;
    }

    @Override
    public String toString() {
        MoreObjects.ToStringHelper helper = MoreObjects.toStringHelper(this);
        return helper.add("Rate", String.format("%2.1f", rate * 0.1) + "Mbps").toString();
    }

    public static List<SdwnTransmissionRate> fromJson(ArrayNode nodes) throws IllegalArgumentException {
        List<SdwnTransmissionRate> rates = new ArrayList<>(nodes.size());
        for (JsonNode node : nodes) {
            if (!node.getNodeType().equals(JsonNodeType.NUMBER)) {
                throw new IllegalArgumentException("Transmission rates array must contain numbers");
            }
            rates.add(new TransmissionRate(node.asLong()));
        }
        return rates;
    }

    public static TransmissionRate fromJson(ObjectNode node) throws IllegalArgumentException {
        throw new IllegalArgumentException("Transmission rates cannot be constructed from JSON objects.");
    }
}
