package de.tuberlin.inet.sdwn.cli;


import de.tuberlin.inet.sdwn.core.api.SdwnCoreService;
import de.tuberlin.inet.sdwn.core.api.entity.SdwnAccessPoint;
import org.apache.karaf.shell.commands.Argument;
import org.apache.karaf.shell.commands.Command;
import org.onlab.packet.MacAddress;
import org.onosproject.cli.AbstractShellCommand;
import org.onosproject.openflow.controller.Dpid;

@Command(scope = "sdwn", name = "client-handover",
         description = "Hand over a client to another AP")
public class HandoverCommand extends AbstractShellCommand {

    @Argument(index = 0, name = "CLIENT", description = "MAC address of the client",
              required = true)
    private String clientMacStr = null;

    @Argument(index = 1, name = "DPID", description = "Datapath ID of the switch hosting the destination AP",
              required = true)
    private String dpidStr = null;

    @Argument(index = 2, name = "AP", description = "The destination AP")
    private String apNameStr = null;

    @Override
    protected void execute() {
        SdwnCoreService service = get(SdwnCoreService.class);

        try {
            MacAddress clientMac = MacAddress.valueOf(clientMacStr);
            Dpid dpid = new Dpid(dpidStr);

            SdwnAccessPoint dstAp = service.apsForSwitch(dpid).stream()
                    .filter(ap -> ap.name().equals(apNameStr))
                    .findFirst().orElse(null);

            if (dstAp == null) {
                print("ERROR. Either the switch does not exist or it is not hosting %s", apNameStr);
                return;
            }

            if (service.handOver(clientMac, dstAp)) {
                print("Handing over %s to AP %s on %s", clientMacStr, dpidStr, dpid);
            } else {
                print("Failed to hand over %s to AP %s on %s. See log for details",
                      clientMacStr, dpidStr);
            }
        } catch (IllegalArgumentException e) {
            print(e.getMessage());
        }
    }
}
