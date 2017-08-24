package de.tuberlin.inet.sdwn.rest;

import org.onlab.rest.AbstractWebApplication;

import java.util.Set;

/**
 * SDWN Web application.
 */
public class SdwnWebApplication extends AbstractWebApplication {
    @Override
    public Set<Class<?>> getClasses() {
        return getClasses(ClientWebResource.class,
                          WirelessSwitchWebResource.class,
                          AccessPointWebResource.class);
    }
}
