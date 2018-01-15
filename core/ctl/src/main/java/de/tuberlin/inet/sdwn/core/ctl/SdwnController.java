/*
 * Copyright 2017-present Open Networking Laboratory
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
package de.tuberlin.inet.sdwn.core.ctl;

import com.fasterxml.jackson.databind.node.ObjectNode;
import com.google.common.base.Strings;
import com.google.common.collect.ImmutableSet;
import de.tuberlin.inet.sdwn.core.api.*;
import de.tuberlin.inet.sdwn.core.api.entity.*;
import de.tuberlin.inet.sdwn.core.ctl.entity.Client;
import de.tuberlin.inet.sdwn.core.ctl.entity.ClientCryptoKeys;
import de.tuberlin.inet.sdwn.core.ctl.entity.Nic;
import de.tuberlin.inet.sdwn.core.ctl.task.AddClientTransaction;
import de.tuberlin.inet.sdwn.core.ctl.task.DelClientTransaction;
import de.tuberlin.inet.sdwn.core.ctl.task.GetClientsQuery;
import io.netty.util.internal.ConcurrentSet;
import org.apache.felix.scr.annotations.Activate;
import org.apache.felix.scr.annotations.Component;
import org.apache.felix.scr.annotations.Deactivate;
import org.apache.felix.scr.annotations.Modified;
import org.apache.felix.scr.annotations.Property;
import org.apache.felix.scr.annotations.Reference;
import org.apache.felix.scr.annotations.ReferenceCardinality;
import org.apache.felix.scr.annotations.Service;
import org.onlab.packet.MacAddress;
import org.onlab.packet.VlanId;
import org.onlab.util.Tools;
import org.onosproject.cfg.ComponentConfigService;
import org.onosproject.core.ApplicationId;
import org.onosproject.core.CoreService;
import org.onosproject.net.ConnectPoint;
import org.onosproject.net.Host;
import org.onosproject.net.HostLocation;
import org.onosproject.net.PortNumber;
import org.onosproject.net.host.DefaultHostDescription;
import org.onosproject.net.host.HostDescription;
import org.onosproject.net.host.HostProvider;
import org.onosproject.net.host.HostProviderRegistry;
import org.onosproject.net.host.HostProviderService;
import org.onosproject.net.provider.AbstractProvider;
import org.onosproject.net.provider.ProviderId;
import org.onosproject.openflow.controller.Dpid;
import org.onosproject.openflow.controller.OpenFlowController;
import org.onosproject.openflow.controller.OpenFlowMessageListener;
import org.onosproject.openflow.controller.OpenFlowSwitch;
import org.onosproject.openflow.controller.OpenFlowSwitchListener;
import org.onosproject.openflow.controller.OpenFlowWirelessSwitch;
import org.onosproject.openflow.controller.RoleState;
import org.osgi.service.component.ComponentContext;
import org.projectfloodlight.openflow.protocol.OFFactory;
import org.projectfloodlight.openflow.protocol.OFIeee80211CapabilityFlags;
import org.projectfloodlight.openflow.protocol.OFIeee80211HtCap;
import org.projectfloodlight.openflow.protocol.OFIeee80211VhtCap;
import org.projectfloodlight.openflow.protocol.OFMessage;
import org.projectfloodlight.openflow.protocol.OFPortStatus;
import org.projectfloodlight.openflow.protocol.OFSdwnAddLvap;
import org.projectfloodlight.openflow.protocol.OFSdwnAddClient;
import org.projectfloodlight.openflow.protocol.OFSdwnDelLvap;
import org.projectfloodlight.openflow.protocol.OFSdwnDelClient;
import org.projectfloodlight.openflow.protocol.OFSdwnGetClientsReply;
import org.projectfloodlight.openflow.protocol.OFSdwnGetClientsRequest;
import org.projectfloodlight.openflow.protocol.OFSdwnHeader;
import org.projectfloodlight.openflow.protocol.OFSdwnIeee80211Mgmt;
import org.projectfloodlight.openflow.protocol.OFSdwnIeee80211MgmtReply;
import org.projectfloodlight.openflow.protocol.OFSdwnReply;
import org.projectfloodlight.openflow.protocol.OFSdwnSetChannel;
import org.projectfloodlight.openflow.protocol.XidGenerator;
import org.projectfloodlight.openflow.protocol.XidGenerators;
import org.projectfloodlight.openflow.types.McsRxMask;
import org.projectfloodlight.openflow.types.OFPort;
import org.slf4j.Logger;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

import static com.google.common.base.Preconditions.checkNotNull;
import static java.lang.System.currentTimeMillis;
import static org.onosproject.net.DeviceId.deviceId;
import static org.onosproject.net.HostId.hostId;
import static org.onosproject.openflow.controller.Dpid.uri;
import static org.slf4j.LoggerFactory.getLogger;

/**
 * SDWN controller for ONOS.
 */
