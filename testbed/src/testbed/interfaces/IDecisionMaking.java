package testbed.interfaces;

import java.util.Map;
import java.util.Set;

/**
 * Interface for implementing the decision making component of trust models. A
 * trust model that wants to select interaction partners, must implement the
 * methods of this interface.
 * 
 * @author David
 * 
 */
public interface IDecisionMaking {

    /**
     * This method should return a map that defines partner agents, with which
     * Alpha wants to interact in the current time slot.
     * 
     * <p>
     * Keys in the resulting map must represent services, while values must
     * represent agents. Thus it is possible to interact with a single agent
     * more than once per time tick, but each interaction must be for a
     * different service.
     * 
     * <p>
     * The map must contain only valid agents and services, otherwise an
     * {@link IllegalArgumentException} will be thrown.
     * 
     * @param services
     *            Set of available types of services
     * @return A map representing partner selections.
     */
    public Map<Integer, Integer> getNextInteractionPartners(
	    Set<Integer> services);

}
