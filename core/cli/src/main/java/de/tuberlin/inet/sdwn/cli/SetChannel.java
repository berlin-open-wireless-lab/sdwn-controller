package de.tuberlin.inet.sdwn.cli;

import de.tuberlin.inet.sdwn.core.api.Ieee80211Channels;
import de.tuberlin.inet.sdwn.core.api.SdwnCoreService;
import de.tuberlin.inet.sdwn.core.api.entity.SdwnAccessPoint;
import org.apache.karaf.shell.commands.Argument;
import org.apache.karaf.shell.commands.Command;
import org.onosproject.cli.AbstractShellCommand;
import org.onosproject.openflow.controller.Dpid;

import static de.tuberlin.inet.sdwn.core.api.Ieee80211Channels.FrequencyBand.IEEE80211_BAND_2GHZ;
import static de.tuberlin.inet.sdwn.core.api.Ieee80211Channels.FrequencyBand.IEEE80211_BAND_5GHZ;
import static de.tuberlin.inet.sdwn.core.api.Ieee80211Channels.FrequencyBand.IEEE80211_BAND_60GHZ;

@Command(scope = "sdwn", name = "set-channel",
        description = "Set the channel for a given AP. The AP sends a Channel Switch Announcement and then switches the channel after the given amount of beacons have been sent.")
public class SetChannel extends AbstractShellCommand {

    @Argument(index = 0, name = "DPID", required = true, description = "Datapath ID of the switch hosting the AP.")
    private String switchDpidStr = null;

    @Argument(index = 1, name = "AP", required = true, description = "Name of the AP.")
    private String apStr = null;

    @Argument(index = 2, name = "BAND", required = true, description = "Frequency band of the channel. Needed to differentiate overlapping channel numbers in 2.4 and 5 Ghz bands.")
    private String freqBandStr = null;

    @Argument(index = 3, name = "CHANNEL", required = true, description = "Channel number.")
    private String chanStr = null;

    @Argument(index = 4, name = "BEACON_COUNT",
            description = "Number of beacon frames to transmit before channel is switched")
    private String beaconCountStr = null;

    @Override
    protected void execute() {
        SdwnCoreService controller = get(SdwnCoreService.class);

        int freq, beaconCount;
        Dpid dpid = new Dpid(switchDpidStr);
        SdwnAccessPoint accessPoint = controller.aps().stream()
                .filter(ap -> ap.nic().switchID().equals(dpid))
                .filter(ap -> ap.name().equals(apStr))
                .findFirst().orElse(null);

        if (accessPoint == null) {
            print("AP not found");
            return;
        }

        switch (freqBandStr) {
            case "2GHz":
                freq = Ieee80211Channels.channelToFrequency(
                        Integer.parseInt(chanStr), IEEE80211_BAND_2GHZ);
                break;
            case "5GHz":
                freq = Ieee80211Channels.channelToFrequency(
                        Integer.parseInt(chanStr), IEEE80211_BAND_5GHZ);
                break;
            case "60GHz":
                freq = Ieee80211Channels.channelToFrequency(
                        Integer.parseInt(chanStr), IEEE80211_BAND_60GHZ);
                break;
            default:
                print("Please provide hz band: '2GHz', '5GHz' or '60GHz'.");
                return;
        }

        try {
            beaconCount = beaconCountStr == null ? 1 : Integer.parseInt(beaconCountStr);
        } catch (NumberFormatException e) {
            print("Invalid beacon count: %d.", beaconCountStr);
            return;
        }

        if (!controller.setChannel(dpid, accessPoint.portNumber(), freq, beaconCount))
            print("An error occurred. See log for details.");
    }
}
