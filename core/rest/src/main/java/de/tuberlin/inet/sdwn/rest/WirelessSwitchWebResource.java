package de.tuberlin.inet.sdwn.rest;

import com.fasterxml.jackson.databind.node.ArrayNode;
import de.tuberlin.inet.sdwn.core.api.SdwnCoreService;
import org.onosproject.openflow.controller.Dpid;
import org.onosproject.openflow.controller.OpenFlowController;
import org.onosproject.openflow.controller.SdwnWirelessSwitch;
import org.onosproject.rest.AbstractWebResource;
import org.slf4j.Logger;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import java.util.ArrayList;
import java.util.List;

import static org.slf4j.LoggerFactory.getLogger;

@Path("switches")
public class WirelessSwitchWebResource extends AbstractWebResource {

    private final Logger log = getLogger(getClass());

    /**
     * Get a list of all connected switches.
     *
     * @return 200 OK
     * @onos.rsModel SdwnSwitchGet
     */
    @GET
    @Produces(MediaType.APPLICATION_JSON)
    public Response getSwitches() {
        OpenFlowController openFlowController = get(OpenFlowController.class);
        List<SdwnWirelessSwitch> switches = new ArrayList<>();
        get(SdwnCoreService.class).switches()
                .forEach(dpid -> switches.add((SdwnWirelessSwitch) openFlowController.getSwitch(dpid)));

        ArrayNode node = new WirelessSwitchCodec().encode(switches, this);
        return ok(node.toString()).build();
    }

    /**
     * Get information about a given switch.
     *
     * @return 200 OK
     * @param dpidStr Datapath ID
     * @onos.rsModel SdwnSwitchGet
     */
    @GET
    @Path("{dpid}")
    @Produces(MediaType.APPLICATION_JSON)
    public Response getSwitch(@PathParam("dpid") String dpidStr) {
        OpenFlowController controller = get(OpenFlowController.class);
        try {
            SdwnWirelessSwitch sw = (SdwnWirelessSwitch) controller.getSwitch(new Dpid(dpidStr));
            return ok(new WirelessSwitchCodec().encode(sw, this).toString()).build();
        } catch (ClassCastException e) {
            throw new IllegalArgumentException("Not a wireless switch.");
        }
    }

    /**
     * Get a list of access points hosted on a switch.
     *
     * @param dpidStr the Datapath ID of the switch
     * @return 200 OK
     */
    @GET
    @Path("{dpid}/aps")
    @Produces(MediaType.APPLICATION_JSON)
    public Response getApsForSwitch(@PathParam("dpid") String dpidStr) {
        ArrayNode result = new AccessPointCodec()
                .encode(get(SdwnCoreService.class).apsForSwitch(new Dpid(dpidStr)), this);
        return ok(result.toString()).build();
    }
}
