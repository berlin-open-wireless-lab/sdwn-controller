package de.tuberlin.inet.sdwn.mobilitymanager.impl;

import com.google.common.base.Strings;
import de.tuberlin.inet.sdwn.core.api.*;
import de.tuberlin.inet.sdwn.core.api.entity.SdwnAccessPoint;
import de.tuberlin.inet.sdwn.core.api.entity.SdwnClient;
import de.tuberlin.inet.sdwn.mobilitymanager.api.SdwnMobilityManager;
import org.apache.felix.scr.annotations.*;
import org.onlab.packet.MacAddress;
import org.onlab.util.Tools;
import org.onosproject.openflow.controller.Dpid;
import org.osgi.service.component.ComponentContext;
import org.slf4j.Logger;

import java.util.Dictionary;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import static com.google.common.base.Preconditions.checkNotNull;
import static org.slf4j.LoggerFactory.getLogger;

/*
 * TODO    - filter dst by RSSI from hearing map
 * TODO    - detect weak links and automatically trigger handover when a better AP is available
 */
@Component(immediate = true)
@Service
public class MobilityManager implements SdwnMobilityManager {

    private final Logger log = getLogger(getClass());

    @Reference(cardinality = ReferenceCardinality.MANDATORY_UNARY)
    private SdwnCoreService controller;

    private InternalMgmtFrameListener mgmtFrameListener = new InternalMgmtFrameListener();

    private static final String MOBILITY_MANAGER_80211_MGMT_PRIORITY = "mobilityManagerMgmtFramePrio";
    private static final int DEFAULT_MOBILITY_MANAGER_80211_MGMT_PRIORITY = 99;

    @Property(name = MOBILITY_MANAGER_80211_MGMT_PRIORITY, intValue = DEFAULT_MOBILITY_MANAGER_80211_MGMT_PRIORITY,
            label = "Priority for handling 802.11 managment frames")
    private int mobilityManagerMgmtFramePrio = DEFAULT_MOBILITY_MANAGER_80211_MGMT_PRIORITY;

    private static final String MOBILITY_MANAGER_HANDOVER_TIMEOUT = "mobilityManagerHandoverTimeout";
    private static final int DEFAULT_MOBILITY_MANAGER_HANDOVER_TIMEOUT = 5000;

    @Property(name = MOBILITY_MANAGER_HANDOVER_TIMEOUT, longValue = DEFAULT_MOBILITY_MANAGER_HANDOVER_TIMEOUT,
            label = "Handover transaction timeout in milliseconds")
    private long mobilityManagerHandoverTimeout = DEFAULT_MOBILITY_MANAGER_HANDOVER_TIMEOUT;

    private InternalClientListener clientListener = new InternalClientListener();
    private InternalSwitchListener switchListener = new InternalSwitchListener();
    private Map<MacAddress, HandoverTransactionContext> ongoingHandovers = new ConcurrentHashMap<>();

    @Activate
    public void activate() {
        controller.registerClientListener(clientListener);
        controller.registerSwitchListener(switchListener);
        controller.register80211MgtmFrameListener(mgmtFrameListener, mobilityManagerMgmtFramePrio);
        log.info("Started");
    }

    @Deactivate
    public void deactivate() {
        controller.removeClientListener(clientListener);
        controller.removeSwitchListener(switchListener);
        controller.remove80211MgmtFrameListener(mgmtFrameListener);
        log.info("Stopped");
    }

    @Modified
    public void modified(ComponentContext context) {
        Dictionary<?, ?> properties = context.getProperties();

        String updatedConfig = Tools.get(properties, MOBILITY_MANAGER_80211_MGMT_PRIORITY);
        if (!Strings.isNullOrEmpty(updatedConfig)) {
            mobilityManagerMgmtFramePrio = Integer.valueOf(updatedConfig);
            controller.remove80211MgmtFrameListener(mgmtFrameListener);
            controller.register80211MgtmFrameListener(mgmtFrameListener, mobilityManagerMgmtFramePrio);
            log.info("Mobility Manager priority for handling 802.11 management frames set to {}", updatedConfig);
        }
        updatedConfig = Tools.get(properties, MOBILITY_MANAGER_HANDOVER_TIMEOUT);
        if (!Strings.isNullOrEmpty(updatedConfig)) {
            mobilityManagerHandoverTimeout = Long.valueOf(updatedConfig);
            log.info("Default handover timeout set to {} ms", updatedConfig);
        }
    }

    @Override
    public void handOver(SdwnClient c, SdwnAccessPoint dst) {
        startHandover(c, dst, mobilityManagerHandoverTimeout);
    }

    @Override
    public void handOver(SdwnClient c, SdwnAccessPoint dst, long timeout) {
        startHandover(c, dst, timeout);
    }

    private void startHandover(SdwnClient c, SdwnAccessPoint dst, long timeout) {
        checkNotNull(c);
        checkNotNull(dst);

        if (c.ap().equals(dst)) {
            return;
        }

        HandoverTransactionContext handover = new HandoverTransactionContext(timeout, dst, c);
        controller.startTransaction(handover);
        ongoingHandovers.put(c.macAddress(), handover);
    }


    private final class InternalClientListener implements SdwnClientListener {

        @Override
        public void clientAssociated(SdwnClient c) {
            if (ongoingHandovers.containsKey(c.macAddress())) {
                if (ongoingHandovers.get(c.macAddress()).dst().equals(c.ap())) {
                    log.info("Handover finished. {} is now associated with {}:{}", c.macAddress(), c.ap().nic().switchID(), c.ap().name());
                }
            }
        }

        @Override
        public void clientDisassociated(SdwnClient c, SdwnAccessPoint fromAp) {
            if (ongoingHandovers.containsKey(c.macAddress())) {
                if (ongoingHandovers.get(c.macAddress()).dst().equals(fromAp)) {
                    log.info("Handover started. {} disassociated from {}:{}", c.macAddress(), fromAp.nic().switchID(), fromAp.name());
                }
            }
        }
    }

    private final class InternalSwitchListener implements SdwnSwitchListener {

        @Override
        public void switchConnected(Dpid dpid) {

        }

        @Override
        public void switchDisconnected(Dpid dpid) {
            ongoingHandovers.forEach((mac, ctx) -> {
                if (ctx.dst().nic().switchID().equals(dpid)) {
                    ongoingHandovers.remove(mac);
                    log.error("Handover failed: switch disconnected. Client: {} Switch: {}", mac, dpid);
                }
            });
        }
    }


    private final class InternalMgmtFrameListener extends Sdwn80211MgmtFrameListenerAdapter {

        @Override
        public ResponseAction receivedAuthRequest(org.onlab.packet.MacAddress clientMac, SdwnAccessPoint atAP, long xid, long rssi, long freq) {
            if (ongoingHandovers.containsKey(clientMac)) {
                HandoverTransactionContext ctx = ongoingHandovers.get(clientMac);

                if (ctx.client().ap().equals(atAP)) {
                    return ResponseAction.GRANT;
                } else {
                    return ResponseAction.DENY;
                }
            }

            return ResponseAction.NONE;
        }

        @Override
        public ResponseAction receivedAssocRequest(org.onlab.packet.MacAddress clientMac, SdwnAccessPoint atAP, long xid, long rssi, long freq) {
            if (ongoingHandovers.containsKey(clientMac)) {
                HandoverTransactionContext ctx = ongoingHandovers.get(clientMac);

                if (ctx.client().ap().equals(atAP)) {
                    return ResponseAction.GRANT;
                } else {
                    return ResponseAction.DENY;
                }
            }

            return ResponseAction.NONE;
        }
    }
}
