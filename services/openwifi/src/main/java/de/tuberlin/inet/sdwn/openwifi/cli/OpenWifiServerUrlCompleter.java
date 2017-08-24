package de.tuberlin.inet.sdwn.openwifi.cli;

import de.tuberlin.inet.sdwn.openwifi.api.OpenWifiIntegrationService;
import org.apache.karaf.shell.console.Completer;
import org.apache.karaf.shell.console.completer.StringsCompleter;

import java.net.URL;
import java.util.List;
import java.util.SortedSet;
import java.util.stream.Collectors;

import static org.onosproject.cli.AbstractShellCommand.get;

public class OpenWifiServerUrlCompleter implements Completer {

    @Override
    public int complete(String buf, int cursor, List<String> candidates) {
        StringsCompleter completer = new StringsCompleter();
        SortedSet<String> cached = completer.getStrings();

        try {
            cached.addAll(get(OpenWifiIntegrationService.class).cachedAddresses().stream()
                                  .map(URL::toString)
                                  .collect(Collectors.toSet()));
        } catch (NullPointerException npe) {
            // do nothing
        }

        return completer.complete(buf, cursor, candidates);
    }
}
