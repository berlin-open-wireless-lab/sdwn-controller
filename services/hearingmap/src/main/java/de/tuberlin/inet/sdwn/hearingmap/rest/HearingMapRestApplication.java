package de.tuberlin.inet.sdwn.hearingmap.rest;

import org.onlab.rest.AbstractWebApplication;

import java.util.Set;

/**
 * Simple Hearing Map Web Application.
 */
public class HearingMapRestApplication extends AbstractWebApplication {
    @Override
    public Set<Class<?>> getClasses() {
        return getClasses(HearingMapWebResource.class);
    }
}
