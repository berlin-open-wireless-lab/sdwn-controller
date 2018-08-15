package de.tuberlin.inet.sdwn.rest;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import de.tuberlin.inet.sdwn.core.api.SdwnCoreService;
import de.tuberlin.inet.sdwn.core.api.entity.SdwnAccessPoint;
import org.onlab.packet.MacAddress;
import org.onosproject.openflow.controller.Dpid;
import org.onosproject.rest.AbstractWebResource;

import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import java.io.IOException;
import java.io.InputStream;
import java.util.stream.Collectors;

import static de.tuberlin.inet.sdwn.core.api.Ieee80211Channels.FrequencyBand.IEEE80211_BAND_2GHZ;
import static de.tuberlin.inet.sdwn.core.api.Ieee80211Channels.FrequencyBand.IEEE80211_BAND_5GHZ;
import static de.tuberlin.inet.sdwn.core.api.Ieee80211Channels.FrequencyBand.IEEE80211_BAND_60GHZ;
import static de.tuberlin.inet.sdwn.core.api.Ieee80211Channels.channelToFrequency;
import static de.tuberlin.inet.sdwn.core.api.Ieee80211Channels.frequencyToChannel;

@Path("aps")
public class AccessPointWebResource extends AbstractWebResource {

    /**
     * Get a list of all access points.
     *
     * @return 200 OK
     * @onos.rsModel SdwnApGet
     */
    @GET
    @Produces(MediaType.APPLICATION_JSON)
    public Response getApsByBssid() {
        ArrayNode result = new AccessPointCodec().encode(get(SdwnCoreService.class).aps(), this);
        return ok(result.toString()).build();
    }

    /**
     * Get a list of access points.
     *
     * @param bssid BSSID to filter access points by
     * @return 200 OK
     * @onos.rsModel SdwnApGet
     */
    @GET
    @Path("{bssid}")
    @Produces(MediaType.APPLICATION_JSON)
    public Response getApsByBssid(@PathParam("bssid") String bssid) {
        ArrayNode result = new AccessPointCodec()
                .encode(get(SdwnCoreService.class).aps().stream()
                                .filter(ap -> ap.bssid().equals(MacAddress.valueOf(bssid)))
                                .collect(Collectors.toList()), this);
        return ok(result.toString()).build();
    }

    /**
     * Get the operating hz and channel number for the given access point
     * on the given switch.
     *
     * @param dpidStr Datapath ID of the switch
     * @param apName name of the access point
     * @return {"channel": <channel>, "frequency" <frequency in Hz>}
     */
    @GET
    @Path("{dpid}/{ap}/channel")
    @Produces(MediaType.APPLICATION_JSON)
    public Response getChannel(@PathParam("dpid") String dpidStr, @PathParam("ap") String apName) {
        SdwnCoreService service = get(SdwnCoreService.class);
        SdwnAccessPoint ap = service.apsForSwitch(new Dpid(dpidStr)).stream()
                .filter(a -> a.name().equals(apName))
                .findFirst().orElse(null);
        if (ap == null) {
            return Response.status(Response.Status.NOT_FOUND).build();
        }

        ObjectNode response = mapper().createObjectNode()
                .put("channel", frequencyToChannel(ap.frequency().hz()))
                .put("frequency", ap.frequency().hz());
        return ok(response.toString()).build();
    }

    /**
     * Switch the operating channel of an AP after a given number of beacon frames.
     * This makes the AP send a channel switch anouncement to its clients prior to
     * switching the channel.
     *
     * @param stream JSON object
     * @return 200 OK
     * @onos.rsModel SdwnApChannelSet
     */
    @POST
    @Path("{dpid}/{ap}/channel")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public Response setChannel(@PathParam("dpid") String dpidStr, @PathParam("ap") String apName, InputStream stream) {
        SdwnCoreService service = get(SdwnCoreService.class);
        try {
            JsonNode tree = mapper().readTree(stream);
            Dpid dpid = new Dpid(dpidStr);
            String band = tree.get("band").asText();
            int channel = tree.get("channel").asInt();
            int freq;
            switch (band) {
                case "2GHz":
                    freq = channelToFrequency(channel, IEEE80211_BAND_2GHZ);
                    break;
                case "5GHz":
                    freq = channelToFrequency(channel, IEEE80211_BAND_5GHZ);
                    break;
                case "60GHz":
                    freq = channelToFrequency(channel, IEEE80211_BAND_60GHZ);
                    break;
                default:
                    throw new IllegalArgumentException("Unknown band. Use '2GHz', '5GHz' or '60GHz'.");
            }

            SdwnAccessPoint ap = service.apByDpidAndName(dpid, apName);
            if (ap == null) {
                throw new IllegalArgumentException(String.format("%s is not an AP on %s or the switch does not exist.", apName, dpid));
            }

            service.setChannel(dpid, ap.name(), freq, tree.get("beacon_count").asInt(1));

            return ok(mapper().createObjectNode().put("hz", freq).put("channel", channel)).build();
        } catch (IOException e) {
            throw new IllegalArgumentException(e.getMessage());
        } catch (NullPointerException e) {
            throw new IllegalArgumentException();
        }
    }
}
