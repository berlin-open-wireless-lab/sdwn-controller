package de.tuberlin.inet.sdwn.core.api.entity;

import com.google.common.base.MoreObjects;
import org.projectfloodlight.openflow.protocol.OFIeee80211HtCap;

public class IeeeHtCapabilityParsingException extends SdwnEntityParsingException {

    protected final OFIeee80211HtCap htCap;

    public IeeeHtCapabilityParsingException(String msg, OFIeee80211HtCap htCap) {
        super(msg, null);
        this.htCap = htCap;
    }

    public OFIeee80211HtCap getHtCap() {
        return htCap;
    }

    @Override
    public String toString() {
        return MoreObjects.toStringHelper(this)
                .add("message", this.getMessage())
                .add("cause", this.htCap)
                .toString();
    }
}
