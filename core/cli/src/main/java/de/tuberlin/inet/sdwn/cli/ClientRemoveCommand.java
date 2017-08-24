package de.tuberlin.inet.sdwn.cli;

import de.tuberlin.inet.sdwn.core.api.SdwnCoreService;
import org.apache.karaf.shell.commands.Argument;
import org.apache.karaf.shell.commands.Command;
import org.onlab.packet.MacAddress;
import org.onosproject.cli.AbstractShellCommand;

@Command(scope = "sdwn", name = "client-remove", description = "Remove a client from its AP")
public class ClientRemoveCommand extends AbstractShellCommand {

    @Argument(name = "MAC", description = "MAC Address of client to be removed",
    required = true)
    private String staMacStr = null;

    @Argument(index = 1, name = "bantime",
            description = "Number of seconds to ban the deleted client from re-associating with the AP.")
    private String banTimeStr = null;

    @Override
    protected void execute() {
        try {
            MacAddress mac = MacAddress.valueOf(staMacStr);
            long banTime;
            if (banTimeStr == null) {
                banTime = 0;
            } else {
                banTime = Long.valueOf(banTimeStr);
            }

            if (get(SdwnCoreService.class).removeClientFromAp(mac, banTime)) {
                print("Deleting client %s", mac.toString());
            } else {
                print("Could not delete client %s", mac.toString());
            }
        } catch (IllegalArgumentException e) {
            print(e.getMessage());
        }
    }
}
