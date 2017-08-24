package de.tuberlin.inet.sdwn.cli;

import de.tuberlin.inet.sdwn.core.api.SdwnCoreService;
import org.apache.karaf.shell.console.Completer;
import org.apache.karaf.shell.console.completer.StringsCompleter;
import org.onlab.packet.MacAddress;
import org.onosproject.cli.AbstractShellCommand;

import java.util.Iterator;
import java.util.List;
import java.util.SortedSet;

public class BssidCompleter implements Completer {

    @Override
    public int complete(String buf, int cursor, List<String> candidates) {
        StringsCompleter delegate = new StringsCompleter();
        SdwnCoreService controller = AbstractShellCommand.get(SdwnCoreService.class);
        Iterator<MacAddress> it = controller.bssids().iterator();
        SortedSet<String> strings = delegate.getStrings();

        while (it.hasNext()) {
            strings.add(it.next().toString());
        }

        return delegate.complete(buf, cursor, candidates);
    }
}
