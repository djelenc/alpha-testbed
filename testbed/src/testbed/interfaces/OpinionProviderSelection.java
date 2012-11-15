package testbed.interfaces;

import java.util.Set;

/**
 * Interface for implementing selection of opinion providers in scenarios. A
 * scenario that would allow Alpha to select opinion providers, has to implement
 * method of this interface.
 * 
 */
public interface OpinionProviderSelection {

    /**
     * Conveys opinion request to the scenario.
     * 
     * <p>
     * This method is used to tell the scenario, which opinions it should
     * generate in the current tick. The set of opinionRequest should be
     * obtained form the trust model.
     * 
     * <p>
     * This methoud shold ignor invalid opinion requests, such that request
     * opinions from non-existent agents or for non-existent services.
     * 
     * @param opinionRequests
     *            A set of opinionRequests.
     */
    public void setOpinionRequests(Set<OpinionRequest> opinionRequests);
}
