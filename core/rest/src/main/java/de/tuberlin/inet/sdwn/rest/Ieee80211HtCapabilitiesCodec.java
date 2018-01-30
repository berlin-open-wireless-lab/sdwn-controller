package de.tuberlin.inet.sdwn.rest;

import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import de.tuberlin.inet.sdwn.core.api.Ieee80211HtCapability;
import org.onosproject.codec.CodecContext;
import org.onosproject.codec.JsonCodec;
import org.projectfloodlight.openflow.protocol.OFIeee80211HtCap;

import static com.google.common.base.Preconditions.checkNotNull;

public class Ieee80211HtCapabilitiesCodec extends JsonCodec<Ieee80211HtCapability> {
    @Override
    public ObjectNode encode(Ieee80211HtCapability htCap, CodecContext context) {
        checkNotNull(htCap);

        ObjectNode result = context.mapper().createObjectNode();
        ArrayNode htCapabilitiesNode = result.putArray("ht_capability_info");
        htCap.getCapInfo().stream()
            .map(c -> c.name)
            .forEach(htCapabilitiesNode::add);

        result.put("max_rx_ampdu_length", htCap.getMaxRxAmpduLen());
        result.put("min_rx_ampdu_time_spacing", htCap.getMinRxAmpduSpacing());
        result.put("mcs_rx_mask", htCap.getMcsRxMask());
        result.put("mcs_rx_highest", htCap.getMcsRxHighest());
        result.put("mcs_tx_params", htCap.getMcsTxParams());
        return result;
    }
}
