package de.tuberlin.inet.sdwn.openwifi.impl;


import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import java.util.List;

@XmlRootElement
public class OpenWifiServiceEntity {

    private String name;
    private List<OpenWifiQuery> queries;
    private String capabilityMatch;
    private String capabilityScript;

    public OpenWifiServiceEntity() {
        // JAXB needs default constructor
    }

    public OpenWifiServiceEntity(String name, List<OpenWifiQuery> queries,
                                 String capabilityMatch, String capabilityScript) {
        super();
        this.name = name;
        this.queries = queries;
        this.capabilityMatch = capabilityMatch;
        this.capabilityScript = capabilityScript;
    }

    @XmlElement
    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    @XmlElement
    public List<OpenWifiQuery> getQueries() {
        return queries;
    }

    public void setQueries(List<OpenWifiQuery> queries) {
        this.queries = queries;
    }

    @XmlElement(name = "capability_match")
    public String getCapabilityMatch() {
        return capabilityMatch;
    }

    public void setCapabilityMatch(String capabilityMatch) {
        this.capabilityMatch = capabilityMatch;
    }

    @XmlElement(name = "capability_script")
    public String getCapabilityScript() {
        return capabilityScript;
    }

    public void setCapabilityScript(String capabilityScript) {
        this.capabilityScript = capabilityScript;
    }

    @Override
    public String toString() {
        return String.format("{\"name\": \"%s\", \"queries\": %s, \"capability_match\": \"%s\", \"capability_script\": \"%s\"}",
                             name, queriesToString(), capabilityMatch, capabilityScript);
    }

    private String queriesToString() {
        StringBuilder sb = new StringBuilder("[");
        queries.forEach(q -> sb.append(q.toString()).append(", "));
        sb.replace(sb.lastIndexOf(","), sb.length(), "]");
        return sb.toString();
    }
}
