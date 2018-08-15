package de.tuberlin.inet.sdwn.hearingmap.cli;

import de.tuberlin.inet.sdwn.hearingmap.SdwnHearingMap;
import org.apache.karaf.shell.commands.Command;
import org.onosproject.cli.AbstractShellCommand;

@Command(scope = "sdwn", name = "clear-hearingmap", description = "Clear the hearing map.")
public class ClearCommand extends AbstractShellCommand {

    @Override
    protected void execute() {
        get(SdwnHearingMap.class).clear();
    }
}
