package testbed.trustmodel;

import java.util.HashSet;
import java.util.Set;

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

    protected Set<Integer> agents, services;

    @Override
    public Set<OpinionRequest> getOpinionRequests() {
	final Set<OpinionRequest> opinionRequests = new HashSet<OpinionRequest>();

	for (int agent1 : agents) {
	    for (int agent2 : agents) {
		for (int service : services) {
		    if (agent1 != agent2) {
			opinionRequests.add(new OpinionRequest(agent1, agent2,
				service));
		    }
		}
	    }
	}

	return opinionRequests;
    }

    @Override
    public void setAgents(Set<Integer> agents) {
	this.agents = agents;

    }

    @Override
    public void setServices(Set<Integer> services) {
	this.services = services;
    }

}
