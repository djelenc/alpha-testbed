package testbed.interfaces;

import java.util.Map;

/**
 * Interface for implementing selection of partners in scenarios. A scenario
 * that would allow Alpha to select interaction partners must implement methods
 * of this interface.
 * 
 */
public interface IPartnerSelection {

    /**
     * Sets the next interaction partners for the given services.
     * 
     * <p>
     * This method is used to tell the scenario, with which agents does agent
     * Alpha want to interact next. The parameter partners is a map, where keys
     * represent services and values agents. For instance, partners.put(3, 1),
     * indicates that agent Alpha wants to interact with agent 1 for the service
     * 3.
     * 
     * <p>
     * The programmer of the Scenario must ensure that the given values are
     * correct -- if, for instance, agent 1 (or service 3) are invalid values,
     * the method must throw {@link IllegalArgumentException}.
     * 
     * @param partners
     *            Map of services and agents
     */
    public void setNextInteractionPartners(Map<Integer, Integer> partners);
}
