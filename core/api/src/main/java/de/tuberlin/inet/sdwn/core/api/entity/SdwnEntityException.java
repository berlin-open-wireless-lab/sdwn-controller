package de.tuberlin.inet.sdwn.core.api.entity;

import com.google.common.base.MoreObjects;

public class SdwnEntityException extends Throwable {

    protected final SdwnEntity entity;

    public SdwnEntityException(String msg, SdwnEntity cause) {
        super(msg);
        entity = cause;
    }

    public SdwnEntity getEntity() {
        return entity;
    }

    @Override
    public String toString() {
        return MoreObjects.toStringHelper(this)
                .add("message", this.getMessage())
                .add("cause", this.entity)
                .toString();
    }
}