@Component(immediate = true)
@Service
public class SdwnController implements SdwnCoreService {
    private static final String TRANSACTION_TIMEOUT = "transactionTimeout";
    private static final long DEFAULT_TRANSACTION_TIMEOUT = 5;

    @Reference(cardinality = ReferenceCardinality.MANDATORY_UNARY)
    protected CoreService coreService;

    @Reference(cardinality = ReferenceCardinality.MANDATORY_UNARY)
    protected OpenFlowController controller;

    @Reference(cardinality = ReferenceCardinality.MANDATORY_UNARY)
    protected HostProviderRegistry hostProviderRegistry;

    @Reference(cardinality = ReferenceCardinality.MANDATORY_UNARY)
    protected ComponentConfigService componentConfigService;

    protected HostProviderService hostProviderService;

    private ApplicationId appId;

    private InternalSwitchListener switchListener = new InternalSwitchListener(this);
    private InternalSdwnMessageListener msgListener = new InternalSdwnMessageListener();
    private InternalHostProvider hostProvider = new InternalHostProvider();

    private final Logger log = getLogger(getClass());

    @Property(name = TRANSACTION_TIMEOUT, longValue = DEFAULT_TRANSACTION_TIMEOUT,
            label = "Number of second that the App waits for a switch to respond to a query" +
                    "before removing all state associated with the transaction.")
    private long transactionTimeout = DEFAULT_TRANSACTION_TIMEOUT;

    private XidGenerator xidGen = XidGenerators.create();
    private TransactionManager transactionManager = new TransactionManager(xidGen);

    protected Map<SdwnAccessPoint, Set<MacAddress>> denyMap = new ConcurrentHashMap<>();
    protected Set<SdwnSwitchListener> switchListeners = new ConcurrentSet<>();
    protected Set<SdwnClientListener> clientListeners = new ConcurrentSet<>();
    protected MgmtFrameListenerList mgmtFrameListeners = new MgmtFrameListenerList();
    protected SdwnEntityStore store = new SdwnEntityStore();

    protected SdwnClientAuthenticatorService clientAuthenticator;

    @Activate
    protected void activate() {
        appId = coreService.registerApplication("de.tuberlin.inet.sdwn.sdwn-controller");
        componentConfigService.registerProperties(getClass());
        controller.addListener(switchListener);
        controller.addMessageListener(msgListener);
        hostProviderService = hostProviderRegistry.register(hostProvider);
        log.info("Started");
    }

    @Deactivate
    protected void deactivate() {
        disconnectWirelessSwitches();
        controller.removeListener(switchListener);
        controller.removeMessageListener(msgListener);
        hostProviderRegistry.unregister(hostProvider);
        hostProviderService = null;
        log.info("Stopped");
    }

    private void disconnectWirelessSwitches() {
        store.switches().stream()
                .map(controller::getSwitch)
                .forEach(OpenFlowSwitch::disconnectSwitch);
    }

    @Modified
    protected void modified(ComponentContext context) {
        Dictionary<?, ?> properties = context.getProperties();

        String updatedConfig = Tools.get(properties, TRANSACTION_TIMEOUT);
        if (!Strings.isNullOrEmpty(updatedConfig)) {
            transactionTimeout = Long.valueOf(updatedConfig);
            log.info("Transaction will be treated as failed after {} ms", updatedConfig);
        }
    }

    /* Client Authentication */
    // TODO
    @Override
    public boolean registerClientAuthenticator(SdwnClientAuthenticatorService authenticator) {
        if (clientAuthenticator != null) {
            return false;
        }

        clientAuthenticator = authenticator;
        log.info("New client authenticator: {}", authenticator.getClass().toString());
        return false;
    }

    @Override
    public void removeClientAuthenticator(SdwnClientAuthenticatorService authenticator) {
        if (clientAuthenticator.getClass().equals(authenticator.getClass())) {
            log.info("Client authenticator {} unregistered", clientAuthenticator.getClass().toString());
            clientAuthenticator = null;
        }
    }

