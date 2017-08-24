package de.tuberlin.inet.sdwn.core.ctl.entity;

import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.google.common.base.MoreObjects;
import de.tuberlin.inet.sdwn.core.api.Ieee80211Capability;
import de.tuberlin.inet.sdwn.core.api.entity.SdwnAccessPoint;
import de.tuberlin.inet.sdwn.core.api.entity.SdwnClient;
import de.tuberlin.inet.sdwn.core.api.entity.SdwnTransmissionRate;
import de.tuberlin.inet.sdwn.core.api.entity.SdwnClientCryptoKeys;
import org.onlab.packet.MacAddress;
import org.projectfloodlight.openflow.protocol.OFIeee80211HtCap;
import org.projectfloodlight.openflow.protocol.OFIeee80211VhtCap;
import org.projectfloodlight.openflow.protocol.OFSdwnAddClient;
import org.projectfloodlight.openflow.protocol.OFSdwnGetClientsReply;
import org.projectfloodlight.openflow.protocol.OFSdwnGetClientsReplyCrypto;
import org.projectfloodlight.openflow.protocol.OFSdwnGetClientsReplyLvap;
import org.projectfloodlight.openflow.protocol.OFSdwnGetClientsReplyNormal;

import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static com.google.common.collect.Lists.newArrayList;
import static org.projectfloodlight.openflow.protocol.OFIeee80211CapabilityFlags.CAPABILITY_FLAG_HT_CAP;
import static org.projectfloodlight.openflow.protocol.OFIeee80211CapabilityFlags.CAPABILITY_FLAG_VHT_CAP;

public class Client implements SdwnClient {

    private SdwnAccessPoint ap;
    private List<SdwnTransmissionRate> rates;
    private Set<Ieee80211Capability> cap;
    private OFIeee80211HtCap htCap;
    private OFIeee80211VhtCap vhtCap;
    private int assocId;
    private MacAddress mac;
    private SdwnClientCryptoKeys keys;

    private Client(SdwnAccessPoint ap, MacAddress mac, List<SdwnTransmissionRate> rates,
                   int cap, OFIeee80211HtCap htCap, OFIeee80211VhtCap vhtCap, int assocId,
                   SdwnClientCryptoKeys keys) {
        this.mac = mac;
        this.ap = ap;
        this.cap = Ieee80211Capability.fromInt(cap);
        this.rates = rates;
        this.htCap = htCap;
        this.vhtCap = vhtCap;
        this.assocId = assocId;
        this.keys = keys;
    }

    private Client(SdwnAccessPoint ap, MacAddress mac, List<SdwnTransmissionRate> rates,
                   Set<Ieee80211Capability> cap, OFIeee80211HtCap htCap, OFIeee80211VhtCap vhtCap, int assocId,
                   SdwnClientCryptoKeys keys) {
        this.mac = mac;
        this.ap = ap;
        this.cap = cap;
        this.rates = rates;
        this.htCap = htCap;
        this.vhtCap = vhtCap;
        this.assocId = assocId;
        this.keys = keys;
    }

    public static Client fromAddClient(SdwnAccessPoint ap, OFSdwnAddClient msg) {
        List<SdwnTransmissionRate> rates = newArrayList();
        OFIeee80211HtCap htCap;
        OFIeee80211VhtCap vhtCap;

        if (msg.getCapFlags().contains(CAPABILITY_FLAG_HT_CAP)) {
            htCap = msg.getHtCapabilities();
        } else {
            htCap = null;
        }

        if (msg.getCapFlags().contains(CAPABILITY_FLAG_VHT_CAP)) {
            vhtCap = msg.getVhtCapabilities();
        } else {
            vhtCap = null;
        }

        MacAddress mac = MacAddress.valueOf(msg.getClient().getBytes());

        for (byte b : msg.getSupportedRates().getBytes()) {
            rates.add(TransmissionRate.fromByte(b));
        }

        return new Client(ap, mac, rates, msg.getCapabilities(), htCap,
                          vhtCap, msg.getAssocId(), ClientCryptoKeys.fromOf(msg.getKeys()));
    }

    public static Client fromGetClientsReply(SdwnAccessPoint ap, OFSdwnGetClientsReply reply) {
        if (reply instanceof OFSdwnGetClientsReplyLvap) {
            return fromGetClientsReplyLvap(ap, (OFSdwnGetClientsReplyLvap) reply);
        } else if (reply instanceof OFSdwnGetClientsReplyNormal) {
            return fromGetClientsReplyNormal(ap, (OFSdwnGetClientsReplyNormal) reply);
        } else if (reply instanceof OFSdwnGetClientsReplyCrypto) {
            return fromGetClientsReplyCrypto(ap, (OFSdwnGetClientsReplyCrypto) reply);
        }
        return null;
    }

    private static Client fromGetClientsReplyLvap(SdwnAccessPoint ap, OFSdwnGetClientsReplyLvap msg) {
        return new Client(ap, MacAddress.valueOf(msg.getMac().getBytes()), null, 0, null, null, 0, null);
    }

