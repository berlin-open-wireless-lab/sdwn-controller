package de.tuberlin.inet.sdwn.core.api;

import org.onosproject.openflow.controller.Dpid;

/**
 * Callbacks for switch connect/disconnect events.
 */
public interface SdwnSwitchListener {

    void switchConnected(Dpid dpid);

    void switchDisconnected(Dpid dpid);
}
