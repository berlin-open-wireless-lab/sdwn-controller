package de.tuberlin.inet.sdwn.core.api;

/**
 * Utility methods to resolve IEEE 802.11 channel numbers to frequencies and vice-versa.
 */
public class Ieee80211Channels {

    public enum FrequencyBand {
        IEEE80211_BAND_2GHZ,
        IEEE80211_BAND_5GHZ,
        IEEE80211_BAND_60GHZ
    }

    private Ieee80211Channels() {
        // prohibit instantiation
    }

    /**
     * Return the frequency in Hz for the given channel in the given frequency band.
     */
    public static int channelToFrequency(int channel, FrequencyBand band) {

        if (channel <= 0) {
            return 0;
        }

        switch (band) {
            case IEEE80211_BAND_2GHZ:
                if (channel == 14) {
                    return 2484;
                } else if (channel < 14) {
                    return 2407 + channel * 5;
                }
                break;
            case IEEE80211_BAND_5GHZ:
                if (channel >= 182 && channel <= 196) {
                    return 4000 + channel * 5;
                } else {
                    return 5000 + channel * 5;
                }
            case IEEE80211_BAND_60GHZ:
                if (channel < 5) {
                    return 56160 + channel * 2160;
                }
                break;
        }
        return 0;
    }

    /**
     * Return the channel for the given frequency in Hz.
     */
    public static int frequencyToChannel(long freq) {
        if (freq == 2484) {
            return 14;
        } else if (freq < 2484) {
            return (int) (freq - 2407) / 5;
        } else if (freq >= 4910 && freq <= 4980) {
            return (int) (freq - 4000L) / 5;
        } else if (freq <= 45000) /* DMG band lower limit */ {
            return (int) (freq - 5000) / 5;
        } else if (freq >= 58320 && freq <= 64800) {
            return (int) (freq - 56160) / 2160;
        } else {
            return 0;
        }
    }
}
