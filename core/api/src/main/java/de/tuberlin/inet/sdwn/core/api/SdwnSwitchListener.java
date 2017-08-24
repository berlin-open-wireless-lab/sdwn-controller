package de.tuberlin.inet.sdwn.core.api;

import org.onosproject.openflow.controller.Dpid;

public interface SdwnSwitchListener {

    void switchConnected(Dpid dpid);

    void switchDisconnected(Dpid dpid);
}
