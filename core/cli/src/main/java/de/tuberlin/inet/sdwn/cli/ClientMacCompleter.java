package de.tuberlin.inet.sdwn.cli;


import de.tuberlin.inet.sdwn.core.api.SdwnCoreService;
import de.tuberlin.inet.sdwn.core.api.entity.SdwnClient;
import org.apache.karaf.shell.console.Completer;
import org.apache.karaf.shell.console.completer.StringsCompleter;
import org.onlab.packet.MacAddress;

import java.util.Iterator;
import java.util.List;
import java.util.SortedSet;

import static org.onosproject.cli.AbstractShellCommand.get;

public class ClientMacCompleter implements Completer {

    @Override
    public int complete(String buf, int cursor, List<String> candidates) {
        StringsCompleter delegate = new StringsCompleter();
        Iterator<MacAddress> it = get(SdwnCoreService.class).clients().stream()
                .map(SdwnClient::macAddress).iterator();
        SortedSet<String> strings = delegate.getStrings();

        while (it.hasNext()) {
            strings.add(it.next().toString());
        }

        return delegate.complete(buf, cursor, candidates);
    }
}
