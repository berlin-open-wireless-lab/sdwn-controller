package de.tuberlin.inet.sdwn.core.ctl;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import de.tuberlin.inet.sdwn.core.api.entity.SdwnAccessPoint;
import de.tuberlin.inet.sdwn.core.api.entity.SdwnClient;
import de.tuberlin.inet.sdwn.core.api.entity.SdwnNic;
import org.onlab.packet.MacAddress;
import org.onosproject.openflow.controller.Dpid;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

import org.slf4j.Logger;

import static org.slf4j.LoggerFactory.getLogger;

public class SdwnEntityStore {

    private static final Logger log = getLogger(SdwnEntityStore.class);

    private Map<Dpid, List<SdwnNic>> switchToNicsMap = new ConcurrentHashMap<>();
    private Map<SdwnAccessPoint, SdwnNic> apToNicMap = new ConcurrentHashMap<>();
    private Map<MacAddress, Set<SdwnAccessPoint>> apsByBssid = new ConcurrentHashMap<>();
    private Map<String, SdwnAccessPoint> apsByDpidAndName = new ConcurrentHashMap<>();
    private Map<Dpid, Dpid> relatedSwitchMap = new ConcurrentHashMap<>();
    private Map<MacAddress, SdwnClient> clients = new ConcurrentHashMap<>();

    public List<SdwnNic> nicsForSwitch(Dpid dpid) {
        List<SdwnNic> nics = this.switchToNicsMap.getOrDefault(dpid, null);
        if (nics == null) {
            return Collections.emptyList();
        } else {
            return ImmutableList.copyOf(nics);
        }
    }

    public void putNics(List<SdwnNic> nics) {
        if (nics == null || nics.isEmpty()) {
            return;
        }

        nics.forEach(nic -> {
            if (switchToNicsMap.containsKey(nic.switchID())) {
                switchToNicsMap.get(nic.switchID()).add(nic);
            } else {
                switchToNicsMap.put(nic.switchID(), Lists.newArrayList(nic));
            }

            log.info("Switch {} has NIC {}", nic.switchID(), nic.mac());
        });

    }

    public void putAp(SdwnAccessPoint ap, SdwnNic nic) {
        if (ap == null || nic == null) {
            return;
        }

        if (this.apToNicMap.containsKey(ap)) {
            this.apToNicMap.remove(ap);
        }
        this.apToNicMap.put(ap, nic);
        if (apsByBssid.containsKey(ap.bssid())) {
            apsByBssid.get(ap.bssid()).add(ap);
        } else {
            apsByBssid.put(ap.bssid(), Sets.newHashSet(ap));
        }
        this.apsByDpidAndName.put(String.format("%s-%s", nic.switchID(), ap.name()), ap);

        log.info("NIC {} is hosting AP {} (BSSID {})", nic.mac().toString(), ap.name(), ap.bssid());
    }

    public void removeAp(SdwnAccessPoint ap) {
        if (apsByBssid.containsKey(ap.bssid())) {
            apsByBssid.get(ap.bssid()).remove(ap);
            if (apsByBssid.get(ap.bssid()).isEmpty()) {
                apsByBssid.remove(ap.bssid());
            }
        }
        apsByDpidAndName.remove(String.format("%s-%s", ap.nic().switchID(), ap.name()));
        if (apToNicMap.containsKey(ap)) {
            apToNicMap.get(ap).removeAP(ap.name());
            apToNicMap.remove(ap);
        }
    }

    public SdwnAccessPoint apByDpidAndName(Dpid dpid, String name) {
        return apsByDpidAndName.get(String.format("%s-%s", dpid, name));
    }

    public Set<SdwnAccessPoint> apsByBssid(MacAddress bssid) {
        return apsByBssid.get(bssid);
    }

    public void removeSwitch(Dpid dpid) {
        switchToNicsMap.remove(dpid);
        relatedSwitchMap.remove(dpid);

        List<MacAddress> bssidsToRemove = Lists.newArrayList();

        apsByBssid.forEach((bssid, aps) -> {
            aps.removeIf(ap -> ap.nic().switchID().equals(dpid));
            if (apsByBssid.get(bssid).isEmpty()) {
                bssidsToRemove.add(bssid);
            }
        });

        bssidsToRemove.forEach(apsByBssid::remove);

        List<String> toRemove = apsByDpidAndName.keySet().stream()
                .filter(key -> key.split("-")[0].equals(dpid.toString()))
                .collect(Collectors.toList());

        toRemove.forEach(key -> apsByDpidAndName.remove(key));
    }

    public Set<MacAddress> bssids() {
        return apsByBssid.keySet();
    }

    public Set<Dpid> switches() {
        return switchToNicsMap.keySet();
    }

    public void putRelatedSwitch(Dpid swId, Dpid relatedSwId) {
        if (swId != null && relatedSwId != null) {
            relatedSwitchMap.put(swId, relatedSwId);
        }
    }

    public Dpid relatedSwitch(Dpid dpid) {
        return relatedSwitchMap.get(dpid);
    }

    public List<SdwnClient> clients() {
        return ImmutableList.copyOf(clients.values());
    }

    public SdwnClient getClient(MacAddress mac) {
        return clients.get(mac);
    }


    public void addClient(SdwnClient client, SdwnAccessPoint ap) {
        clients.put(client.macAddress(), client);
        ap.addClient(client);
    }

    public void removeClient(MacAddress mac) {
        SdwnClient client = clients.get(mac);
        if (client == null) {
            return;
        }

        clients.remove(mac);
    }
}
