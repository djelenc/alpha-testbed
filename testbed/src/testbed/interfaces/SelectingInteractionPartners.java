package testbed.interfaces;

import java.util.List;
import java.util.Map;

/**
 * Interface for implementing the decision making component of trust models that
 * selects interaction partners. A trust model that wants to select interaction
 * partners, has to implement the methods of this interface.
 * 
 * @author David
 * 
 */
public interface SelectingInteractionPartners {

    /**
     * This method should return a map that defines partner agents, with which
     * Alpha wants to interact in the current tick.
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
     *            List of available types of services
     * @return A map representing partner selections.
     */
    public Map<Integer, Integer> getInteractionPartners(List<Integer> services);

}
