package de.tuberlin.inet.sdwn.mobilitymanager.cli;

import de.tuberlin.inet.sdwn.core.api.SdwnCoreService;
import de.tuberlin.inet.sdwn.core.api.entity.SdwnAccessPoint;
import de.tuberlin.inet.sdwn.core.api.entity.SdwnClient;
import de.tuberlin.inet.sdwn.mobilitymanager.api.SdwnMobilityManager;
import org.apache.karaf.shell.commands.Argument;
import org.apache.karaf.shell.commands.Command;
import org.onlab.packet.MacAddress;
import org.onosproject.cli.AbstractShellCommand;
import org.onosproject.openflow.controller.Dpid;

@Command(scope = "sdwn", name = "handover", description = "Trigger the handover of a given client to another AP")
public class HandoverCommand extends AbstractShellCommand {


    @Argument(index = 0, name = "CLIENT", description = "the client's MAC address", required = true)
    private String clientStr = null;

    @Argument(index = 1, name = "DPID", description = "datapath ID of the switch hosting the destination AP.", required = true)
    private String dpidStr = null;

    @Argument(index = 2, name = "AP", description = "Name of the destination AP", required = true)
    private String apStr = null;

    @Argument(index = 3, name = "TIMEOUT", description = "Handover timeout in milliseconds")
    private String timeoutStr = null;

    @Override
    protected void execute() {
        SdwnCoreService controller = get(SdwnCoreService.class);
        SdwnMobilityManager mobilityManager = get(SdwnMobilityManager.class);

        SdwnClient client = controller.getClient(MacAddress.valueOf(clientStr));
        if (client == null) {
            print("No such client: %s", clientStr);
            return;
        }

        Dpid dpid = new Dpid(dpidStr);
        SdwnAccessPoint ap = controller.apByDpidAndName(dpid, apStr);
        if (ap == null) {
            print("No such AP: %s:%s", dpidStr, apStr);
            return;
        }

        long timeout;
        if (timeoutStr != null) {
            try {
                timeout = Long.parseLong(timeoutStr);
            } catch (NumberFormatException e) {
                print("Not a valid timeout: %s", timeoutStr);
                return;
            }
            mobilityManager.handOver(client, ap, timeout);
        } else {
            mobilityManager.handOver(client, ap);
        }
    }
}
