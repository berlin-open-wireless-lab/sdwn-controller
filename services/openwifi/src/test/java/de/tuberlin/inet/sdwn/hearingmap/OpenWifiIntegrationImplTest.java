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
package de.tuberlin.inet.sdwn.openwifi;

import de.tuberlin.inet.sdwn.openwifi.impl.OpenWifiIntegrationImpl;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

/**
 * Set of tests of the ONOS application component.
 */
public class OpenWifiIntegrationImplTest {

    private OpenWifiIntegrationImpl component;

    private final String HOST = "127.0.0.1";
    private final int PORT = 12345;
    private final String PATH = "/path/to/openwifi/rest/api";

    @Before
    public void setUp() {
        component = new OpenWifiIntegrationImpl();
    }

    @After
    public void tearDown() {

    }

    @Test
    public void basics() {

    }

}