    @Override
    public SdwnClient createClientFromJson(ObjectNode node) {
        return Client.fromJson(node);
    }

    @Override
    public Set<SdwnAccessPoint> apsForBssid(MacAddress bssid) {
        return store.apsByBssid(bssid);
    }

    @Override
    public SdwnAccessPoint apByDpidAndName(Dpid dpid, String name) {
        return store.apByDpidAndName(dpid, name);
    }

    @Override
    public void registerSwitchListener(SdwnSwitchListener listener) throws IllegalArgumentException {
        if (switchListeners.contains(listener)) {
            throw new IllegalArgumentException("Listener already registered");
        }
        switchListeners.add(listener);
    }

    @Override
    public void removeSwitchListener(SdwnSwitchListener listener) {
        switchListeners.remove(listener);
    }

    @Override
    public void registerClientListener(SdwnClientListener listener) throws IllegalArgumentException {
        if (clientListeners.contains(listener)) {
            throw new IllegalArgumentException("Listener already registered");
        }

        clientListeners.add(listener);
    }

    @Override
    public void removeClientListener(SdwnClientListener listener) {
        clientListeners.remove(listener);
    }

    @Override
    public void register80211MgtmFrameListener(Sdwn80211MgmtFrameListener listener, int priority) throws IllegalArgumentException {
        mgmtFrameListeners.addListener(listener, priority);
    }

    @Override
    public void remove80211MgmtFrameListener(Sdwn80211MgmtFrameListener listener) {
        mgmtFrameListeners.removeListener(listener);
    }

    @Override
    public Set<SdwnClient> clients() {
        return ImmutableSet.copyOf(store.clients());
    }

    @Override
    public Set<MacAddress> bssids() {
        return ImmutableSet.copyOf(store.bssids());
    }

    @Override
    public Set<SdwnAccessPoint> aps() {
        Set<SdwnAccessPoint> apSet = new HashSet<>();
        store.bssids().stream()
                .map(store::apsByBssid)
                .forEach(apSet::addAll);
        return apSet;
    }

    @Override
    public Set<SdwnAccessPoint> apsForSwitch(Dpid dpid) throws NoSuchElementException {
        return aps().stream().filter(ap -> ap.nic().switchID().equals(dpid)).collect(Collectors.toSet());
    }

    @Override
    public Set<Dpid> switches() {
        return ImmutableSet.copyOf(store.switches());
    }

    @Override
    public OpenFlowWirelessSwitch getSwitch(Dpid dpid) {
        checkNotNull(dpid);
        OpenFlowSwitch sw = controller.getSwitch(dpid);

        if (sw == null || !(sw instanceof OpenFlowWirelessSwitch)) {
            return null;
        }
        return (OpenFlowWirelessSwitch) sw;
    }

    @Override
    public void newClient(SdwnAccessPoint atAp, SdwnClient client) {
        if (atAp == null || client == null) {
            return;
        }
        store.addClient(client, atAp);
        publishHostForClient(client);
    }

    @Override
    public SdwnClient getClient(MacAddress mac) {
        return store.getClient(mac);
    }

    @Override
    public boolean addClientToAp(SdwnAccessPoint ap, SdwnClient client) {
        if (ap == null || client == null || !sendAddClient(ap, client)) {
            return false;
        }
        return true;
    }

    @Override
    public void blacklistClientAtAp(SdwnAccessPoint ap, MacAddress mac, long banTime) {
        checkNotNull(ap);
        checkNotNull(mac);
        OpenFlowWirelessSwitch sw = switchForAP(ap);
        checkNotNull(sw);

        sw.sendMsg(sw.factory().buildSdwnBlacklistClient()
                .setClient(org.projectfloodlight.openflow.types.MacAddress.of(mac.toBytes()))
                .setBanTime(banTime < 0 ? 0 : banTime > Integer.MAX_VALUE ? Integer.MAX_VALUE : (int) banTime)
                .setIfNo(OFPort.of(ap.portNumber()))
                .build()
        );

        ap.blacklistClient(mac);
    }

    @Override
    public void clearClientBlacklistingAtAp(SdwnAccessPoint ap, MacAddress mac) {
        checkNotNull(ap);
        checkNotNull(mac);
        OpenFlowWirelessSwitch sw = switchForAP(ap);
        checkNotNull(sw);

        sw.sendMsg(sw.factory().buildSdwnBlacklistClient()
                .setIfNo(OFPort.of(ap.portNumber()))
                .setBanTime(0)
                .setClient(org.projectfloodlight.openflow.types.MacAddress.of(mac.toBytes()))
                .build());

        ap.clearClientBlacklisting(mac);
    }

