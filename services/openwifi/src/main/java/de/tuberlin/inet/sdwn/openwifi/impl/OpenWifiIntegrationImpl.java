package de.tuberlin.inet.sdwn.openwifi.impl;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.JsonNodeType;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.google.common.base.Strings;
import de.tuberlin.inet.sdwn.openwifi.api.OpenWifiIntegrationService;
import org.apache.felix.scr.annotations.Activate;
import org.apache.felix.scr.annotations.Component;
import org.apache.felix.scr.annotations.Deactivate;
import org.apache.felix.scr.annotations.Modified;
import org.apache.felix.scr.annotations.Property;
import org.apache.felix.scr.annotations.Service;
import org.glassfish.jersey.client.JerseyClientBuilder;
import org.onlab.packet.IpAddress;
import org.onlab.rest.JsonBodyWriter;
import org.onlab.util.Tools;
import org.osgi.service.component.ComponentContext;
import org.slf4j.Logger;

import javax.ws.rs.ProcessingException;
import javax.ws.rs.client.Client;
import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.client.Entity;
import javax.ws.rs.client.WebTarget;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URL;
import java.util.ArrayList;
import java.util.Dictionary;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import static javax.ws.rs.client.ClientBuilder.newClient;
import static org.slf4j.LoggerFactory.getLogger;

@Component(immediate = true)
@Service
public class OpenWifiIntegrationImpl implements OpenWifiIntegrationService {

    private static final String CAPABILITY_MATCH = "capabilityMatch";
    private static final String CAPABILITY_SCRIPT = "capabilityScript";
    private static final String CAPABILITY_NAME = "name";

    private static final String DEFAULT_CAPABILITY_MATCH = "wlanflow - 1-1";
    private static final String DEFAULT_CAPABILITY_SCRIPT = "opkg list-installed | grep wlanflow";
    private static final String DEFAULT_CAPABILITY_NAME = "wlanflow 1-1";

    private final Logger log = getLogger(getClass());
    private ObjectMapper objMapper = new ObjectMapper();
    private Map<OpenWifiRestClient, String> registered = new ConcurrentHashMap<>();

    @Property(name = CAPABILITY_MATCH, value = DEFAULT_CAPABILITY_MATCH,
    label = "Output of the capability script on the node.")
    private String capabilityMatch = DEFAULT_CAPABILITY_MATCH;

    @Property(name = CAPABILITY_SCRIPT, value = DEFAULT_CAPABILITY_SCRIPT,
    label = "Script to be executed on the node. Output has to match the value of capabilityMatch.")
    private String capabilityScript = DEFAULT_CAPABILITY_SCRIPT;

    @Property(name = CAPABILITY_NAME, value = DEFAULT_CAPABILITY_NAME,
    label = "Capability name as it appears in OpenWifi.")
    private String capabilityName = DEFAULT_CAPABILITY_NAME;

    @Activate
    public void activate() {
        log.info("Started");
    }

    @Deactivate
    public void deactivate() {
        registered.forEach(OpenWifiRestClient::unregister);
        log.info("Stopped");
    }

    @Modified
    public void modified(ComponentContext context) {
        Dictionary<?, ?> properties = context.getProperties();

        String updatedConfig = Tools.get(properties, CAPABILITY_MATCH);
        if (!Strings.isNullOrEmpty(updatedConfig)) {
            capabilityMatch = updatedConfig;
            log.info("capability_match value set to '{}'", updatedConfig);
        }

        updatedConfig = Tools.get(properties, CAPABILITY_SCRIPT);
        if (!Strings.isNullOrEmpty(updatedConfig)) {
            capabilityScript = updatedConfig;
            log.info("capability_script value set to '{}'", updatedConfig);
        }

        updatedConfig = Tools.get(properties, CAPABILITY_NAME);
        if (!Strings.isNullOrEmpty(updatedConfig)) {
            capabilityName = updatedConfig;
            log.info("capability_name value set to '{}'", updatedConfig);
        }
    }

