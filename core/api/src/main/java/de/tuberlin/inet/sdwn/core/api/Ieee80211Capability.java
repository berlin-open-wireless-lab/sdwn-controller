package de.tuberlin.inet.sdwn.core.api;

import java.util.HashSet;
import java.util.Set;

/**
 * IEEE 802.11 capability types.
 */
public enum Ieee80211Capability {

    ESS(0x8000),
    IBSS(0x4000),
    CF_POLLABLE(0x2000),
    CF_POLL_REQUEST(0x1000),
    PRIVACY(0x0800),
    SHORT_PREAMBLE(0x0400),
    PBCC(0x0200),
    CHANNEL_AGILITY(0x0100),
    SPECTRUM_MANAGEMENT(0x0080),
    QoS(0x0040),
    SHORT_TIME_SLOT(0x0020),
    APSD(0x0010),
    RADIO_MEASUREMENT(0x0008),
    DSSS_OFDM(0x0004),
    DELAYED_BLOCK_ACK(0x0002),
    IMMEDIATE_BLOCK_ACK(0x0001);

    private final int value;
    Ieee80211Capability(int value) {
        this.value = value;
    }

    public static Ieee80211Capability valueOf(int i) {
        switch (i) {
            case 0x0001: return IMMEDIATE_BLOCK_ACK;
            case 0x0002: return DELAYED_BLOCK_ACK;
            case 0x0004: return DSSS_OFDM;
            case 0x0008: return RADIO_MEASUREMENT;
            case 0x0010: return APSD;
            case 0x0020: return SHORT_TIME_SLOT;
            case 0x0040: return QoS;
            case 0x0080: return SPECTRUM_MANAGEMENT;
            case 0x0100: return CHANNEL_AGILITY;
            case 0x0200: return PBCC;
            case 0x0400: return SHORT_PREAMBLE;
            case 0x0800: return PRIVACY;
            case 0x1000: return CF_POLL_REQUEST;
            case 0x2000: return CF_POLLABLE;
            case 0x4000: return IBSS;
            case 0x8000: return ESS;
            default: return null;
        }
    }

    /**
     * De-serialization method.
     * @param cap the 16-bit capabilities field
     * @return a set of capability types.
     */
    public static Set<Ieee80211Capability> fromInt(int cap) {
        Set<Ieee80211Capability> caps = new HashSet<>();
        for (int i = 0; i < 16; i++) {
            if (bitIsSet(cap, i)) {
                caps.add(valueOf(1 << i));
            }
        }
        return caps;
    }

    /**
     * Seriaization method.
     *
     * @param caps set of capability types
     * @return 16-bit capability representation.
     */
    public static int toInt(Set<Ieee80211Capability> caps) {
        int capInt = 0;

        for (Ieee80211Capability cap : caps) {
            capInt |= cap.value;
        }

        return capInt;
    }

    private static boolean bitIsSet(int i, int bit) {
        return ((i >> bit) & 1) == 1;
    }
}
