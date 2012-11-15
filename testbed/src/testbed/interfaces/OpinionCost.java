package testbed.interfaces;

import java.util.Set;

/**
 * Interface for metrics that evaluate the cost of obtaining opinions.
 * 
 * @author David
 * 
 */
public interface OpinionCost extends Metric {

    /**
     * 
     * Evaluates given set of opinion requests.
     * 
     * @param agents
     *            Set of currently available agents
     * @param services
     *            Set of available services
     * @param opinionRequests
     *            Set of requested opinions
     * @return Score
     */
    public double evaluate(Set<Integer> agents, Set<Integer> services,
	    Set<OpinionRequest> opinionRequests);
}
