package de.tuberlin.inet.sdwn.cli;

import de.tuberlin.inet.sdwn.core.api.Ieee80211Channels;
import de.tuberlin.inet.sdwn.core.api.SdwnCoreService;
import de.tuberlin.inet.sdwn.core.api.entity.SdwnAccessPoint;
import org.apache.karaf.shell.commands.Argument;
import org.apache.karaf.shell.commands.Command;
import org.onlab.packet.MacAddress;
import org.onosproject.cli.AbstractShellCommand;
import org.onosproject.openflow.controller.Dpid;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import static org.onosproject.openflow.controller.Dpid.dpid;
import static org.onosproject.openflow.controller.Dpid.uri;

@Command(scope = "sdwn", name = "aps", description = "Show information about access points.")
public class ApsCommand extends AbstractShellCommand {

    @Argument(index = 0, name = "DPID", description = "Datapath ID to filter APs")
    private String dpidStr = null;

    @Argument(index = 1, name = "BSSID", description = "BSSID to filter APs")
    private String bssidStr = null;

    @Argument(index = 2, name = "AP", description = "Name of AP")
    private String apName = null;

    @Override
    protected void execute() {
        SdwnCoreService controller = AbstractShellCommand.get(SdwnCoreService.class);
        Map<MacAddress, Set<SdwnAccessPoint>> map = new HashMap<>();

        final Dpid dpid = dpidStr == null ? null : dpid(uri(Long.parseLong(dpidStr.replaceAll(":", ""), 16)));
        final MacAddress bssid = bssidStr == null ? null : MacAddress.valueOf(bssidStr);

        if (dpidStr == null) {
            controller.aps().forEach(ap -> map.computeIfAbsent(ap.bssid(), b -> new HashSet<>()).add(ap));
        } else {
            if (bssidStr == null) {
                controller.apsForSwitch(dpid).forEach(ap -> map.computeIfAbsent(ap.bssid(), b -> new HashSet<>()).add(ap));
            } else {
                if (apName == null) {
                    controller.apsForSwitch(dpid).stream()
                            .filter(ap -> ap.bssid().equals(bssid))
                            .forEach(ap -> map.computeIfAbsent(ap.bssid(), b -> new HashSet<>()).add(ap));
                } else {
                    controller.apsForSwitch(dpid).stream()
                            .filter(ap -> ap.bssid().equals(bssid))
                            .filter(ap -> ap.name().equals(apName))
                            .forEach(ap -> map.computeIfAbsent(ap.bssid(), b -> new HashSet<>()).add(ap));
                }
            }
        }

        printMap(controller, map);
    }

    private void printMap(SdwnCoreService controller, Map<MacAddress, Set<SdwnAccessPoint>> map) {
        map.keySet().stream()
                .sorted((macAddress, otherMac) -> (int) (macAddress.toLong() - otherMac.toLong()))
                .forEach(bssid -> {
                    print("BSSID: %s", bssid);
                    map.get(bssid).forEach(ap -> {
                        print("* %s on switch %s", ap.name(), ap.nic().switchID());
                        print("\t- SSID: %s", ap.ssid());
                        print("\t- Frequency: %1.3fGhz (channel %d)", (double) ap.frequency().hz() / 1000.0,
                              Ieee80211Channels.frequencyToChannel((int) ap.frequency().hz()));
                        print("\t- Clients:");
                        ap.clients().forEach(sta -> print("\t\t- %s (Assoc ID %d)", sta.macAddress(), sta.assocId()));
                    });
                });
    }
}
