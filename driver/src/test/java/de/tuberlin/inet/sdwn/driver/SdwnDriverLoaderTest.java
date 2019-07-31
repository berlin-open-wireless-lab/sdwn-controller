package de.tuberlin.inet.sdwn.driver;

import de.tuberlin.inet.sdwn.driver.loader.SdwnDriversLoader;
import org.junit.Before;
import org.onosproject.net.driver.AbstractDriverLoaderTest;

public class SdwnDriverLoaderTest extends AbstractDriverLoaderTest {

    @Before
    public void setUp() {
        loader = new SdwnDriversLoader();
    }
}
