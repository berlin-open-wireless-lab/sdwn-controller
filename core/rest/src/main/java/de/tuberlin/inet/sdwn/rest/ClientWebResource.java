package de.tuberlin.inet.sdwn.rest;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ArrayNode;
import de.tuberlin.inet.sdwn.core.api.SdwnCoreService;
import de.tuberlin.inet.sdwn.core.api.entity.SdwnAccessPoint;
import de.tuberlin.inet.sdwn.core.api.entity.SdwnClient;
import org.onlab.packet.MacAddress;
import org.onosproject.openflow.controller.Dpid;
import org.onosproject.rest.AbstractWebResource;

import javax.ws.rs.Consumes;
import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

import static javax.ws.rs.core.Response.serverError;
import static org.slf4j.LoggerFactory.getLogger;

@Path("clients")
public class ClientWebResource extends AbstractWebResource {

    /**
     * Get a list of associated clients.
     *
     * @return 200 OK
     * @onos.rsModel SdwnClientGet
     */
    @GET
    @Produces(MediaType.APPLICATION_JSON)
    public Response getClients() {
        List<SdwnClient> clients = new ArrayList<>();
        get(SdwnCoreService.class).aps().forEach(ap -> clients.addAll(ap.clients()));
        ArrayNode result = new ClientCodec().encode(clients, this);
        return ok(result.toString()).build();
    }

    /**
     * Get information for the client with the given MAC address.
     *
     * @param macStr MAC address of the client
     * @onos.rsModel SdwnClientGet
     * @return 200 OK
     */
    @GET
    @Path("{mac}")
    @Produces(MediaType.APPLICATION_JSON)
    public Response getClient(@PathParam("mac") String macStr) {
        MacAddress mac = MacAddress.valueOf(macStr);

        SdwnClient client = get(SdwnCoreService.class).clients().stream()
                .filter(c -> c.macAddress().equals(mac))
                .findFirst()
                .orElse(null);

        if (client == null) {
            return ok(mapper().createArrayNode().toString()).build();
        }

        return ok(new ClientCodec().encode(client, this).toString()).build();
    }

    /**
     * Inject client state into an AP.
     *
     * @param stream JSON object containing a Datapath ID of the switch where the AP is hosted,
     *               the AP's name, and an array of client objects.
     * @return array of clients where association state injection was successfully initiated.
     * @onos.rsModel SdwnClientAdd
     */
    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public Response addClients(InputStream stream) {
        ArrayNode root;
        SdwnCoreService service = get(SdwnCoreService.class);
        try {
            JsonNode tree = mapper().readTree(stream);
            Dpid dpid = new Dpid(tree.get("datapath_id").asText());
            String apStr = tree.get("ap").asText();
            ArrayNode clientNodes = (ArrayNode) tree.get("clients");
            List<SdwnClient> clients = new ClientCodec().decode(clientNodes, this);

            SdwnAccessPoint ap = service.apsForSwitch(dpid).stream()
                    .filter(accessPoint -> accessPoint.name().equals(apStr))
                    .findFirst().orElse(null);

            if (ap == null) {
                throw new IllegalArgumentException(String.format("Error. Access Point %s unknown on switch %s or switch does not exist.", apStr, dpid));
            }

            root = mapper().createArrayNode();

            clients.forEach(client -> {
                if (service.addClientToAp(ap, client)) {
                    root.add(new ClientCodec().encode(client, this));
                }
            });
        } catch (IOException e) {
            throw new IllegalArgumentException(e.getMessage());
        }

        return ok(root.toString()).build();
    }

    /**
     * Kick a client from the AP it is associated with.
     *
     * @param macStr the client's MAC address
     * @return 200 OK
     */
    @DELETE
    @Path("{mac}")
    public Response delClient(@PathParam("mac") String macStr) {
        return delClient(MacAddress.valueOf(macStr), 0);
    }

    /**
     * Kick a client fro the AP it is associated with banning it for {@code banTime} seconds (?)
     *
     * @param macStr the client's MAC address
     * @param banTimeStr the ban time
     * @return 200 OK
     */
    @DELETE
    @Path("{mac}/{banTime}")
    public Response delClient(@PathParam("mac") String macStr, @PathParam("banTime") String banTimeStr) {
        return delClient(MacAddress.valueOf(macStr), Long.parseLong(banTimeStr));
    }

    private Response delClient(MacAddress mac, long banTime) {
        SdwnCoreService service = get(SdwnCoreService.class);

        if (service.removeClientFromAp(mac, banTime)) {
            return Response.ok().build();
        } else {
            return serverError().build();
        }
    }
}