    private static Client fromGetClientsReplyNormal(SdwnAccessPoint ap, OFSdwnGetClientsReplyNormal msg) {
        // TODO: parse rate string
        List<SdwnTransmissionRate> rates = Collections.emptyList();
        OFIeee80211HtCap htCap;
        OFIeee80211VhtCap vhtCap;

        if (msg.getCapFlags().contains(CAPABILITY_FLAG_HT_CAP)) {
            htCap = msg.getHtCapabilities();
        } else {
            htCap = null;
        }

        if (msg.getCapFlags().contains(CAPABILITY_FLAG_VHT_CAP)) {
            vhtCap = msg.getVhtCapabilities();
        } else {
            vhtCap = null;
        }

        MacAddress mac = MacAddress.valueOf(msg.getMac().getBytes());

        return new Client(ap, mac, rates, msg.getCapabilities(), htCap, vhtCap,
                          msg.getAssocId(), null);
    }

    private static Client fromGetClientsReplyCrypto(SdwnAccessPoint ap, OFSdwnGetClientsReplyCrypto msg) {
        // TODO: parse rate string
        List<SdwnTransmissionRate> rates = Collections.emptyList();
        OFIeee80211HtCap htCap;
        OFIeee80211VhtCap vhtCap;

        if (msg.getCapFlags().contains(CAPABILITY_FLAG_HT_CAP)) {
            htCap = msg.getHtCapabilities();
        } else {
            htCap = null;
        }

        if (msg.getCapFlags().contains(CAPABILITY_FLAG_VHT_CAP)) {
            vhtCap = msg.getVhtCapabilities();
        } else {
            vhtCap = null;
        }

        MacAddress mac = MacAddress.valueOf(msg.getMac().getBytes());
        return new Client(ap, mac, rates, msg.getCapabilities(), htCap,
                          vhtCap, msg.getAssocId(), ClientCryptoKeys.fromOf(msg.getKeys()));
    }

    @Override
    public Set<Ieee80211Capability> capabilities() {
        return cap;
    }

    @Override
    public MacAddress macAddress() {
        return mac;
    }

    @Override
    public List<SdwnTransmissionRate> rates() {
        return rates;
    }

    @Override
    public SdwnAccessPoint ap() {
        return ap;
    }

    @Override
    public int assocId() {
        return assocId;
    }

    @Override
    public OFIeee80211HtCap htCapabilities() {
        return htCap;
    }

    @Override
    public OFIeee80211VhtCap vhtCapabilities() {
        return vhtCap;
    }

    @Override
    public SdwnClientCryptoKeys keys() {
        return keys;
    }

    @Override
    public void setCapabilities(int capabilities) {
        cap = Ieee80211Capability.fromInt(capabilities);
    }

    @Override
    public void setCapabilities(Set<Ieee80211Capability> caps) {
        cap = caps;
    }

    @Override
    public void setHtCapabilities(OFIeee80211HtCap htCap) {
        this.htCap = htCap;
    }

    @Override
    public void setVhtCapabilities(OFIeee80211VhtCap vhtCap) {
        this.vhtCap = vhtCap;
    }

    @Override
    public void assoc(SdwnAccessPoint ap) {
        this.ap = ap;
    }

    @Override
    public void disassoc() {
        ap.removeClient(this);
        ap = null;
    }

    @Override
    public boolean equals(Object obj) {
        return (obj instanceof Client) && ((Client) obj).mac.equals(mac);
    }

    @Override
    public String toString() {
        MoreObjects.ToStringHelper helper = MoreObjects.toStringHelper(this);
        return helper.add("MAC address", mac)
                .add("Access Point", ap.bssid())
                .add("Assoc ID", assocId)
                .add("Capabilities", cap)
                .add("HT Capabilities", htCap)
                .add("VHT Capabilities", vhtCap)
                .add("Supported Rates", rates).toString();
    }

    /**
     * Hash based on MAC.
     */
    @Override
    public int hashCode() {
        return mac.hashCode();
    }

    public static Client fromJson(ObjectNode node) throws IllegalArgumentException {
        MacAddress mac = MacAddress.valueOf(node.get("mac").asText());
        List<SdwnTransmissionRate> rates = null;

        if (node.has("transmission_rates")) {
            rates = TransmissionRate.fromJson((ArrayNode) node.get("transmission_rates"));
        } else {
            rates = Collections.emptyList();
        }

        SdwnClientCryptoKeys keys = node.has("keys") ? ClientCryptoKeys.fromJsonObject((ObjectNode) node.get("keys")) : null;

        Set<Ieee80211Capability> capabilities = new HashSet<>();
        node.get("capabilities").forEach(cap -> capabilities.add(Ieee80211Capability.valueOf(cap.textValue())));

        return new Client(null,
                          mac,
                          rates,
                          capabilities,
                          null, null, // TODO: HT/VHT capabilities
                          node.has("assoc_id") ? node.get("assoc_id").asInt() : -1,
                          keys);
    }
}
