package de.tuberlin.inet.sdwn.core.ctl.task;

import de.tuberlin.inet.sdwn.core.api.Ieee80211Channels;
import de.tuberlin.inet.sdwn.core.api.SdwnCoreService;
import de.tuberlin.inet.sdwn.core.api.SdwnTransaction;
import de.tuberlin.inet.sdwn.core.api.SdwnTransactionStatus;
import de.tuberlin.inet.sdwn.core.api.entity.SdwnAccessPoint;
import de.tuberlin.inet.sdwn.core.api.entity.SdwnFrequency;
import de.tuberlin.inet.sdwn.core.api.entity.SdwnFrequencyBand;
import de.tuberlin.inet.sdwn.core.api.entity.SdwnNic;
import org.onosproject.openflow.controller.Dpid;
import org.onosproject.openflow.controller.SdwnWirelessSwitch;
import org.projectfloodlight.openflow.protocol.OFMessage;
import org.projectfloodlight.openflow.protocol.OFSdwnSetChannel;
import org.projectfloodlight.openflow.types.OFPort;
import org.slf4j.Logger;

import java.util.stream.Collectors;

import static de.tuberlin.inet.sdwn.core.api.SdwnTransactionStatus.DONE;
import static de.tuberlin.inet.sdwn.core.api.SdwnTransactionStatus.SKIP;
import static org.slf4j.LoggerFactory.getLogger;

public class SetChannelTransaction implements SdwnTransaction {

    private final long timeout;
    private final SdwnCoreService controller;
    private final Dpid dpid;
    private final SdwnAccessPoint ap;
    private final int frequency;
    private final int beaconCount;

    private final Logger log = getLogger(getClass());

    public SetChannelTransaction(Dpid dpid, SdwnAccessPoint ap, SdwnCoreService controller, int frequency, int beaconCount, long timeout) {
        this.timeout = timeout;
        this.controller = controller;
        this.dpid = dpid;
        this.ap = ap;
        this.beaconCount = beaconCount;
        this.frequency = frequency;
    }

    @Override
    public long timeout() {
        return timeout;
    }

    @Override
    public SdwnTransactionStatus update(Dpid dpid, OFMessage msg) {

        if (!dpid.equals(this.dpid) || !(msg instanceof OFSdwnSetChannel)) {
            return SKIP;
        }

        OFSdwnSetChannel notification = (OFSdwnSetChannel) msg;

        SdwnFrequency freq = null;
        for (SdwnFrequencyBand band : ap.nic().bands()) {
            if (band.containsFrequency(notification.getFrequency())) {
                freq = band.frequencies().stream()
                        .filter(f -> f.hz() == notification.getFrequency())
                        .findFirst().orElse(null);
                break;
            }
        }

        if (freq == null) {
            log.error("{} GHz operation not supported", ((double) frequency) / 1000.0);
            return DONE;
        }

        ap.setFrequency(freq);
        log.info("[{}]:{} is now operating at {} GHz (channel {})",
                dpid, ap.name(), String.format("%1.3f", (double) notification.getFrequency() / 1000.0),
                Ieee80211Channels.frequencyToChannel(notification.getFrequency()));
        return DONE;
    }

    @Override
    public void start(long xid) {
        SdwnWirelessSwitch sw = controller.getSwitch(dpid);
        if (sw == null) {
            log.error("{} is unknown", dpid);
            return;
        }

        if (!ap.nic().supportsFrequency(frequency)) {
            log.error("[{}]:{} does not support {} GHz", dpid, ap.name(), String.format("%1.3f", ((double) frequency) / 1000.0));
            return;
        }

        controller.sendMessage(dpid, sw.factory().buildSdwnSetChannel()
                .setBeaconCount((long) beaconCount)
                .setXid(xid)
                .setFrequency(frequency)
                .setIfNo(OFPort.of(ap.portNumber()))
                .build());
    }

    @Override
    public void timedOut() {

    }

    @Override
    public void aborted() {

    }

    @Override
    public void done() {

    }
}
