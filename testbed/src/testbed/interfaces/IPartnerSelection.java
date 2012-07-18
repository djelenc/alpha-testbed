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
     * represent agents and values services. For instance, partners.put(3, 1),
     * indicates that agent Alpha wants to interact with agent 3 for the service
     * 1.
     * 
     * <p>
     * The programmer must ensure that the given values are correct -- if, for
     * instance, agent 3 (or service 1) are invalid values, the method must
     * throw {@link IllegalArgumentException}.
     * 
     * @param partners
     *            Map of agents and services
     */
    public void setNextInteractionPartners(Map<Integer, Integer> partners);
}
