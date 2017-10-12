package de.tuberlin.inet.sdwn.mobilitymanager.rest;

import de.tuberlin.inet.sdwn.core.api.SdwnCoreService;
import de.tuberlin.inet.sdwn.core.api.entity.SdwnAccessPoint;
import de.tuberlin.inet.sdwn.core.api.entity.SdwnClient;
import de.tuberlin.inet.sdwn.mobilitymanager.api.SdwnMobilityManager;
import jdk.nashorn.internal.ir.ObjectNode;
import org.onlab.packet.MacAddress;
import org.onosproject.openflow.controller.Dpid;
import org.onosproject.rest.AbstractWebResource;

import javax.ws.rs.Consumes;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.core.Response;

import static javax.ws.rs.core.MediaType.APPLICATION_JSON;

@Path("")
public class MobilityManagerWebResource extends AbstractWebResource {

    @POST
    @Path("{mac}/handover/{dpid}/{ap}")
    @Consumes(APPLICATION_JSON)
    public Response handover(@PathParam("mac") String macStr, @PathParam("dpid") String dpidStr, @PathParam("ap") String apName) {
        ObjectNode root;
        SdwnCoreService controller = get(SdwnCoreService.class);
        SdwnMobilityManager mobilityManager = get(SdwnMobilityManager.class);

        Dpid dpid = new Dpid(dpidStr);
        SdwnClient client = controller.getClient(MacAddress.valueOf(macStr));
        SdwnAccessPoint ap = controller.apByDpidAndName(dpid, apName);

        mobilityManager.handOver(client, ap);

        return Response.ok().build();
    }

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
