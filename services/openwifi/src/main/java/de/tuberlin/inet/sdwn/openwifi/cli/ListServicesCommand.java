package de.tuberlin.inet.sdwn.openwifi.cli;

import de.tuberlin.inet.sdwn.openwifi.api.OpenWifiIntegrationService;
import de.tuberlin.inet.sdwn.openwifi.impl.OpenWifiServiceEntity;
import org.apache.karaf.shell.commands.Argument;
import org.apache.karaf.shell.commands.Command;
import org.onosproject.cli.AbstractShellCommand;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.Map;


@Command(scope = "sdwn", name = "openwifi-services", description = "List registered OpenWifi services for one or multiple OpenWifi servers.",
        detailedDescription = "If a URL to the root of an OpenWifi REST API is provided, only that server will be queried. If no URL is provided, " +
                "all known OpenWifi servers will be queried.")
public class ListServicesCommand extends AbstractShellCommand {

    @Argument(name = "URL", description = "OpenWifi server URL", valueToShowInHelp = "1.2.3.4:80/path/to/openwifi/rest/api/root")
    private String url = null;

    @Override
    protected void execute() {
        OpenWifiIntegrationService service = get(OpenWifiIntegrationService.class);

        if (this.url == null) {
            Map<URL, Map<String, OpenWifiServiceEntity>> services = service.listServices();

            if (services == null) {
                print("ERROR. See log for details.");
                return;
            }

            services.forEach((url, serviceMap) -> {
                print("%s:", url);
                printServices(serviceMap);
                print("\n");
            });
        } else {
            try {
                Map<String, OpenWifiServiceEntity> services = service.listServices(url);
                if (services == null) {
                    print("ERROR. See log for details.");
                    return;
                }
                printServices(services);
            } catch (MalformedURLException e) {
                print("ERROR. Not a valid URL: %s", url);
            }
        }

    }

    private void printServices(Map<String, OpenWifiServiceEntity> map) {
        map.forEach((id, service) -> print("{%s: %s}", id, service.toString()));
    }
}
