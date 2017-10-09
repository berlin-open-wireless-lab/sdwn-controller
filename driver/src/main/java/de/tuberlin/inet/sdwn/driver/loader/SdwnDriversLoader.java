package de.tuberlin.inet.sdwn.driver.loader;

import org.apache.felix.scr.annotations.Component;
import org.onosproject.net.driver.AbstractDriverLoader;

@Component(immediate = true)
public class SdwnDriversLoader extends AbstractDriverLoader {

    public SdwnDriversLoader() {
        super("/sdwn-drivers.xml");
    }
}