    @Override
    public boolean register(String uri, IpAddress addr, int port, String name, String ubusPath) throws MalformedURLException {
        return register(uri, addr, port, capabilityName, capabilityMatch, capabilityScript, ubusPath);
    }

    @Override
    public boolean register(String uri, IpAddress addr, int port, String capName, String capMatch, String capScript, String ubusPath) throws MalformedURLException {
        OpenWifiRestClient client = newOpenWifiRestClient(uri);
        String id;

        log.info("Registering with OpenWifi at {}", uri);
        Response response = client.register(addr, port, capName, capMatch, capScript, ubusPath);

        if (response.getStatus() != 200) {
            log.error("Registration failed: {}", response);
            return false;
        }
        id = response.readEntity(String.class);

        // trim off quotation marks at both ends
        id = id.substring(1, id.length() - 1);
        log.info("Registered with OpenWifi. ID is {}", id);

        registered.put(client, id);
        return true;
    }

    @Override
    public boolean unregister() {
        registered.forEach((client, id) -> {
            Response response = client.unregister(id);

            if (response.getStatus() != 200) {
                log.error("De-registration from {} failed", client);
            } else {
                registered.remove(client);
            }
        });

        return registered.isEmpty();
    }

    @Override
    public boolean unregister(String uri) throws MalformedURLException {

        URL url = URI.create(uri).toURL();
        boolean success;

        for (Map.Entry<OpenWifiRestClient, String> e : registered.entrySet()) {
            if (e.getKey().url.equals(url)) {
                log.info("De-registering from {} with ID {}", e.getKey().url, e.getValue());

                success = e.getKey().unregister(e.getValue()).getStatus() == 200;
                if (success) {
                    registered.remove(e.getKey());
                }
                return success;
            }
        }

        log.error("No ID found for {}", url.getHost());
        return false;
    }

    @Override
    public boolean unregister(String uri, String id) throws MalformedURLException {
        Response response = newOpenWifiRestClient(uri).unregister(id);

        if (response.getStatus() != 200) {
            log.error("De-registration failed with status {}", response.getStatus());
            return false;
        }

        log.info("De-registered from {}", uri);
        return true;
    }

    @Override
    public Map<URL, Map<String, OpenWifiServiceEntity>> listServices() {

        Map<URL, Map<String, OpenWifiServiceEntity>> map = new ConcurrentHashMap<>();

        registered.forEach((client, s) -> {
            try {
                Map<String, OpenWifiServiceEntity> services = listServices(client.url.toString());

                if (services != null) {
                    map.put(client.url, services);
                }
            } catch (MalformedURLException e) {
                // this should never happen
            }
        });

        return map;
    }

    @Override
    public Map<String, OpenWifiServiceEntity> listServices(String serverUri) throws MalformedURLException {

        URL url = URI.create(serverUri).toURL();

        WebTarget target = JerseyClientBuilder.newBuilder()
                .register(JsonBodyReader.class).build()
                .target(url.toString()).path("service");

        Map<String, OpenWifiServiceEntity> serviceMap = new HashMap<>();
        JsonNode jsonMap = target.request(MediaType.APPLICATION_JSON_TYPE).get(JsonNode.class);

        if (!jsonMap.getNodeType().equals(JsonNodeType.OBJECT)) {
            return null;
        }

        try {
            jsonMap.fields().forEachRemaining(field -> {
                if (!field.getValue().getNodeType().equals(JsonNodeType.OBJECT)) {
                    throw new ProcessingException("Services JSON contains non-object service node.");
                }
                serviceMap.put(field.getKey(), readOpenWifiService((ObjectNode) field.getValue()));
            });

        } catch (ProcessingException e) {
            log.error("Error while reading OpenWifiService.", e);
            return null;
        }

        return serviceMap;
    }

