package de.tuberlin.inet.sdwn.openwifi.cli;

import de.tuberlin.inet.sdwn.openwifi.api.OpenWifiIntegrationService;
import org.apache.karaf.shell.commands.Argument;
import org.apache.karaf.shell.commands.Command;
import org.onosproject.cli.AbstractShellCommand;

import java.net.MalformedURLException;

@Command(scope = "sdwn", name = "openwifi-unregister", description = "Unregister an SDWN controller from OpenWifi")
public class UnregisterCommand extends AbstractShellCommand {

    @Argument(index = 0, name = "URL", description = "OpenWifi server", valueToShowInHelp = "https://1.2.3.4:5678/path/to/openwifi/rest/api/root")
    private String url = null;

    @Argument(index = 1, name = "API_KEY", description = "OpenWifi API key")
    private String apiKey = null;

    @Argument(index = 2, name = "ID", description = "OpenWifi service ID")
    private String id = null;

    @Override
    protected void execute() {
        OpenWifiIntegrationService service = get(OpenWifiIntegrationService.class);
        try {

            boolean success;

            if (url == null) {
                success = service.unregister();
            } else if (id == null) {
                success = service.unregister(url);
            } else {
                success = service.unregister(url, apiKey, id);
            }

            if (!success) {
                print("ERROR. See log for details.");
            }
        } catch (MalformedURLException e) {
            print("ERROR. Not a valid url: %s", url);
        }
    }
}
