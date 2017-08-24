package de.tuberlin.inet.sdwn.core.ctl.entity;

import org.projectfloodlight.openflow.protocol.OFIeee80211HtCap;

import java.util.HashSet;
import java.util.Set;

public class Ieee80211HtCapability {

    public enum Capability {
        RX_LDPC,

        HT20,
        HT20_HT40,

        STATIC_SM_POWER_SAFE,
        DYNAMIC_SM_POWER_SAFE,
        SM_POWER_SAFE_DISABLED,

        RX_GREENFIELD,
        RX_HT20_SGI,
        RX_HT40_SGI,
        TX_STBC,

        NO_RX_STBC,
        RX_STBC_1_STREAM,
        RX_STBC_2_STREAM,
        RX_STBC_3_STREAM,

        HT_DELAYED_BLOCK_ACK,

        MAX_AMSDU_LEN_3839,
        MAX_AMSDU_LEN_7935
//        PRINT_HT_CAP((cap & BIT(0)), "RX LDPC");
//        PRINT_HT_CAP((cap & BIT(1)), "HT20/HT40");
//        PRINT_HT_CAP(!(cap & BIT(1)), "HT20");

//        PRINT_HT_CAP(((cap >> 2) & 0x3) == 0, "Static SM Power Save");
//        PRINT_HT_CAP(((cap >> 2) & 0x3) == 1, "Dynamic SM Power Save");
//        PRINT_HT_CAP(((cap >> 2) & 0x3) == 3, "SM Power Save disabled");

//        PRINT_HT_CAP((cap & BIT(4)), "RX Greenfield");
//        PRINT_HT_CAP((cap & BIT(5)), "RX HT20 SGI");
//        PRINT_HT_CAP((cap & BIT(6)), "RX HT40 SGI");
//        PRINT_HT_CAP((cap & BIT(7)), "TX STBC");

//        PRINT_HT_CAP(((cap >> 8) & 0x3) == 0, "No RX STBC");
//        PRINT_HT_CAP(((cap >> 8) & 0x3) == 1, "RX STBC 1-stream");
//        PRINT_HT_CAP(((cap >> 8) & 0x3) == 2, "RX STBC 2-streams");
//        PRINT_HT_CAP(((cap >> 8) & 0x3) == 3, "RX STBC 3-streams");

//        PRINT_HT_CAP((cap & BIT(10)), "HT Delayed Block Ack");

//        PRINT_HT_CAP(!(cap & BIT(11)), "Max AMSDU length: 3839 bytes");
//        PRINT_HT_CAP((cap & BIT(11)), "Max AMSDU length: 7935 bytes");
    }

    private Set<Capability> capabilities;

    private Set<Capability> capsFromU16(int bitfield) {
        Set<Capability> caps = new HashSet<>();

        // TODO
        return caps;
    }

    private Ieee80211HtCapability(int capInfo) {
        this.capabilities = capsFromU16(capInfo);
    }

    public static Ieee80211HtCapability fromOF(OFIeee80211HtCap ofCap) {
        // TODO
        return null;
    }

    @Override
    public String toString() {
        // TODO
        return "";
    }
}
