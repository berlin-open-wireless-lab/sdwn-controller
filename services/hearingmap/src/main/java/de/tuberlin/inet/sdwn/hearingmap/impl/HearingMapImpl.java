/*
 * Copyright 2017-present Open Networking Foundation
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package de.tuberlin.inet.sdwn.hearingmap.impl;

import com.google.common.base.Strings;
import com.google.common.collect.ImmutableMap;
import de.tuberlin.inet.sdwn.core.api.Sdwn80211MgmtFrameListener;
import de.tuberlin.inet.sdwn.core.api.SdwnCoreService;
import de.tuberlin.inet.sdwn.hearingmap.SdwnHearingMap;
import de.tuberlin.inet.sdwn.core.api.SdwnSwitchListenerAdapter;
import de.tuberlin.inet.sdwn.core.api.entity.SdwnAccessPoint;
import io.netty.util.Timeout;
import io.netty.util.TimerTask;
import org.apache.felix.scr.annotations.Activate;
import org.apache.felix.scr.annotations.Component;
import org.apache.felix.scr.annotations.Deactivate;
import org.apache.felix.scr.annotations.Modified;
import org.apache.felix.scr.annotations.Property;
import org.apache.felix.scr.annotations.Reference;
import org.apache.felix.scr.annotations.ReferenceCardinality;
import org.apache.felix.scr.annotations.Service;
import org.onlab.packet.MacAddress;
import org.onlab.util.Timer;
import org.onlab.util.Tools;
import org.onosproject.openflow.controller.Dpid;
import org.osgi.service.component.ComponentContext;
import org.slf4j.Logger;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

import static com.google.common.base.Preconditions.checkNotNull;
import static java.lang.System.currentTimeMillis;
import static org.slf4j.LoggerFactory.getLogger;

/**
 * Simple SDWN Client hearing map using the SDWN core's IEEE 802.11 frame listener
 * interface.
 */
@Component(immediate = true)
@Service
public class HearingMapImpl implements SdwnHearingMap {

    private static final String HEARING_MAP_TIMEOUT = "hearingMapTimeout";
    private static final long DEFAULT_HEARING_MAP_TIMEOUT = 30;

    @Property(name = HEARING_MAP_TIMEOUT, longValue = DEFAULT_HEARING_MAP_TIMEOUT,
            label = "Station hearing map timeout in seconds")
    private long hearingMapTimeout = DEFAULT_HEARING_MAP_TIMEOUT;

    private final Logger log = getLogger(getClass());

    private final long TIMEOUT;
    protected Timeout cleanupTask;

    protected Map<MacAddress, List<HearingMapEntry>> map = new ConcurrentHashMap<>();

    @Reference(cardinality = ReferenceCardinality.MANDATORY_UNARY)
    private SdwnCoreService sdwnService;

    private InternalFrameListener frameListener = new InternalFrameListener();
    private InternalSwitchListener switchListener = new InternalSwitchListener();

    public HearingMapImpl() {
        this(DEFAULT_HEARING_MAP_TIMEOUT);
    }

    public HearingMapImpl(long timeoutVal) {
        this.TIMEOUT = timeoutVal;
    }

    @Activate
    public void activate() {
        sdwnService.register80211MgtmFrameListener(frameListener, 0);
        sdwnService.registerSwitchListener(switchListener);
        log.info("Started");
    }

    @Deactivate
    public void deactivate() {
        sdwnService.removeSwitchListener(switchListener);
        sdwnService.remove80211MgmtFrameListener(frameListener);
        log.info("Stopped");
    }

    @Modified
    protected void modified(ComponentContext context) {
        Dictionary<?, ?> properties = context.getProperties();

        String updatedConfig = Tools.get(properties, HEARING_MAP_TIMEOUT);
        if (!Strings.isNullOrEmpty(updatedConfig)) {
            hearingMapTimeout = Long.valueOf(updatedConfig);
            log.info("Hearing Map entries will be deleted after {}s", updatedConfig);
        }
    }

