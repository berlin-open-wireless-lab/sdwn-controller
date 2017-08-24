package de.tuberlin.inet.sdwn.cli;

import de.tuberlin.inet.sdwn.core.api.SdwnCoreService;
import org.apache.karaf.shell.commands.Command;
import org.onosproject.cli.AbstractShellCommand;

@Command(scope = "sdwn", name = "clients", description = "List all clients")
public class ClientsCommand extends AbstractShellCommand {

    @Override
    protected void execute() {
        SdwnCoreService controller = get(SdwnCoreService.class);
        controller.clients().stream()
                .forEach(client ->
                                 print("* %s associated with %s [BSSID: %s, Interface: %d:%s]",
                                       client.macAddress(), client.ap().nic().switchID(),
                                       client.ap().bssid(), client.ap().portNumber(), client.ap().name()));
        print("\n");
    }
}
