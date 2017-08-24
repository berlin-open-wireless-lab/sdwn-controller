package de.tuberlin.inet.sdwn.openwifi.impl;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement
public class OpenWifiQuery {

    private String pkg;
    private String type;
    private String option;
    private String val;

    public OpenWifiQuery() {
        // JAXB needs default constructor
    }

    public OpenWifiQuery(String pkg, String type, String option, String val) {
        this.pkg = pkg;
        this.type = type;
        this.option = option;
        this.val = val;
    }

    @XmlElement(name = "package")
    public String getPkg() {
        return pkg;
    }

    public void setPkg(String pkg) {
        this.pkg = pkg;
    }

    @XmlElement
    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    @XmlElement
    public String getOption() {
        return option;
    }

    public void setOption(String option) {
        this.option = option;
    }

    @XmlElement(name = "set")
    public String getVal() {
        return val;
    }

    public void setVal(String val) {
        this.val = val;
    }

    @Override
    public String toString() {
        return String.format("{\"package\": \"%s\", \"type\": \"%s\", \"option\": \"%s\", \"set\": \"%s\"}",
                             pkg, type, option, val);
    }
}
