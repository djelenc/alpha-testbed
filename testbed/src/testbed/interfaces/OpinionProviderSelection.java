package testbed.interfaces;

import java.util.List;

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
     * This method should ignore invalid opinion requests; requests for opinions
     * from or about non-existing agents or for non-existing services.
     * 
     * @param opinionRequests
     *            A list of opinionRequests.
     */
    public void setOpinionRequests(List<OpinionRequest> opinionRequests);
}
