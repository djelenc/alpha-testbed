package testbed.metric;

import java.util.Set;

import testbed.interfaces.OpinionCost;
import testbed.interfaces.OpinionRequest;

public class DefaultOpinionCost extends AbstractMetric implements OpinionCost {

    @Override
    public double evaluate(Set<Integer> agents, Set<Integer> services,
	    Set<OpinionRequest> opinionRequests) {
	return opinionRequests.size();
    }

}
