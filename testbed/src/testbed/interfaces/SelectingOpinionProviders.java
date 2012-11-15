package testbed.interfaces;

import java.util.Set;

/**
 * Interface for implementing the decision making component of trust models that
 * selects opinion providers. A trust model that decides whom to ask for
 * opinions, has to implement the methods of this interface.
 * 
 * @author David
 * 
 */
public interface SelectingOpinionProviders {

    /**
     * This method returns a set of opinion requests that will be generated in
     * current tick.
     * 
     * <p>
     * If returned set contains invalid request (for invalid agents or services)
     * such requests are ignored.
     * 
     * @return A set of opinion requests.
     */
    public Set<OpinionRequest> getOpinionRequests();

}
