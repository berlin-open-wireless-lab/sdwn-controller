package de.tuberlin.inet.sdwn.cli;

import de.tuberlin.inet.sdwn.core.api.Sdwn80211MacEventCounter;
import org.apache.karaf.shell.commands.Argument;
import org.apache.karaf.shell.commands.Command;
import org.onosproject.cli.AbstractShellCommand;

@Command(scope = "sdwn", name = "80211-mac-events",
        description = "Display the number of received notification related to 802.11 MAC events.")
public class MacEventCount extends AbstractShellCommand {

    @Argument(name = "reset", description = "Reset counters")
    private String resetStr = null;

    @Override
    protected void execute() {
        Sdwn80211MacEventCounter counter = AbstractShellCommand.get(Sdwn80211MacEventCounter.class);

        print("Probe requests: %d\nAuthentication requests: %d\nAssociation requests: %d",
              counter.probesReceived(), counter.authRequestsReceived(), counter.assocRequestsReceived());

        if (resetStr != null) {
            print("Resetting counters");
            counter.reset();
        }
    }
}