    public void clientHeard(Dpid dpid, SdwnAccessPoint ap, MacAddress mac, long rssi, long frequency) {

        HearingMapEntry newEntry = SdwnHearingMap.newHearingMapEntry(dpid, ap, rssi, frequency);

        if (map.containsKey(mac)) {
            Collection<HearingMapEntry> entries = map.get(mac);
            if (entries.contains(newEntry)) {
                entries.remove(newEntry);
            }
            entries.add(newEntry);
        } else {
            log.info("{} heard at {}", mac, dpid);
            map.computeIfAbsent(mac, macAddress -> new ArrayList<>()).add(newEntry);
        }

        if (map.size() == 1) {
            cleanupTask = Timer.getTimer().newTimeout(new CleanUpTask(), TIMEOUT, TimeUnit.SECONDS);
        }
    }

    @Override
    public void removeSwitch(Dpid dpid) {
        HearingMapEntry dummy = SdwnHearingMap.newHearingMapEntry(dpid, null, 0, 0);
        List<MacAddress> toRemove = map.entrySet().stream()
                .filter(entry -> entry.getValue().contains(dummy))
                .map(Map.Entry::getKey)
                .collect(Collectors.toList());

        toRemove.forEach(map::remove);
    }

    @Override
    public Map<MacAddress, Collection<HearingMapEntry>> getState() {
        return ImmutableMap.copyOf(map);
    }

    @Override
    public SortedSet<HearingMapEntry> getApCandidates(MacAddress client) {
        checkNotNull(client);

        SortedSet<HearingMapEntry> candidates = new TreeSet<>();
        candidates.addAll(map.get(client));
        return candidates;
    }

    public void clear() {
        map.clear();
    }

    private final class InternalFrameListener implements Sdwn80211MgmtFrameListener {
        @Override
        public ResponseAction receivedProbeRequest(MacAddress clientMac, SdwnAccessPoint atAP, long xid, long rssi, long freq) {
            clientHeard(atAP.nic().switchID(), atAP, clientMac, rssi, freq);
            return ResponseAction.NONE;
        }

        @Override
        public ResponseAction receivedAuthRequest(MacAddress clientMac, SdwnAccessPoint atAP, long xid, long rssi, long freq) {
            clientHeard(atAP.nic().switchID(), atAP, clientMac, rssi, freq);
            return ResponseAction.NONE;
        }

        @Override
        public ResponseAction receivedAssocRequest(MacAddress clientMac, SdwnAccessPoint atAP, long xid, long rssi, long freq) {
            clientHeard(atAP.nic().switchID(), atAP, clientMac, rssi, freq);
            return ResponseAction.NONE;
        }
    }

    private final class InternalSwitchListener extends SdwnSwitchListenerAdapter {
        @Override
        public void switchDisconnected(Dpid dpid) {
            removeSwitch(dpid);
        }
    }

    private final class CleanUpTask implements TimerTask {

        @Override
        public void run(Timeout timeout) throws Exception {
            long now = currentTimeMillis();

            Map<MacAddress, List<HearingMapEntry>> toClean = new HashMap<>();
            map.forEach((macAddress, hearingMapEntries) -> {

                List<HearingMapEntry> timedOut = hearingMapEntries.stream()
                        .filter(entry -> now - entry.lastHeard() > TIMEOUT)
                        .collect(Collectors.toList());

                if (!timedOut.isEmpty()) {
                    toClean.put(macAddress, timedOut);
                }
            });

            List<MacAddress> toRemove = new ArrayList<>();

            toClean.forEach((macAddress, entries) -> {

                entries.forEach(entry -> {
                    log.debug("Client {} not heard for {}s at {}, removing entry",
                              macAddress, TIMEOUT, entry.switchId());

                    map.get(macAddress).remove(entry);
                });

                if (map.get(macAddress).isEmpty()) {
                    toRemove.add(macAddress);
                }
            });

            toRemove.forEach(map::remove);

            if (!map.isEmpty()) {
                cleanupTask = Timer.getTimer().newTimeout(new CleanUpTask(), TIMEOUT, TimeUnit.SECONDS);
            }
        }
    }
}
