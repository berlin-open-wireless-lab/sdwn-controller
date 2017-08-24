package de.tuberlin.inet.sdwn.core.api;

import org.onosproject.openflow.controller.Dpid;

public abstract class SdwnSwitchListenerAdapter implements SdwnSwitchListener {

    @Override
    public void switchConnected(Dpid dpid) {

    }

    @Override
    public void switchDisconnected(Dpid dpid) {

    }
}
