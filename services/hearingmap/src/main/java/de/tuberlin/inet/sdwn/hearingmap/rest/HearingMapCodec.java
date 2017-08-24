package de.tuberlin.inet.sdwn.hearingmap.rest;

import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import de.tuberlin.inet.sdwn.hearingmap.api.SdwnHearingMap;
import org.onlab.packet.MacAddress;
import org.onosproject.codec.CodecContext;
import org.onosproject.codec.JsonCodec;

import java.util.Collection;
import java.util.Map;

import static com.google.common.base.Preconditions.checkNotNull;
import static java.lang.System.currentTimeMillis;

public class HearingMapCodec extends JsonCodec<Map<MacAddress, Collection<SdwnHearingMap.HearingMapEntry>>> {

    @Override
    public ObjectNode encode(Map<MacAddress, Collection<SdwnHearingMap.HearingMapEntry>> map, CodecContext context) {
        checkNotNull(map);

        ObjectNode table = context.mapper().createObjectNode();
        map.forEach((clientMac, entries) -> table.set(clientMac.toString(), encodeHearingMapEntries(entries, context)));
        return table;
    }

    private ArrayNode encodeHearingMapEntries(Collection<SdwnHearingMap.HearingMapEntry> entries, CodecContext ctx) {
        ArrayNode array = ctx.mapper().createArrayNode();

        entries.forEach(entry -> array.addObject()
                .put("ap", entry.ap().name())
                .put("switch", entry.switchId().toString())
                .put("hz", entry.frequency())
                .put("signal_strength", entry.signalStrength())
                .put("seconds_since", (currentTimeMillis() - entry.lastHeard()) / 1000));
        return array;
    }
}
