package testbed.interfaces;

import java.util.Map;

/**
 * 
 * Scenario interface that allows agent Alpha to select interaction partners.
 * 
 */
public interface IUtilityScenario extends IScenario {

    /**
     * Sets the next interaction partners for the given services.
     * 
     * <p>
     * This method is used to tell the scenario, with which agents does Alpha
     * want to interact. The parameter partners is a map, where keys represent
     * services and values agents. An example value 3 => 15, means that Alpha
     * wants to interact with agent 15, but with the service 3.
     * 
     * @param services
     */
    public void setNextInteractionPartners(Map<Integer, Integer> partners);
}
