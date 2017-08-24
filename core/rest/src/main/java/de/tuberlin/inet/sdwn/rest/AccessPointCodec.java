package de.tuberlin.inet.sdwn.rest;

import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import de.tuberlin.inet.sdwn.core.api.entity.SdwnAccessPoint;
import de.tuberlin.inet.sdwn.core.api.entity.SdwnClient;
import de.tuberlin.inet.sdwn.core.api.entity.SdwnFrequency;
import de.tuberlin.inet.sdwn.core.api.entity.SdwnFrequencyBand;
import org.onlab.packet.MacAddress;
import org.onosproject.codec.CodecContext;
import org.onosproject.codec.JsonCodec;

import java.util.ArrayList;
import java.util.List;

import static com.google.common.base.Preconditions.checkNotNull;

public class AccessPointCodec extends JsonCodec<SdwnAccessPoint> {

    @Override
    public ObjectNode encode(SdwnAccessPoint ap, CodecContext context) {
        checkNotNull(ap);

        ObjectNode result = context.mapper().createObjectNode()
                .put("name", ap.name())
                .put("ssid", ap.ssid())
                .put("bssid", ap.bssid().toString())
                .put("port_no", ap.portNumber())
                .put("frequency", ap.frequency().hz());

        ArrayNode clients = context.mapper().createArrayNode();

        ap.clients().stream()
                .map(SdwnClient::macAddress)
                .map(MacAddress::toString)
                .forEach(clients::add);

        result.set("clients", clients);

        List<SdwnFrequency> freqs = new ArrayList<>();
        ap.nic().bands().forEach(band -> freqs.addAll(band.frequencies()));
        result.set("supported_frequencies", new FrequencyCodec().encode(freqs, context));

        SdwnFrequencyBand band = ap.nic().bands().stream()
                .filter(b -> b.containsFrequency(ap.frequency().hz()))
                .findFirst().orElse(null);

        if (band != null) {
            result.set("ht_capabilities", new Ieee80211HtCapabilitiesCodec().encode(band.htCapabilities(), context));
        }

        return result;
    }

    @Override
    public ArrayNode encode(Iterable<SdwnAccessPoint> aps, CodecContext context) {
        checkNotNull(aps);

        ArrayNode result = context.mapper().createArrayNode();

        aps.forEach(ap -> {
            ArrayNode clients = context.mapper().createArrayNode();
            ap.clients().stream()
                    .map(SdwnClient::macAddress)
                    .map(MacAddress::toString)
                    .forEach(clients::add);

            ObjectNode apNode = result.addObject()
                    .put("name", ap.name())
                    .put("ssid", ap.ssid())
                    .put("bssid", ap.bssid().toString())
                    .put("port_no", ap.portNumber())
                    .put("frequency", ap.frequency().hz());

            apNode.set("clients", clients);
            List<SdwnFrequency> freqs = new ArrayList<>();
            ap.nic().bands().forEach(band -> freqs.addAll(band.frequencies()));
            apNode.set("supported_frequencies", new FrequencyCodec().encode(freqs, context));

            SdwnFrequencyBand band = ap.nic().bands().stream()
                    .filter(b -> b.containsFrequency(ap.frequency().hz()))
                    .findFirst().orElse(null);

            if (band != null) {
                apNode.set("ht_capabilities", new Ieee80211HtCapabilitiesCodec().encode(band.htCapabilities(), context));
            }
        });

        return result;
    }
}