    @Override
    public boolean removeClientFromAp(MacAddress clientMac, long banTime) {
        SdwnClient client = store.getClient(clientMac);
        if (client == null) {
            log.error("Unknown client: {}", clientMac);
            return false;
        }

        long xid = xidGen.nextXid();

        if (!sendDelClient(client.ap(), client, 1, true, banTime, xid)) {
            return false;
        }

        transactionManager.startTransaction(new DelClientTransaction(client, client.ap(), banTime, this, 5000));
        return true;
    }

    @Override
    public boolean sendMessage(Dpid dpid, OFMessage msg) {
        checkNotNull(dpid);
        checkNotNull(msg);

        OpenFlowSwitch ofsw = controller.getSwitch(dpid);

        if (ofsw == null || !ofsw.isConnected() || !(ofsw instanceof OpenFlowWirelessSwitch)) {
            // TODO log error
            return false;
        }

        ofsw.sendMsg(msg);
        return true;
    }

    @Override
    public void removeClient(SdwnClient client) {
        client.disassoc();
    }

    @Override
    public boolean setChannel(Dpid dpid, int ifNo, int freq, int beaconCount) {
        OpenFlowWirelessSwitch sw = wirelessSwitchForDpid(dpid);
        if (sw == null) {
            log.error("Unknown switch: {}", dpid);
            return false;
        }

        boolean supported = false;
        for (SdwnNic nic : store.nicsForSwitch(dpid)) {
            if (nic.supportsFrequency(freq)) {
                supported = true;
                break;
            }
        }

        if (!supported) {
            log.error("{} does not support {} GHz", dpid,
                    String.format("%1.3f", (double) freq / 1000.0));
            return false;
        }

        OFSdwnSetChannel cmd = sw.factory().buildSdwnSetChannel()
                .setXid(xidGen.nextXid())
                .setIfNo(OFPort.of(ifNo))
                .setFrequency(freq)
                .setBeaconCount(beaconCount)
                .build();

        sw.sendMsg(cmd);
        return true;
    }

    @Override
    public long startTransaction(SdwnTransaction t) {
        return transactionManager.startTransaction(t);
    }

    @Override
    public long startTransactionChain(SdwnTransactionChain c) {
        return transactionManager.startTransactionChain(c);
    }

    @Override
    public void abortTransaction(long xid) {
        transactionManager.abortTransaction(xid);
    }

    private void send80211MgmtReply(MacAddress client, SdwnAccessPoint ap, long xid, boolean denied) {
        Dpid dpid = ap.nic().switchID();
        if (dpid == null) {
            log.error("No switch found for AP {}", ap.bssid());
            return;
        }

        OpenFlowWirelessSwitch sw = wirelessSwitchForDpid(dpid);
        if (sw == null) {
            log.error("No switch found for DPID {}", dpid);
            return;
        }

        OFSdwnIeee80211MgmtReply reply = sw.factory().buildSdwnIeee80211MgmtReply()
                .setXid(xid)
                .setIfNo(OFPort.of(ap.portNumber()))
                .setDeny(denied ? (short) 1 : (short) 0).build();
        sw.sendMsg(reply);
    }

    private OpenFlowWirelessSwitch wirelessSwitchForDpid(Dpid dpid) {
        OpenFlowSwitch ofSw = controller.getSwitch(dpid);
        if (ofSw == null || !ofSw.isConnected() || !(ofSw instanceof OpenFlowWirelessSwitch)) {
            return null;
        }

        return (OpenFlowWirelessSwitch) ofSw;
    }

    @Override
    public Dpid getRelatedOfSwitch(Dpid dpid) {
        return store.relatedSwitch(dpid);
    }

    @Override
    public boolean sendMsg(Dpid dpid, OFMessage msg) {
        if (dpid == null || msg == null) {
            return false;
        }

        OpenFlowSwitch ofsw = controller.getSwitch(dpid);
        if (ofsw == null || !(ofsw instanceof OpenFlowWirelessSwitch)) {
            return false;
        }

        OpenFlowWirelessSwitch sw = (OpenFlowWirelessSwitch) ofsw;
        sw.sendMsg(msg);
        return true;
    }

    private class InternalSwitchListener implements OpenFlowSwitchListener {

        SdwnCoreService sdwnController;

        InternalSwitchListener(SdwnCoreService controller) {
            sdwnController = controller;
        }

