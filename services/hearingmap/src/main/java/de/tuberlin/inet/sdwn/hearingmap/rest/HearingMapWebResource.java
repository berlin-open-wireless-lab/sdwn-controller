package de.tuberlin.inet.sdwn.hearingmap.rest;

import de.tuberlin.inet.sdwn.hearingmap.api.SdwnHearingMap;
import org.onlab.packet.MacAddress;
import org.onosproject.openflow.controller.Dpid;
import org.onosproject.rest.AbstractWebResource;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Path("")
public class HearingMapWebResource extends AbstractWebResource {

    /**
     * Return the hearing map state.
     *
     * @onos.rsModel SdwnHearingMapGet
     * @return 200 OK
     */
    @GET
    @Produces(MediaType.APPLICATION_JSON)
    public Response getHearingMap() {
        return ok(new HearingMapCodec().encode(get(SdwnHearingMap.class).getState(), this)).build();
    }

    /**
     * Return the hearing map state filtered by switch ID.
     *
     * @onos.rsModel SdwnHearingMapGet
     * @param dpidStr Data path ID of switch.
     * @return 200 OK
     */
    @GET
    @Path("{dpid}")
    @Produces(MediaType.APPLICATION_JSON)
    public Response getHearingMap(@PathParam("dpid") String dpidStr) {
        Dpid dpid = new Dpid(dpidStr);

        Map<MacAddress, Collection<SdwnHearingMap.HearingMapEntry>> map = get(SdwnHearingMap.class).getState();
        Map<MacAddress, Collection<SdwnHearingMap.HearingMapEntry>> filtered = new HashMap<>();

        map.forEach((macAddress, entries) -> {
            List<SdwnHearingMap.HearingMapEntry> filteredEntries = entries.stream()
                    .filter(entry -> entry.switchId().equals(dpid))
                    .collect(Collectors.toList());

            if (!filteredEntries.isEmpty()) {
                filtered.put(macAddress, filteredEntries);
            }
        });

        return ok(new HearingMapCodec().encode(filtered, this)).build();
    }
}
