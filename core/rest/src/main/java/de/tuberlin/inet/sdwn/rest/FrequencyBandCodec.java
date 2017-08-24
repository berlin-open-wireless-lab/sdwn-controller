package de.tuberlin.inet.sdwn.rest;

import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import de.tuberlin.inet.sdwn.core.api.entity.SdwnFrequencyBand;
import org.onosproject.codec.CodecContext;
import org.onosproject.codec.JsonCodec;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static com.google.common.base.Preconditions.checkNotNull;

public class FrequencyBandCodec extends JsonCodec<SdwnFrequencyBand> {
    @Override
    public ObjectNode encode(SdwnFrequencyBand band, CodecContext context) {
        checkNotNull(band);

        if (band.frequencies().isEmpty()) {
            return context.mapper().createObjectNode();
        }

        ObjectNode result = context.mapper().createObjectNode()
                .put("number", band.bandNumber());

        result.set("frequencies", new FrequencyCodec().encode(band.frequencies(), context));
        return result;
    }

    @Override
    public ArrayNode encode(Iterable<SdwnFrequencyBand> bands, CodecContext context) {
        checkNotNull(bands);

        ArrayNode result = context.mapper().createArrayNode();

        List<SdwnFrequencyBand> sortedBands = new ArrayList<>();
        bands.forEach(sortedBands::add);
        Collections.sort(sortedBands);

        sortedBands.forEach(band -> {
            if (!band.frequencies().isEmpty()) {
                result.addObject()
                        .put("number", band.bandNumber())
                        .set("frequencies", new FrequencyCodec().encode(band.frequencies(), context));
            }
        });
        return result;
    }
}
