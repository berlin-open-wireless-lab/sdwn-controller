package de.tuberlin.inet.sdwn.mobilitymanager.rest;

import org.onlab.rest.AbstractWebApplication;

import java.util.Set;

public class MobilityManagerRestApplication extends AbstractWebApplication {
    @Override
    public Set<Class<?>> getClasses() {
        return getClasses(MobilityManagerWebResource.class);
    }
}
