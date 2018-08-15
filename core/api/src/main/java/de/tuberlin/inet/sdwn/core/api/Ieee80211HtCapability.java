package de.tuberlin.inet.sdwn.core.api;


import com.google.common.collect.ImmutableSet;
import de.tuberlin.inet.sdwn.core.api.entity.IeeeHtCapabilityParsingException;
import de.tuberlin.inet.sdwn.core.api.entity.SdwnEntityParsingException;
import org.projectfloodlight.openflow.protocol.OFIeee80211HtCap;
import org.projectfloodlight.openflow.protocol.OFIeee80211McsInfo;

import java.util.Set;
import java.util.stream.Collectors;

/**
 * IEEE 802.11 HT capabilities abstraction.
 */
public class Ieee80211HtCapability {

    private final Set<IeeeHtCapabilityInfo> capInfo;
    private final int maxRxAmpduLen;
    private final float minRxAmpduSpacing;
    private final byte[] mcsRxMask;
    private final int mcsRxHighest;
    private final short mcsTxParams;

    private Ieee80211HtCapability(int maxRxAmpduLen, float minRxAmpduSpacing, Set<IeeeHtCapabilityInfo> capInfo, byte[] mcsRxMask, int mcsRxHighest, short mcsTxParams) {
        this.capInfo = capInfo;
        this.maxRxAmpduLen = maxRxAmpduLen;
        this.minRxAmpduSpacing = minRxAmpduSpacing;
        this.mcsRxMask = mcsRxMask;
        this.mcsRxHighest = mcsRxHighest;
        this.mcsTxParams = mcsTxParams;
    }

    public Set<IeeeHtCapabilityInfo> getCapInfo() {
        return capInfo;
    }

    public int getMaxRxAmpduLen() {
        return maxRxAmpduLen;
    }

    public float getMinRxAmpduSpacing() {
        return minRxAmpduSpacing;
    }

    public byte[] getMcsRxMask() {
        return mcsRxMask;
    }

    public int getMcsRxHighest() {
        return mcsRxHighest;
    }

    public short getMcsTxParams() {
        return mcsTxParams;
    }

    public static Ieee80211HtCapability fromOF(OFIeee80211HtCap ofHtCap) throws SdwnEntityParsingException {

        int maxRxAmpduLen = exponentToAmpduLength((short)(ofHtCap.getAmpduParamsInfo() >> 6));
        if (maxRxAmpduLen == 0) {
            throw new IeeeHtCapabilityParsingException("Invalid A-MPDU Length Exponent", ofHtCap);
        }

        float minRxAmpduSpacing = computeAmpduSpacing((short)((ofHtCap.getAmpduParamsInfo() >> 3) & 0x07));
        if (minRxAmpduSpacing < 0) {
            throw new IeeeHtCapabilityParsingException("Invalid Minimum MPDU Start Spacing", ofHtCap);
        }

        return new Ieee80211HtCapability(maxRxAmpduLen, minRxAmpduSpacing, fromInt(ofHtCap.getCapInfo()), ofHtCap.getMcs().getRxMask().getMask(), ofHtCap.getMcs().getRxHighest(), ofHtCap.getMcs().getTxParams());
    }

    public static Set<IeeeHtCapabilityInfo> fromInt(int capInfo) {
        return ImmutableSet.copyOf(CAPABILITIES.stream()
                                           .filter(htCap -> ((capInfo & htCap.mask) >> htCap.offset) == htCap.value)
                                           .collect(Collectors.toSet()));
    }

    public static int toInt(Set<IeeeHtCapabilityInfo> caps) {
        int capInfo = 0;

        for (IeeeHtCapabilityInfo cap : caps) {
            capInfo |= (cap.mask & (cap.value << cap.offset));
        }

        return capInfo;
    }

    public static final class IeeeHtCapabilityInfo {
        private final int mask;
        private final int offset;
        private final int value;
        public String name;

        private IeeeHtCapabilityInfo(int mask, int offset, int value, String name) {
            this.mask = mask;
            this.offset = offset;
            this.value = value;
            this.name = name;
        }
    }


    public static final IeeeHtCapabilityInfo LDPC_CODING_CAPABILITY = new IeeeHtCapabilityInfo(0x8000, 15, 1, "LDPC coding capability");

    public static final IeeeHtCapabilityInfo SUPPORTED_CHANNEL_WIDTH_20MHZ = new IeeeHtCapabilityInfo(0x4000, 14, 0, "Channel Width: 20MHz");
    public static final IeeeHtCapabilityInfo SUPPORTED_CHANNEL_WIDTH_20_40MHZ = new IeeeHtCapabilityInfo(0x4000, 14, 1, "Channel Width: 20/40Mhz");

