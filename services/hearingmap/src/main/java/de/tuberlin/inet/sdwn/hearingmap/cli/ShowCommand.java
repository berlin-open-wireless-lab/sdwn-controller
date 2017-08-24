package de.tuberlin.inet.sdwn.hearingmap.cli;

import de.tuberlin.inet.sdwn.hearingmap.api.SdwnHearingMap;
import org.apache.karaf.shell.commands.Argument;
import org.apache.karaf.shell.commands.Command;
import org.onlab.packet.MacAddress;
import org.onosproject.cli.AbstractShellCommand;
import org.onosproject.openflow.controller.Dpid;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import static java.lang.System.currentTimeMillis;

@Command(scope = "sdwn", name = "show-hearingmap", description = "Dump the hearing map state.")
public class ShowCommand extends AbstractShellCommand {

    @Argument(index = 0, name = "DPID", description = "Filter hearing map entries by switch.")
    private String dpidStr = null;

    @Argument(index = 1, name = "AP", description = "Filter hearing map entries by AP.")
    private String apNameStr = null;

    @Override
    protected void execute() {
        if (dpidStr == null) {
            get(SdwnHearingMap.class).getState().forEach(this::printClient);
        } else {
            Dpid dpid = new Dpid(dpidStr);
            Map<MacAddress, Collection<SdwnHearingMap.HearingMapEntry>> filtered = new HashMap<>();
            get(SdwnHearingMap.class).getState().forEach((client, entries) -> entries.forEach(entry -> {
                if (entry.switchId().equals(dpid)) {
                    filtered.put(client, entries);
                }
            }));

            if (apNameStr == null) {
                filtered.forEach(this::printClient);
            } else {
                Map<MacAddress, Collection<SdwnHearingMap.HearingMapEntry>> furtherFiltered = new HashMap<>();
                filtered.forEach((client, entries) -> entries.forEach(entry -> {
                    if (entry.ap().name().equals(apNameStr)) {
                        furtherFiltered.put(client, entries);
                    }
                }));
                furtherFiltered.forEach(this::printClient);
            }
        }
    }

    private void printClient(MacAddress mac, Collection<SdwnHearingMap.HearingMapEntry> entries) {
        print("Client: %s", mac);
        entries.forEach(entry -> {
            print("  * seen at %s (BSSID: %s SSID: '%s') on %s", entry.ap().name(), entry.ap().bssid(), entry.ap().ssid(), entry.switchId());
            print("    - hz: %s", entry.frequency());
            print("    - RSSI: %s dB", entry.signalStrength());
            long seconds = (currentTimeMillis() - entry.lastHeard()) / 1000;
            print("    - %d second%s ago", seconds, seconds == 1 ? "" : "s");
        });
    }
}
