package de.tuberlin.inet.sdwn.cli;

import de.tuberlin.inet.sdwn.core.api.SdwnCoreService;
import de.tuberlin.inet.sdwn.core.api.Ieee80211Channels;
import org.apache.karaf.shell.commands.Argument;
import org.apache.karaf.shell.commands.Command;
import org.onosproject.cli.AbstractShellCommand;
import org.onosproject.openflow.controller.Dpid;
import org.onosproject.openflow.controller.OpenFlowController;
import org.onosproject.openflow.controller.OpenFlowWirelessSwitch;

import java.util.Arrays;

@Command(scope = "sdwn", name = "switches",
        description = "Print information about the given switch (or all switches if none given).")
public class SwitchesCommand extends AbstractShellCommand {

    @Argument(name = "DPID", description = "Datapath ID")
    private String dpidStr = null;

    @Override
    protected void execute() {

        SdwnCoreService service = get(SdwnCoreService.class);
        OpenFlowController controller = get(OpenFlowController.class);

        if (service == null || controller == null) {
            return;
        }

        if (dpidStr == null) {
            service.switches().forEach(dpid -> printSwitch(service, controller, dpid));
        } else {
            printSwitch(service, controller, new Dpid(dpidStr));
        }
    }

    private void printSwitch(SdwnCoreService service, OpenFlowController controller, Dpid dpid) {
        OpenFlowWirelessSwitch sw = (OpenFlowWirelessSwitch) controller.getSwitch(dpid);
        StringBuilder sb = new StringBuilder();
        sb.append("*\t").append(sw.getStringId()).append("\n");
        sb.append("\t- Channel: ").append(sw.channelId()).append("\n");
        sb.append("\t- Manufacturer: ").append(sw.manufacturerDescription()).append("\n");
        sb.append("\t- Hardware: ").append(sw.hardwareDescription()).append("\n");
        sb.append("\t- Software: ").append(sw.softwareDescription()).append("\n");
        sb.append("\t- Datapath description: ").append(sw.datapathDescription()).append("\n");
        sb.append("\t- Serial No: ").append(sw.serialNumber()).append("\n");
        if (sw.relatedOfSwitch() != null) {
            sb.append("\t- Related OpenFlow switch: ").append(sw.relatedOfSwitch().toString()).append("\n");
        }
        sb.append("\t- Network Interface Cards:\n");
        sw.nicEntities().forEach(nic -> {
            sb.append("\t\t* ").append(nic.getMacAddr().toString()).append("\n");
            sb.append("\t\t- Access Points:\n");
            service.apsForSwitch(dpid).stream()
                    .filter(ap -> Arrays.equals(ap.nic().mac().toBytes(), nic.getMacAddr().getBytes()))
                    .forEach(ap -> {
                        sb.append("\t\t\t- Name: ").append(ap.name()).append("\n");
                        sb.append("\t\t\t- BSSID: ").append(ap.bssid().toString()).append("\n");
                        sb.append("\t\t\t- Number: ").append(ap.portNumber()).append("\n");
                        sb.append("\t\t\t- Frequency: ").append(ap.frequency().hz())
                                .append(" (Channel ").append(Ieee80211Channels.frequencyToChannel(ap.frequency().hz())).append(")\n");
                    });

        });

        print(sb.append("\n").toString());
    }
}
