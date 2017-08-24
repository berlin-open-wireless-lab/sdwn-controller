package de.tuberlin.inet.sdwn.core.api;

import org.onlab.packet.MacAddress;

public interface SdwnClientAuthenticatorService {

    /**
     * Basic mode of operation.
     * In BLACKLIST mode, marked clients will be rejected and unmarked clients will be accepted,
     * in WHITELIST mode, marked clients will be accepted and unmarked clients will be rejected.
     */
    enum AuthenticationPolicy {
        BLACKLIST,
        WHITELIST
    }

    /**
     * Return the current authentication policy
     */
    AuthenticationPolicy getPolicy();

    /**
     * Set the authentication policy. If {@code pol} differs from the current
     * policy, all marked client state should be invalidated.
     *
     * @param pol the policy to set
     */
    void setPolicy(AuthenticationPolicy pol);

    /**
     * Marks a client MAC address. The meaning of the mark depends on the current
     * {@code AuthenticationPolicy}.
     */
    void markClient(MacAddress mac);

    /**
     * Return {@code true} if the client is marked, {@code false} otherwise.
     */
    boolean isMarked(MacAddress mac);
}