    private OpenWifiServiceEntity readOpenWifiService(ObjectNode node) throws ProcessingException {
        OpenWifiServiceEntity service = new OpenWifiServiceEntity();

        node.fields().forEachRemaining(entry -> {
            switch (entry.getKey()) {
                case "name":
                    service.setName(throwExceptionOnNull(entry.getValue().textValue()));
                    break;
                case "capability_match":
                    service.setCapabilityMatch(throwExceptionOnNull(entry.getValue().textValue()));
                    break;
                case "capability_script":
                    service.setCapabilityScript(throwExceptionOnNull(entry.getValue().textValue()));
                    break;
                case "queries":
                    if (!entry.getValue().getNodeType().equals(JsonNodeType.ARRAY)) {
                        throw new ProcessingException("'queries' does not contain an array.");
                    }
                    service.setQueries(readOpenWifiQueries((ArrayNode) entry.getValue()));
            }
        });
        return service;
    }

    private String throwExceptionOnNull(String s) throws ProcessingException {
        if (s == null) {
            throw new ProcessingException("Encountered null String!");
        } else {
            return s;
        }
    }

    private List<OpenWifiQuery> readOpenWifiQueries(ArrayNode nodes) throws ProcessingException {
        List<OpenWifiQuery> queries = new ArrayList<>(2);
        nodes.forEach(node -> {
            if (!node.getNodeType().equals(JsonNodeType.OBJECT)) {
                throw new ProcessingException("'queries' contains non-object node");
            }
            queries.add(readOpenWifiQuery((ObjectNode) node));
        });

        return queries;
    }

    private OpenWifiQuery readOpenWifiQuery(ObjectNode node) throws ProcessingException {
        OpenWifiQuery query = new OpenWifiQuery();
        node.fields().forEachRemaining(entry -> {
            switch (entry.getKey()) {
                case "package":
                    query.setPkg(throwExceptionOnNull(entry.getValue().textValue()));
                    break;
                case "type":
                    query.setType(throwExceptionOnNull(entry.getValue().textValue()));
                    break;
                case "option":
                    query.setOption(throwExceptionOnNull(entry.getValue().textValue()));
                    break;
                case "set":
                    query.setVal(throwExceptionOnNull(entry.getValue().textValue()));
                    break;
            }
        });

        return query;
    }

    @Override
    public Set<URL> cachedAddresses() {
        Set<URL> urls = new HashSet<>();
        registered.forEach((client, s) -> urls.add(client.url));
        return urls;
    }

    private OpenWifiRestClient newOpenWifiRestClient(String uriStr) throws MalformedURLException {
        URL url = URI.create(uriStr).toURL();
        return new OpenWifiRestClient(url);
    }

    private class OpenWifiRestClient {

        URL url;

        OpenWifiRestClient(URL url) {
            this.url = url;
        }

        Response register(IpAddress addr, int port, String name, String capMatch, String capScript, String ubusPath) {
            Client client = ClientBuilder.newBuilder()
                    .register(JsonBodyWriter.class)
                    .build();

            ObjectNode request = objMapper.createObjectNode();
            ArrayNode queries = request.putArray("queries");
            createQuery(queries, "ipaddr", addr.toString());
            createQuery(queries, "port", String.valueOf(port));
            if (ubusPath != null) {
                createQuery(queries, "ubuspath", ubusPath);
            }

            request.put("name", name)
                    .put("capability_match", capMatch)
                    .put("capability_script", capScript);

            // TODO: ?key=blablubb_onos
            return client.target(url.toString() + "/service")
                    .request(MediaType.TEXT_PLAIN_TYPE)
                    .header("Content-Type", "application/json")
                    .post(Entity.json(request));
        }

        private void createQuery(ArrayNode array, String option, String val) {
            array.addObject()
                    .put("package", "sdwn")
                    .put("type", "controller")
                    .put("option", option)
                    .put("set", val);
        }

        Response unregister(String id) {
            log.info(String.format("%s/service/%s", url.toString(), id));
            return newClient().target(String.format("%s/service/%s", url.toString(), id)).request().delete();
        }

        @Override
        public String toString() {
            return url.toString();
        }
    }
}
