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
package de.tuberlin.inet.sdwn.openwifi.api;


import de.tuberlin.inet.sdwn.openwifi.impl.OpenWifiServiceEntity;
import org.onlab.packet.IpAddress;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.Map;
import java.util.Set;

/**
 * OpenWifi Integration Service.
 */
public interface OpenWifiIntegrationService {

    boolean register(String uri, String apiKey, IpAddress addr, int port, String name, String ubusPath) throws MalformedURLException;

    boolean register(String uri, String apiKey, IpAddress addr, int port, String name, String capabilityMatch, String capabilityScript, String ubusPath) throws MalformedURLException;

    boolean unregister();

    boolean unregister(String uri) throws MalformedURLException;

    boolean unregister(String uri, String apiKey, String id) throws MalformedURLException;

    Map<URL, Map<String, OpenWifiServiceEntity>> listServices();

    Map<String, OpenWifiServiceEntity> listServices(String serverUri) throws MalformedURLException;

    Set<URL> cachedAddresses();
}
