package de.tuberlin.inet.sdwn.openwifi.cli;

import de.tuberlin.inet.sdwn.openwifi.api.OpenWifiIntegrationService;
import org.apache.karaf.shell.commands.Argument;
import org.apache.karaf.shell.commands.Command;
import org.onlab.packet.IpAddress;
import org.onosproject.cli.AbstractShellCommand;

import java.net.MalformedURLException;

@Command(scope = "sdwn", name = "openwifi-register", description = "Register a given SDWN controller with OpenWifi.")
public class RegisterCommand extends AbstractShellCommand {

    @Argument(index = 0, name = "URL", required = true,
            description = "OpenWifi server address", valueToShowInHelp = "https://1.2.3.4:5678/path/to/openwifi/rest/api/root")
    private String url = null;

    @Argument(index = 1, name = "API_KEY", required = true, description = "OpenWifi API key")
    private String apiKey;

    @Argument(index = 2, name = "CTL-ADDR", required = true,
            description = "Address of SDWN controller's southbound interface",
            valueToShowInHelp = "1.2.3.4")
    private String controllerIpStr = null;

    @Argument(index = 3, name = "CTL-PORT", required = true,
            description = "Listening Port of SDWN controller's southbound interface")
    private String controllerPortStr = null;

    @Argument(index = 4, name = "CAPABILITY-NAME", required = true,
            description = "Name for this capability as shown in OpenWifi")
    private String capabilityName = null;

    @Argument(index = 5, name = "CAPABILITY-MATCH",
            description = "If the output of CAPABILITY_SCRIPT matches CAPABILITY_MATCH when executed on the switch, CAPABILITY_NAME is announced in OpenWifi.")
    private String capabilityMatch = null;

    @Argument(index = 6, name = "CAPABILITY-SCRIPT",
            description = "If the output of CAPABILITY_SCRIPT matches CAPABILITY_MATCH when executed on the switch, CAPABILITY_NAME is announced in OpenWifi.")
    private String capabilityScript = null;

    @Argument(index = 7, name = "UBUS-PATH", description = "Ubus path to use on the wireless switch")
    private String ubusPath = null;

    @Override
    protected void execute() {
        OpenWifiIntegrationService service = get(OpenWifiIntegrationService.class);

        try {
            boolean success;

            if (!(capabilityMatch != null && capabilityScript != null)) {
                success = service.register(url,
                                           apiKey,
                                           IpAddress.valueOf(controllerIpStr.equals("localhost") ? "127.0.0.1" : controllerIpStr),
                                           Integer.parseInt(controllerPortStr),
                                           capabilityName,
                                           capabilityMatch,
                                           capabilityScript,
                                           ubusPath);
            } else {
                success = service.register(url,
                                           apiKey,
                                           IpAddress.valueOf(controllerIpStr.equals("localhost") ? "127.0.0.1" : controllerIpStr),
                                           Integer.parseInt(controllerPortStr),
                                           capabilityName,
                                           ubusPath);
            }

            if (!success) {
                print("ERROR. See log for details.");
            }
        } catch (MalformedURLException e) {
            print("ERROR. Not a valid URL: %s", url);
        }
    }
}