    public static final IeeeHtCapabilityInfo SM_POWER_SAVE_STATIC = new IeeeHtCapabilityInfo(0x3000, 12, 0, "static SM Power Save");
    public static final IeeeHtCapabilityInfo SM_POWER_SAVE_DYNAMIC = new IeeeHtCapabilityInfo(0x3000, 12, 1, "dynamic SM Power Save");
    public static final IeeeHtCapabilityInfo SM_POWER_SAVE_DISABLED = new IeeeHtCapabilityInfo(0x3000, 12, 3, "SM Power Save disabled");

    public static final IeeeHtCapabilityInfo HT_GREENFIELD = new IeeeHtCapabilityInfo(0x0800, 11, 1, "HT Greenfield");

    public static final IeeeHtCapabilityInfo SHORT_GI_20MHZ = new IeeeHtCapabilityInfo(0x0400, 10, 1, "Short GI for 20MHz");

    public static final IeeeHtCapabilityInfo SHORT_GI_40MHZ = new IeeeHtCapabilityInfo(0x0200, 9, 1, "Short GI for 40MHz");

    public static final IeeeHtCapabilityInfo TX_STBC = new IeeeHtCapabilityInfo(0x0100, 8, 1, "TX STBC");

    public static final IeeeHtCapabilityInfo RX_STBC_ONE_SPATIAL_STREAM = new IeeeHtCapabilityInfo(0x00c0, 6, 1, "RX STBC: up to one Spatial Stream");
    public static final IeeeHtCapabilityInfo RX_STBC_TWO_SPATIAL_STREAMS = new IeeeHtCapabilityInfo(0x00c0, 6, 2, "RX STBC: up to 2 Spatial Streams");
    public static final IeeeHtCapabilityInfo RX_STBC_THREE_SPATIAL_STREAMS = new IeeeHtCapabilityInfo(0x00c0, 6, 3, "RX STBC: up to 3 Spatial Streams");

    public static final IeeeHtCapabilityInfo HT_DELAYED_BLOCK_ACK = new IeeeHtCapabilityInfo(0x0020, 5, 1, "HT Delayed Block Ack");

    public static final IeeeHtCapabilityInfo MAX_AMSDU_LENGTH_3839 = new IeeeHtCapabilityInfo(0x0010, 4, 0, "Max A-MSDU length 3839 octets");
    public static final IeeeHtCapabilityInfo MAX_AMSDU_LENGTH_7935 = new IeeeHtCapabilityInfo(0x0010, 4, 1, "Max A-MSDU length 7935 octets");

    public static final IeeeHtCapabilityInfo DSSS_CCK_MODE = new IeeeHtCapabilityInfo(0x0008, 3, 1, "DSSS/CCK");

    public static final IeeeHtCapabilityInfo FORTY_MHZ_INTOLERANT = new IeeeHtCapabilityInfo(0x0002, 1, 1, "40MHz Intolerant");

    public static final IeeeHtCapabilityInfo LSIG_TXOP_PROTECTION = new IeeeHtCapabilityInfo(0x0001, 0, 1, "L-SIG TXOP Protection Support");

    private static final Set<IeeeHtCapabilityInfo> CAPABILITIES = ImmutableSet.of(
            LDPC_CODING_CAPABILITY,
            SUPPORTED_CHANNEL_WIDTH_20MHZ,
            SUPPORTED_CHANNEL_WIDTH_20_40MHZ,
            SM_POWER_SAVE_STATIC,
            SM_POWER_SAVE_DYNAMIC,
            SM_POWER_SAVE_DISABLED,
            HT_GREENFIELD,
            SHORT_GI_20MHZ,
            SHORT_GI_40MHZ,
            TX_STBC,
            RX_STBC_ONE_SPATIAL_STREAM,
            RX_STBC_TWO_SPATIAL_STREAMS,
            RX_STBC_THREE_SPATIAL_STREAMS,
            HT_DELAYED_BLOCK_ACK,
            MAX_AMSDU_LENGTH_3839,
            MAX_AMSDU_LENGTH_7935,
            DSSS_CCK_MODE,
            FORTY_MHZ_INTOLERANT,
            LSIG_TXOP_PROTECTION
    );

    private static int exponentToAmpduLength(short exp) {
        switch (exp) {
            case 0: return 8191;  /* (2 ^(13 + 0)) -1 */
            case 1: return 16383; /* (2 ^(13 + 1)) -1 */
            case 2: return 32767; /* (2 ^(13 + 2)) -1 */
            case 3: return 65535; /* (2 ^(13 + 3)) -1 */
            default: return 0;
        }
    }

    private static float computeAmpduSpacing(short space) {
        switch (space) {
            case 0: return 0.0f;
            case 1: return 0.25f;
            case 2: return 0.5f;
            case 3: return 1.0f;
            case 4: return 2.0f;
            case 5: return 4.0f;
            case 6: return 8.0f;
            case 7: return 16.0f;
            default: return -1.0f;
        }
    }
}
