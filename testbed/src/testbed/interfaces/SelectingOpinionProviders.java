package testbed.interfaces;

import java.util.List;

/**
 * Interface for implementing trust models that selects opinion providers.
 * 
 * @author David
 * 
 */
public interface SelectingOpinionProviders {

    /**
     * This method returns a list of opinion requests that will be generated in
     * current tick.
     * 
     * <p>
     * Invalid request (for invalid agents or services) are ignored.
     * 
     * @return A list of opinion requests.
     */
    public List<OpinionRequest> getOpinionRequests();

    /**
     * Conveys the list of available agents to the trust model.
     * 
     * @param agents
     */
    public void setAgents(List<Integer> agents);

    /**
     * Conveys the list of available services to the trust model.
     * 
     * @param services
     */
    public void setServices(List<Integer> services);
}
