package de.tuberlin.inet.sdwn.core.api;

import de.tuberlin.inet.sdwn.core.api.entity.SdwnAccessPoint;
import de.tuberlin.inet.sdwn.core.api.entity.SdwnClient;

/**
 * Callbacks for events related to client association.
 *
 */
public interface SdwnClientListener {

    void clientAssociated(SdwnClient c);

    void clientDisassociated(SdwnClient c, SdwnAccessPoint fromAp);
}
