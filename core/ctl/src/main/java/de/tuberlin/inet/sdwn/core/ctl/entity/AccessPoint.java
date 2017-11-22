package de.tuberlin.inet.sdwn.core.ctl.entity;

import com.google.common.base.MoreObjects;
import com.google.common.collect.ImmutableList;

import de.tuberlin.inet.sdwn.core.api.entity.SdwnAccessPoint;
import de.tuberlin.inet.sdwn.core.api.entity.SdwnClient;
import de.tuberlin.inet.sdwn.core.api.entity.SdwnEntityParsingException;
import de.tuberlin.inet.sdwn.core.api.entity.SdwnFrequency;
import de.tuberlin.inet.sdwn.core.api.entity.SdwnFrequencyBand;
import de.tuberlin.inet.sdwn.core.api.entity.SdwnNic;
import org.onlab.packet.MacAddress;
import org.projectfloodlight.openflow.protocol.OFSdwnEntityAccesspoint;

import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import static com.google.common.base.Preconditions.checkNotNull;

public class AccessPoint implements SdwnAccessPoint {

    private final int portNumber;
    private final String name;
    private final MacAddress bssid;
    private String ssid;
    private final SdwnNic nic;
    private SdwnFrequency freq;
    private Set<SdwnClient> clients = new HashSet<>();
    private Set<MacAddress> blacklist = new HashSet<>();

    private AccessPoint(int no, String name, MacAddress bssid,
                        String ssid, SdwnNic nic,
                        SdwnFrequency freq, List<Client> clients) {
        portNumber = no;
        this.bssid = bssid;
        this.name = name;
        this.ssid = ssid;
        this.nic = nic;
        this.freq = freq;
        this.clients.addAll(clients);
    }

    public static AccessPoint fromOF(SdwnNic nic, OFSdwnEntityAccesspoint entity) throws SdwnEntityParsingException {
        StringBuilder sb = new StringBuilder();
        for (byte b : entity.getSsid()) {
            sb.append((char) b);
        }

        // look up currently used frequency from frequency band of NIC running the AP
        SdwnFrequency freq = null;

        for (SdwnFrequencyBand band : nic.bands()) {
            if (band.containsFrequency(entity.getCurrFreq())) {
                freq = band.frequencies().stream()
                        .filter(f -> f.hz() == entity.getCurrFreq())
                        .findFirst().orElse(null);
                break;
            }
        }

        if (freq == null) {
            throw new SdwnEntityParsingException(String.format("Failed to find frequency in NIC's frequency band: %d Hz", entity.getCurrFreq()), nic);
        }

        return new AccessPoint(entity.getIfNo().getPortNumber(),
                               entity.getName(),
                               MacAddress.valueOf(entity.getBssid().getBytes()),
                               sb.toString(), nic, freq,
                               Collections.emptyList());
    }

    @Override
    public Type type() {
        return Type.SDWN_ENTITY_ACCESSPOINT;
    }

    public int portNumber() {
        return portNumber;
    }

    public String name() {
        return name;
    }

    public SdwnFrequency frequency() {
        return freq;
    }

    public void setFrequency(SdwnFrequency freq) {
        this.freq = freq;
    }

    public MacAddress bssid() {
        return bssid;
    }

    public String ssid() {
        return ssid;
    }

    public SdwnNic nic() {
        return nic;
    }

    public List<SdwnClient> clients() {
        return ImmutableList.copyOf(clients);
    }

    @Override
    public void addClient(SdwnClient c) {
        if (c != null && !clients.contains(c)) {
            clients.add(c);
        }
    }

    @Override
    public void removeClient(SdwnClient c) {
        checkNotNull(c);
        clients.remove(c);
    }

    @Override
    public boolean clientIsAssociated(MacAddress clientMac) {
        return clients.stream()
                .map(SdwnClient::macAddress)
                .collect(Collectors.toList()).contains(clientMac);
    }

    @Override
    public void blacklistClient(MacAddress mac) {
        checkNotNull(mac);
        if (!blacklist.contains(mac)) {
            blacklist.add(mac);
        }
    }

    @Override
    public void clearClientBlacklisting(MacAddress mac) {
        checkNotNull(mac);
        blacklist.remove(mac);
    }

    @Override
    public boolean clientIsBlacklisted(MacAddress mac) {
        return blacklist.contains(mac);
    }

    @Override
    public String toString() {

        MoreObjects.ToStringHelper helper = MoreObjects.toStringHelper(this);
        return helper.add("Number", portNumber)
                .add("Name", name)
                .add("BSSID", bssid)
                .add("SSID", ssid)
                .toString();
    }

    @Override
    public boolean equals(Object obj) {
        if (!(obj instanceof SdwnAccessPoint)) {
            return false;
        }

        SdwnAccessPoint other = (SdwnAccessPoint) obj;

        return this.nic.equals(other.nic()) &&
                this.name.equals(other.name()) &&
                this.bssid().equals(other.bssid()) &&
                this.freq == other.frequency() &&
                this.portNumber == other.portNumber();
    }
}
