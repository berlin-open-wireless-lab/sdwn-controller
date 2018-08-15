package de.tuberlin.inet.sdwn.rest;

import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.JsonNodeType;
import com.fasterxml.jackson.databind.node.ObjectNode;
import de.tuberlin.inet.sdwn.core.api.Ieee80211Capability;
import de.tuberlin.inet.sdwn.core.api.Ieee80211HtCapability;
import de.tuberlin.inet.sdwn.core.api.SdwnCoreService;
import de.tuberlin.inet.sdwn.core.api.entity.SdwnClient;
import org.onosproject.codec.CodecContext;
import org.onosproject.codec.JsonCodec;
import org.projectfloodlight.openflow.protocol.OFIeee80211HtCap;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import static com.google.common.base.Preconditions.checkNotNull;

public class ClientCodec extends JsonCodec<SdwnClient> {

    @Override
    public ObjectNode encode(SdwnClient client, CodecContext context) {
        checkNotNull(client, "client cannot be null");
        ObjectNode node = context.mapper().createObjectNode()
                .put("mac", client.macAddress().toString())
                .put("switch", client.ap().nic().switchID().toString())
                .put("ap", client.ap().name())
                .put("association_id", client.assocId());
        node.set("capabilities", buildCapabilities(client.capabilities(), context));
        if (client.htCapabilities() != null) {
            node.set("ht_capabilities", buildHtCapabilities(client.htCapabilities(), context));
        }
        // TODO VHT capabilities
        return node;
    }

    @Override
    public SdwnClient decode(ObjectNode node, CodecContext context) {
        checkNotNull(node);
        return context.getService(SdwnCoreService.class).createClientFromJson(node);
    }

    @Override
    public ArrayNode encode(Iterable<SdwnClient> clients, CodecContext context) {
        checkNotNull(clients, "clients cannot be null");
        ArrayNode array = context.mapper().createArrayNode();

        clients.forEach(client -> {
            ObjectNode objNode = array.addObject()
                    .put("mac", client.macAddress().toString())
                    .put("switch", client.ap().nic().switchID().toString())
                    .put("ap", client.ap().name())
                    .put("bssid", client.ap().bssid().toString())
                    .put("association_id", client.assocId());
            objNode.set("capabilities", buildCapabilities(client.capabilities(), context));
            if (client.htCapabilities() != null) {
                objNode.set("ht_capabilities", buildHtCapabilities(client.htCapabilities(), context));
            }
            // TODO: VHT capabilities
        });
        return array;
    }

    @Override
    public List<SdwnClient> decode(ArrayNode nodes, CodecContext context) {
        List<SdwnClient> clients = new ArrayList<>(nodes.size());
        nodes.forEach(node -> {
            if (node.getNodeType().equals(JsonNodeType.OBJECT)) {
                clients.add(decode((ObjectNode) node, context));
            }
        });
        return clients;
    }

    private ArrayNode buildCapabilities(Set<Ieee80211Capability> caps, CodecContext ctx) {
        ArrayNode node = ctx.mapper().createArrayNode();
        caps.forEach(cap -> node.add(cap.toString()));
        return node;
    }

    private ObjectNode buildHtCapabilities(OFIeee80211HtCap htCaps, CodecContext ctx) {
        ObjectNode node = ctx.mapper().createObjectNode();
        ArrayNode capInfo = node.putArray("capability_info");
        Ieee80211HtCapability.fromInt(htCaps.getCapInfo()).forEach(cap -> capInfo.add(cap.name));

        // TODO: remaining info
        return node;
    }
}
