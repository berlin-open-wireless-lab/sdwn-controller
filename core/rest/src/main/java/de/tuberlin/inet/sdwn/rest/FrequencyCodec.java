package de.tuberlin.inet.sdwn.rest;

import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import de.tuberlin.inet.sdwn.core.api.entity.SdwnFrequency;
import org.onosproject.codec.CodecContext;
import org.onosproject.codec.JsonCodec;

public class FrequencyCodec extends JsonCodec<SdwnFrequency> {
    @Override
    public ObjectNode encode(SdwnFrequency freq, CodecContext context) {
        return context.mapper().createObjectNode()
                .put("hz", freq.hz())
                .put("max_tx_power", freq.maxTxPower());
    }

    @Override
    public ArrayNode encode(Iterable<SdwnFrequency> freqs, CodecContext context) {
        ArrayNode node = context.mapper().createArrayNode();

        freqs.forEach(freq -> node.addObject()
                .put("hz", freq.hz())
                .put("max_tx_power", freq.maxTxPower()));

        return node;
    }
}