        @Override
        public void switchAdded(Dpid dpid) {
            OpenFlowSwitch ofsw = controller.getSwitch(dpid);

            if (ofsw == null || !ofsw.isConnected() || !(ofsw instanceof OpenFlowWirelessSwitch)) {
                return;
            }

            OpenFlowWirelessSwitch sw = (OpenFlowWirelessSwitch) ofsw;

            log.info("new Wireless Switch: {}", dpid);
            store.putRelatedSwitch(dpid, sw.relatedOfSwitch());

            List<SdwnNic> nics = sw.nicEntities().stream()
                    .map(nicEntity -> Nic.fromOF(dpid, nicEntity, sw.sdwnEntities()))
                    .collect(Collectors.toList());

            log.info("NICs: {}", sw.nicEntities());

            store.putNics(nics);

            for (SdwnNic nic : nics) {
                nic.aps().forEach(ap -> {
                    store.putAp(ap, nic);
                    transactionManager.startTransaction(new GetClientsQuery(ap, dpid, sdwnController, 5000));
                });
            }

            // notify switch listeners
            switchListeners.forEach(listener -> listener.switchConnected(dpid));
        }

        private OFSdwnGetClientsRequest buildGetClientsMessage(OpenFlowWirelessSwitch sw, SdwnAccessPoint ap) {
            return sw.factory().buildSdwnGetClientsRequest()
                    .setIfNo(OFPort.of(ap.portNumber()))
                    .setXid(xidGen.nextXid())
                    .build();
        }

        @Override
        public void switchRemoved(Dpid dpid) {
            log.info("Wireless Switch disconnected: {}", dpid);
            store.removeSwitch(dpid);
            switchListeners.forEach(l -> l.switchDisconnected(dpid));
        }

        @Override
        public void switchChanged(Dpid dpid) {

        }

        @Override
        public void portChanged(Dpid dpid, OFPortStatus ofPortStatus) {

        }

        @Override
        public void receivedRoleReply(Dpid dpid, RoleState roleState, RoleState roleState1) {

        }
    }

    private OpenFlowWirelessSwitch switchForAP(SdwnAccessPoint ap) {
        if (ap.nic() == null) {
            return null;
        }

        Dpid swDpid = ap.nic().switchID();
        if (swDpid == null) {
            return null;
        }

        OpenFlowWirelessSwitch sw = (OpenFlowWirelessSwitch) controller.getSwitch(swDpid);
        if (sw == null || !sw.isConnected()) {
            return null;
        }

        return sw;
    }

    private OFIeee80211HtCap emptyHtCaps(OFFactory factory) {
        return factory.buildIeee80211HtCap()
                .setMcs(factory.buildIeee80211McsInfo()
                        .setTxParams((short) 0)
                        .setRxHighest(0)
                        .setRxMask(McsRxMask.NONE)
                        .build())
                .setAmpduParamsInfo((short) 0)
                .setAntennaSelectionInfo((short) 0)
                .setCapInfo(0)
                .setExtendedHtCapInfo(0)
                .setTxBfCapInfo(0L)
                .build();
    }

    private OFIeee80211VhtCap emptyVhtCap(OFFactory factory) {
        return factory.buildIeee80211VhtCap()
                .setMcs(
                        factory.buildIeee80211VhtMcsInfo()
                                .setRxVhtMcs(0)
                                .setTxVhtMcs(0)
                                .setTxHighest(0)
                                .setRxHighest(0)
                                .build())
                .setCapInfo(0L)
                .build();
    }

