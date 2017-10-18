package de.tuberlin.inet.sdwn.mobilitymanager.rest;

import de.tuberlin.inet.sdwn.core.api.SdwnCoreService;
import de.tuberlin.inet.sdwn.core.api.entity.SdwnAccessPoint;
import de.tuberlin.inet.sdwn.core.api.entity.SdwnClient;
import de.tuberlin.inet.sdwn.mobilitymanager.SdwnMobilityManager;
import org.onlab.packet.MacAddress;
import org.onosproject.openflow.controller.Dpid;
import org.onosproject.rest.AbstractWebResource;

import javax.ws.rs.*;
import javax.ws.rs.core.Response;

import static javax.ws.rs.core.MediaType.APPLICATION_JSON;

@Path("clients")
public class MobilityManagerWebResource extends AbstractWebResource {

//    @GET
//    @Path("{mac}")
//    @Produces(APPLICATION_JSON)
//    public Response getPossibleHandoverDestinations(@PathParam("mac") String macStr) {
//        ArrayNode array = mapper().createArrayNode();
//        SdwnHearingMap map = get(SdwnHearingMap.class);
//
//        Set<SdwnHearingMap.HearingMapEntry> dsts = map.getApCandidates(MacAddress.valueOf(macStr));
//        dsts.forEach(entry ->
//            array.addObject()
//                    .put("dpid", entry.ap().nic().switchID().toString())
//                    .put("ap", entry.ap().name())
//                    .put("rssi", entry.signalStrength())
//                    .put("ms_since", currentTimeMillis() - entry.lastHeard()));
//
//        return ok(array).build();
//    }

    /**
     * Trigger the handover of a wireless client to another AP.
     *
     * @onos.rsModel SdwnMobilityManagerHandover
     * @param macStr the client's MAC address
     * @param dpidStr Datapath ID of the switch hosting the destination AP
     * @param apName Name of the destination AP
     * @return 200 OK
     */
    @POST
    @Path("{mac}/handover/{dpid}/{ap}")
    @Consumes(APPLICATION_JSON)
    public Response handover(@PathParam("mac") String macStr, @PathParam("dpid") String dpidStr, @PathParam("ap") String apName) {
        SdwnCoreService controller = get(SdwnCoreService.class);
        SdwnMobilityManager mobilityManager = get(SdwnMobilityManager.class);

        Dpid dpid = new Dpid(dpidStr);
        SdwnClient client = controller.getClient(MacAddress.valueOf(macStr));
        SdwnAccessPoint ap = controller.apByDpidAndName(dpid, apName);

        mobilityManager.handOver(client, ap);

        return Response.ok().build();
    }

    /**
     * Trigger the handover of a wireless client to another AP.
     *
     * @onos.rsModel SdwnMobilityManagerHandover
     * @param macStr the client\s MAC address
     * @param dpidStr Datpath ID of the switch hosting the destination AP
     * @param apName Name of the destination AP
     * @param timeoutStr timeout for the handover in ms
     * @return 200 OK
     */
    @POST
    @Path("{mac}/handover/{dpid}/{ap}/{timeout}")
    @Consumes(APPLICATION_JSON)
    public Response handover(@PathParam("mac") String macStr, @PathParam("dpid") String dpidStr, @PathParam("ap") String apName, @PathParam("timeout") String timeoutStr) {
        SdwnCoreService controller = get(SdwnCoreService.class);
        SdwnMobilityManager mobilityManager = get(SdwnMobilityManager.class);

        Dpid dpid = new Dpid(dpidStr);
        SdwnClient client = controller.getClient(MacAddress.valueOf(macStr));
        SdwnAccessPoint ap = controller.apByDpidAndName(dpid, apName);

        long timeout = Long.parseLong(timeoutStr);

        mobilityManager.handOver(client, ap, timeout);

        return Response.ok().build();
    }
}
