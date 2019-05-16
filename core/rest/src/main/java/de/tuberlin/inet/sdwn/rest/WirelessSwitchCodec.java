package de.tuberlin.inet.sdwn.rest;

import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import de.tuberlin.inet.sdwn.core.api.SdwnCoreService;
import de.tuberlin.inet.sdwn.core.api.entity.SdwnAccessPoint;
import de.tuberlin.inet.sdwn.core.api.entity.SdwnFrequencyBand;
import de.tuberlin.inet.sdwn.core.api.entity.SdwnNic;
import org.onosproject.codec.CodecContext;
import org.onosproject.codec.JsonCodec;
import org.onosproject.openflow.controller.Dpid;
import org.onosproject.openflow.controller.SdwnWirelessSwitch;

import java.util.HashSet;
import java.util.Set;

import static com.google.common.base.Preconditions.checkNotNull;

public class WirelessSwitchCodec extends JsonCodec<SdwnWirelessSwitch> {

    @Override
    public ObjectNode encode(SdwnWirelessSwitch sw, CodecContext context) {
        checkNotNull(sw);
        SdwnCoreService service = context.getService(SdwnCoreService.class);

        ObjectNode node = context.mapper().createObjectNode()
                .put("datapath_id", sw.getStringId())
                .put("channel_id", sw.channelId())
                .put("manufacturer", sw.manufacturerDescription())
                .put("hardware", sw.hardwareDescription())
                .put("software", sw.softwareDescription())
                .put("datapath_desc", sw.datapathDescription())
                .put("serial_no", sw.serialNumber());

        if (sw.relatedOfSwitch() != null) {
            node.put("related_of_switch", sw.relatedOfSwitch().toString());
        }

        ArrayNode aps = context.mapper().createArrayNode();
        service.apsForSwitch(new Dpid(sw.getId())).stream()
                .map(SdwnAccessPoint::name)
                .forEachOrdered(aps::add);
        node.set("access_points", aps);

        Set<SdwnFrequencyBand> freqBands = new HashSet<>();
        service.apsForSwitch(new Dpid(sw.getId())).stream()
                .map(SdwnAccessPoint::nic)
                .map(SdwnNic::bands)
                .forEach(freqBands::addAll);

        node.set("frequency_bands", new FrequencyBandCodec().encode(freqBands, context));
        return node;
    }

    @Override
    public ArrayNode encode(Iterable<SdwnWirelessSwitch> switches, CodecContext context) {
        checkNotNull(switches);
        SdwnCoreService service = context.getService(SdwnCoreService.class);

        ArrayNode result = context.mapper().createArrayNode();
        switches.forEach(sw -> {
            ObjectNode node = result.addObject()
                    .put("datapath_id", sw.getStringId())
                    .put("channel_id", sw.channelId())
                    .put("manufacturer", sw.manufacturerDescription())
                    .put("hardware", sw.hardwareDescription())
                    .put("software", sw.softwareDescription())
                    .put("datapath_desc", sw.datapathDescription())
                    .put("serial_no", sw.serialNumber());

            if (sw.relatedOfSwitch() != null) {
                node.put("related_of_switch", sw.relatedOfSwitch().toString());
            }

            ArrayNode aps = context.mapper().createArrayNode();
            service.apsForSwitch(new Dpid(sw.getId())).stream()
                    .map(SdwnAccessPoint::name)
                    .forEachOrdered(aps::add);
            node.set("access_points", aps);

            Set<SdwnFrequencyBand> freqBands = new HashSet<>();
            service.apsForSwitch(new Dpid(sw.getId())).stream()
                    .map(SdwnAccessPoint::nic)
                    .map(SdwnNic::bands)
                    .forEach(freqBands::addAll);

            node.set("frequency_bands", new FrequencyBandCodec().encode(freqBands, context));
        });

        return result;
    }
}