    private boolean sendAddClient(SdwnAccessPoint ap, SdwnClient client) {
        if (client.ap() != null || client.ap() == ap) {
            log.error("Cannot add client {} to AP {}: Already associated with {}",
                    client.macAddress(), ap.bssid(), client.ap().bssid());
            return false;
        }

        OpenFlowWirelessSwitch sw = switchForAP(ap);
        if (sw == null) {
            return false;
        }

        log.info("Adding client {} to AP {} on Switch {}",
                client.macAddress(), ap.name(), sw.getStringId());

        // TODO: verify overlap of supported rates
        StringBuilder sb = new StringBuilder("");
        client.rates().forEach(r -> sb.append(String.format("%02x", r.rate())));

        OFSdwnAddClient.Builder cmdBuilder = sw.factory().buildSdwnAddClient()
                .setClient(org.projectfloodlight.openflow.types.MacAddress.of(client.macAddress().toBytes()))
                .setAp(OFPort.of(ap.portNumber()))
                .setCapabilities(Ieee80211Capability.toInt(client.capabilities()))
                .setHtCapabilities(client.htCapabilities())
                .setSupportedRates(sb.toString())
                .setXid(xidGen.nextXid());

        List<OFIeee80211CapabilityFlags> capFlags = Collections.emptyList();
        if (client.htCapabilities() != null) {
            cmdBuilder.setHtCapabilities(client.htCapabilities());
            capFlags.add(OFIeee80211CapabilityFlags.CAPABILITY_FLAG_HT_CAP);
        } else {
            cmdBuilder.setHtCapabilities(emptyHtCaps(sw.factory()));
        }

        if (client.vhtCapabilities() != null) {
            cmdBuilder.setVhtCapabilities(client.vhtCapabilities());
            capFlags.add(OFIeee80211CapabilityFlags.CAPABILITY_FLAG_VHT_CAP);
        } else {
            cmdBuilder.setVhtCapabilities(emptyVhtCap(sw.factory()));
        }

        if (client.keys() != null) {
            cmdBuilder.setKeys(ClientCryptoKeys.toOF(client.keys(), sw.factory()));
        }

        transactionManager.startTransaction(new AddClientTransaction(client, ap, 3000));
        log.info("Sending {}", cmdBuilder.build());
        sw.sendMsg(cmdBuilder.build());
        return true;
    }

    private void publishHostForClient(SdwnClient client) {
        Dpid dpid = client.ap().nic().switchID();
        if (dpid == null) {
            log.error("Switch not found");
            return;
        }

        ConnectPoint connPoint = new ConnectPoint(deviceId(uri(dpid)),
                PortNumber.portNumber(client.ap().portNumber()));
        HostLocation loc = new HostLocation(connPoint, currentTimeMillis());
        HostDescription desc = new DefaultHostDescription(client.macAddress(), VlanId.NONE, loc);

        log.info("Client {} is associated with AP {} ({})", client.macAddress(), client.ap().name(), client.ap().bssid());

        hostProviderService.hostDetected(hostId(client.macAddress()), desc, false);
    }

    // TODO: proper enum for reason code
    private boolean sendDelClient(SdwnAccessPoint ap, SdwnClient client,
                                  int reason, boolean deAuth, long banTime,
                                  long xid) {

        if (!client.ap().equals(ap) || !ap.clientIsAssociated(client.macAddress())) {
            log.error("Could not delete client {} from AP {} on {}: Client is not associated.",
                    client.macAddress(), ap.name(), ap.nic().switchID());
            return false;
        }

        OpenFlowWirelessSwitch sw = switchForAP(ap);
        if (sw == null) {
            log.error("Could not delete client {} from AP {} on {}: Switch unknown or not connected.",
                    client.macAddress(), ap.name(), ap.nic().switchID());
            return false;
        }

        OFSdwnDelClient cmd = sw.factory().buildSdwnDelClient()
                .setAp(OFPort.of(ap.portNumber()))
                .setClient(org.projectfloodlight.openflow.types.MacAddress.of(client.macAddress().toBytes()))
                .setXid(xid)
                .setReason(reason)
                .setDeauth(deAuth ? (short) 1 : (short) 0)
                .setBanTime(banTime)
                .build();

        log.info("Sending {}", cmd);

        sw.sendMsg(cmd);
        return true;
    }

    /**
     * Instruct the switch with the given NIC to install a new LVAP on the NIC.
     *
     * @param nic    the NIC where the LVAP should be created
     * @param bssid  the BSSID the LVAP should advertise. Must be unique.
     * @param beacon the LVAP's beacon frame
     */
    public void addLvap(Nic nic, MacAddress bssid, byte[] beacon) {
        Dpid swDpid = nic.switchID();
        if (swDpid == null) {
            return;
        }

        OpenFlowWirelessSwitch sw = (OpenFlowWirelessSwitch) controller.getSwitch(swDpid);
        if (sw == null || !sw.isConnected()) {
            return;
        }

        log.info("adding LVAP {} to {} on {}", bssid, nic, swDpid);

        OFIeee80211HtCap htCap = emptyHtCaps(sw.factory());

        OFIeee80211VhtCap vhtCap = emptyVhtCap(sw.factory());

        OFSdwnAddLvap cmd = sw.factory().buildSdwnAddLvap()
                .setBssid(org.projectfloodlight.openflow.types.MacAddress.of(bssid.toBytes()))
                .setCapFlags(ImmutableSet.of())
                .setHtCapabilities(htCap)
                .setVhtCapabilities(vhtCap)
                .setBeacon(beacon)
                .build();
        sw.sendMsg(cmd);
    }

