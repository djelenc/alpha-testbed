package testbed.metric;

import java.util.List;

import testbed.interfaces.OpinionCost;
import testbed.interfaces.OpinionRequest;

/**
 * Opinion cost implementation that returns the ration between the number of
 * requested opinions and the number of all possible opinion requests.
 * 
 * @author David
 * 
 */
public class DefaultOpinionCost extends AbstractMetric implements OpinionCost {

    @Override
    public double evaluate(List<Integer> agents, List<Integer> services,
	    List<OpinionRequest> opinionRequests) {
	return opinionRequests.size() / (agents.size() - 1d) / agents.size()
		/ services.size();
    }
}
