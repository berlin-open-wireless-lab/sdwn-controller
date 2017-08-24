package de.tuberlin.inet.sdwn.core.ctl.entity;

import com.google.common.base.MoreObjects;
import de.tuberlin.inet.sdwn.core.api.entity.SdwnEntityParsingException;
import de.tuberlin.inet.sdwn.core.api.entity.SdwnFrequencyBand;
import de.tuberlin.inet.sdwn.core.api.entity.SdwnAccessPoint;
import de.tuberlin.inet.sdwn.core.api.entity.SdwnNic;
import org.onlab.packet.MacAddress;
import org.onosproject.openflow.controller.Dpid;
import org.projectfloodlight.openflow.protocol.OFSdwnEntity;
import org.projectfloodlight.openflow.protocol.OFSdwnEntityAccesspoint;
import org.projectfloodlight.openflow.protocol.OFSdwnEntityBand;
import org.projectfloodlight.openflow.protocol.OFSdwnEntityNic;

import java.util.LinkedList;
import java.util.List;
import java.util.stream.Collectors;

public class Nic implements SdwnNic {

    private long index;
    private final Dpid switchId;
    private final MacAddress mac;
    private List<SdwnFrequencyBand> bands;
    private List<SdwnAccessPoint> aps;

    private Nic(long index, MacAddress mac, Dpid switchId) {
        this.switchId = switchId;
        this.index = index;
        this.mac = mac;
    }

    private Nic(long index, MacAddress mac, Dpid switchId, List<SdwnFrequencyBand> bands, List<SdwnAccessPoint> aps) {
        this(index, mac, switchId);
        this.bands = bands;
        this.aps = aps;
    }

    @Override
    public Type type() {
        return Type.SDWN_ENTITY_NIC;
    }

    public static Nic fromOF(Dpid switchId, OFSdwnEntityNic ofNic,
                             List<OFSdwnEntity> entities) {
        Nic nic = new Nic(
                ofNic.getIndex(),
                MacAddress.valueOf(ofNic.getMacAddr().getBytes()),
                switchId);

        List<SdwnFrequencyBand> bands = new LinkedList<>();
        entities.stream()
                .filter(OFSdwnEntityBand.class::isInstance)
                .map(OFSdwnEntityBand.class::cast)
                .filter(b -> b.getIndex() == ofNic.getIndex())
                .forEach(b ->  {
                    try {
                        bands.add(FrequencyBand.fromOF(ofNic.getIndex(), b, entities));
                    } catch (SdwnEntityParsingException e) {
                        // do not add band
                    }
                });

        nic.bands = bands;
        nic.aps = entities.stream()
                .filter(OFSdwnEntityAccesspoint.class::isInstance)
                .map(OFSdwnEntityAccesspoint.class::cast)
                .filter(ap -> ap.getPhyMac().equals(ofNic.getMacAddr()))
                .map(ap -> AccessPoint.fromOF(nic, ap))
                .collect(Collectors.toList());
        return nic;
    }

    @Override
    public long index() {
        return index;
    }

    @Override
    public Dpid switchID() {
        return switchId;
    }

    @Override
    public MacAddress mac() {
        return mac;
    }

    @Override
    public List<SdwnAccessPoint> aps() {
        return aps;
    }

    @Override
    public void addAP(SdwnAccessPoint ap) {
        this.aps.add(ap);
    }

    @Override
    public void removeAP(String name) {
        if (name != null) {
            aps.removeIf(ap -> ap.name().equals(name));
        }
    }

    @Override
    public List<SdwnFrequencyBand> bands() {
        return bands;
    }

    @Override
    public boolean supportsFrequency(int freq) {
        for (SdwnFrequencyBand band : bands) {
            if (band.containsFrequency(freq)) {
                return true;
            }
        }
        return false;
    }

    @Override
    public String toString() {
        MoreObjects.ToStringHelper helper = MoreObjects.toStringHelper(this);
        return helper.add("device index", index)
                .add("MAC address", mac)
                .add("Supported Bands", bands)
                .add("Access points", aps)
                .toString();
    }
}
