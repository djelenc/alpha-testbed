package testbed.trustmodel;

import java.util.Set;
import java.util.TreeSet;

import testbed.interfaces.OpinionRequest;
import testbed.interfaces.SelectingOpinionProviders;

/**
 * Trust model on the basis of the {@link Simple} that supports selection
 * partners for interactions and selection opinion providers.
 * 
 * @author David
 * 
 */
public class SimpleSelectingOpinionProviders extends
	SimpleSelectingInteractionPartners implements SelectingOpinionProviders {

    protected Set<Integer> agents;

    @Override
    public void initialize(Object... params) {
	super.initialize(params);
    }

    @Override
    public Set<OpinionRequest> getOpinionRequests() {
	final Set<OpinionRequest> opinionRequests = new TreeSet<OpinionRequest>();

	for (int target : agents) {
	    // ask only if there are less than 3 experiences
	    if (exCnt[target] < 3) {
		for (int source : agents) {
		    if (target != source) {
			opinionRequests.add(new OpinionRequest(source, target,
				0));
		    }

		}
	    }
	}

	return opinionRequests;
    }

    @Override
    public void setAgents(Set<Integer> agents) {
	this.agents = agents;
	int max = Integer.MIN_VALUE;

	for (int ag : agents) {
	    if (max < ag)
		max = ag;
	}

	if (max > exSum.length - 1) {
	    expandArrays(max);
	}
    }

    @Override
    public void setServices(Set<Integer> services) {

    }

    @Override
    public String toString() {
	return "Simple with opinion selection";
    }

}
