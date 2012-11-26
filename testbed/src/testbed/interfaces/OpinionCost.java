package testbed.interfaces;

import java.util.List;

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
     *            List of currently available agents
     * @param services
     *            List of available services
     * @param opinionRequests
     *            List of requested opinions
     * @return Score
     */
    public double evaluate(List<Integer> agents, List<Integer> services,
	    List<OpinionRequest> opinionRequests);
}