    public void delLvap(Dpid dpid, MacAddress bssid) {
        OpenFlowWirelessSwitch sw = (OpenFlowWirelessSwitch) controller.getSwitch(dpid);
        if (sw == null || !sw.isConnected()) {
            return;
        }

        OFSdwnDelLvap cmd = sw.factory().buildSdwnDelLvap()
                .setBssid(org.projectfloodlight.openflow.types.MacAddress.of(bssid.toBytes()))
                .build();
        sw.sendMsg(cmd);
    }

    private class InternalSdwnMessageListener implements OpenFlowMessageListener {

        private boolean isSdwnMsg(OFMessage msg) {
            return (msg instanceof OFSdwnHeader || msg instanceof OFSdwnReply);
        }

        @Override
        public void handleIncomingMessage(Dpid dpid, OFMessage ofMessage) {
            if (!isSdwnMsg(ofMessage)) {
                return;
            }

            if (ofMessage instanceof OFSdwnIeee80211Mgmt) {
                handleMgmtFrame(dpid, (OFSdwnIeee80211Mgmt) ofMessage);
            } else if (ofMessage instanceof OFSdwnAddClient) {
                handleAddClientNotification(dpid, (OFSdwnAddClient) ofMessage);
            } else if (ofMessage instanceof OFSdwnDelClient) {
                handleDelClientNotification(dpid, (OFSdwnDelClient) ofMessage);
            } else if (ofMessage instanceof OFSdwnGetClientsReply) {
                transactionManager.msgReceived(dpid, ofMessage);
            } else if (ofMessage instanceof OFSdwnSetChannel) {
                handleSetChannelNotification(dpid, (OFSdwnSetChannel) ofMessage);
            }
        }

        private void handleSetChannelNotification(Dpid dpid, OFSdwnSetChannel msg) {
            SdwnNic apNic = store.nicsForSwitch(dpid).stream()
                    .filter(nic -> !nic.aps().stream()
                            .filter(ap -> ap.portNumber() == msg.getIfNo().getPortNumber())
                            .collect(Collectors.toList()).isEmpty())
                    .findFirst().orElse(null);
            if (apNic == null) {
                return;
            }

            for (SdwnAccessPoint ap : apNic.aps()) {
                if (ap.portNumber() != msg.getIfNo().getPortNumber()) {
                    continue;
                }

                SdwnFrequency freq = null;
                for (SdwnFrequencyBand band : apNic.bands()) {
                    if (band.containsFrequency(msg.getFrequency())) {
                        freq = band.frequencies().stream()
                                .filter(f -> f.hz() == msg.getFrequency())
                                .findFirst().orElse(null);
                        break;
                    }
                }

                if (freq == null) {
                    log.error("Set channel notification reports hz unsupported by NIC: {} MHz", msg.getFrequency());
                    return;
                }

                ap.setFrequency(freq);
                log.info("AP {} on {} is now operating at {} GHz (channel {})",
                        ap.name(), dpid, String.format("%1.3f", (double) msg.getFrequency() / 1000.0),
                        Ieee80211Channels.frequencyToChannel(msg.getFrequency()));
                return;
            }
        }

