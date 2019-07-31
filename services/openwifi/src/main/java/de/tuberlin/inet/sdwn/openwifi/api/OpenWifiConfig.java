package de.tuberlin.inet.sdwn.openwifi.api;

public class OpenWifiConfig {

    private String name;
    private String capabilityMatch;
    private String capabilityScript;
    private String ubusPath;

    public OpenWifiConfig(String name, String capabilityMatch, String capabilityScript, String ubusPath) {
        this.name = name;
        this.capabilityMatch = capabilityMatch;
        this.capabilityScript = capabilityScript;
        this.ubusPath = ubusPath;
    }

    public String getName() {
        return name;
    }

    public String getCapabilityMatch() {
        return capabilityMatch;
    }

    public String getCapabilityScript() {
        return capabilityScript;
    }

    public String getUbusPath() {
        return ubusPath;
    }
}