        // TODO: use thread pool for message handling
        // TODO: client authenticator needs to handle AUTH and ASSOC frames
        private void handleMgmtFrame(Dpid dpid, OFSdwnIeee80211Mgmt msg) {
            SdwnAccessPoint ap = ifNoToAp(dpid, msg.getIfNo().getPortNumber());
            if (ap == null) {
                return;
            }

            Sdwn80211MgmtFrameListener.ResponseAction responseAction;
            Sdwn80211MgmtFrameListener.ResponseAction lastResponse = Sdwn80211MgmtFrameListener.ResponseAction.NONE;
            int lastPriority = 0;

            switch (msg.getIeee80211Type()) {
                case ASSOC:
                    for (MgmtFrameListenerList.MgmtFrameListenerListEntry e : mgmtFrameListeners.list) {
                        responseAction = e.listener.receivedAssocRequest(MacAddress.valueOf(msg.getAddr().getBytes()),
                                ap, msg.getXid(), msg.getSsi(), msg.getFreq());

                        if ((responseAction != lastResponse) && (e.priority > lastPriority)) {
                            lastResponse = responseAction;
                            lastPriority = e.priority;
                        }
                    }
                    break;
                case AUTH:
                    for (MgmtFrameListenerList.MgmtFrameListenerListEntry e : mgmtFrameListeners.list) {
                        responseAction = e.listener.receivedAuthRequest(MacAddress.valueOf(msg.getAddr().getBytes()),
                                ap, msg.getXid(), msg.getSsi(), msg.getFreq());

                        if ((responseAction != lastResponse) && (e.priority > lastPriority)) {
                            lastResponse = responseAction;
                            lastPriority = e.priority;
                        }
                    }
                    break;
                case PROBE:
                    for (MgmtFrameListenerList.MgmtFrameListenerListEntry e : mgmtFrameListeners.list) {
                        responseAction = e.listener.receivedProbeRequest(MacAddress.valueOf(msg.getAddr().getBytes()),
                                ap, msg.getXid(), msg.getSsi(), msg.getFreq());

                        if ((responseAction != lastResponse) && (e.priority > lastPriority)) {
                            lastResponse = responseAction;
                            lastPriority = e.priority;
                        }
                    }
                    break;
            }

            if (lastResponse != Sdwn80211MgmtFrameListener.ResponseAction.NONE) {
                log.info("{}ing {} request by {} at [{}]:{}", lastResponse.equals(Sdwn80211MgmtFrameListener.ResponseAction.DENY) ? "Deny" : "Grant",
                        msg.getIeee80211Type(), msg.getAddr(), dpid, ap.name());
                send80211MgmtReply(MacAddress.valueOf(msg.getAddr().getBytes()), ap, msg.getXid(), lastResponse.equals(Sdwn80211MgmtFrameListener.ResponseAction.DENY));
            }
        }

        private SdwnAccessPoint ifNoToAp(Dpid sw, int ifNo) {
            for (SdwnNic nic : store.nicsForSwitch(sw)) {
                for (SdwnAccessPoint ap : nic.aps()) {
                    if (ap.portNumber() == ifNo) {
                        return ap;
                    }
                }
            }
            return null;
        }

        private void handleAddClientNotification(Dpid dpid, OFSdwnAddClient msg) {
            SdwnAccessPoint ap = ifNoToAp(dpid, msg.getAp().getPortNumber());

            if (ap == null) {
                log.error("No Access Point with number {} known on {}", msg.getAp().getPortNumber(), dpid);
                return;
            }

            SdwnClient client = Client.fromAddClient(ap, msg);
            store.addClient(client, ap);
            clientListeners.forEach(l -> l.clientAssociated(client));
            // FIXME! hostapd on the agent does not have HT/VHT capabilities at this time
            //       send get client request to fetch capabilities/(V)HT capabilities. Needs new SdwnTransactionContext
            publishHostForClient(client);
            transactionManager.msgReceived(dpid, msg);
        }

        private void handleDelClientNotification(Dpid dpid, OFSdwnDelClient msg) {
            SdwnClient client = store.getClient(MacAddress.valueOf(msg.getClient().getBytes()));
            checkNotNull(client);

            if (client.ap().portNumber() != msg.getAp().getPortNumber()) {
                log.error("Data mismatch! Switch {} reports {} disassociated from AP {}. We thought it was associated with {}({}) on {}",
                        dpid, client.macAddress(), msg.getAp().getPortNumber(),
                        client.ap().name(), client.ap().portNumber(), client.ap().nic().switchID());
                store.removeClient(client.macAddress());
            }

            log.info("Client {} disconnected from AP {} on {}", client.macAddress(), client.ap(), dpid);

            SdwnAccessPoint ap = client.ap();
            client.disassoc();
            clientListeners.forEach(l -> l.clientDisassociated(client, ap));
            store.removeClient(client.macAddress());
            hostProviderService.hostVanished(hostId(client.macAddress()));
            transactionManager.msgReceived(dpid, msg);
        }

        @Override
        public void handleOutgoingMessage(Dpid dpid, List<OFMessage> list) {
            // TODO
        }
    }

    private class InternalHostProvider extends AbstractProvider implements HostProvider {

        InternalHostProvider() {
            super(new ProviderId("of", "de.tuberlin.inet.sdwn.provider.client", true));
        }

        @Override
        public void triggerProbe(Host host) {
            SdwnClient client = store.getClient(host.mac());
            if (client == null) {
                log.error("Could not probe {}: not connected", host);
            }
        }
    }
}
