package testbed.scenario;

import java.util.HashSet;
import java.util.Set;

import testbed.deceptionmodel.Silent;
import testbed.interfaces.Opinion;
import testbed.interfaces.OpinionProviderSelection;
import testbed.interfaces.OpinionRequest;

/**
 * A transitive scenario that allows an agent to select interaction partners.
 * 
 * @author David
 * 
 */
public class TransitiveOpinionProviderSelection extends
	TransitiveInteractionPartnerSelection implements
	OpinionProviderSelection {

    protected Set<OpinionRequest> opinionRequests;

    @Override
    public void initialize(Object... parameters) {
	super.initialize(parameters);
	opinionRequests = null;
    }

    @Override
    public void setOpinionRequests(Set<OpinionRequest> opinionRequests) {
	this.opinionRequests = opinionRequests;
    }

    @Override
    public Set<Opinion> generateOpinions() {
	Set<Opinion> opinions = new HashSet<Opinion>();

	for (OpinionRequest or : opinionRequests) {
	    final int agent1 = or.agent1;
	    final int agent2 = or.agent2;

	    // NOTE: because the scenario has only 1 service, the service
	    // component is ignored

	    if (!(dms[agent1][agent2] instanceof Silent)) {
		final double cap = capabilities.get(agent2);

		double itd = generator.nextDoubleFromUnitTND(cap, sd_o);
		itd = dms[agent1][agent2].calculate(itd);

		final Opinion opinion;
		opinion = new Opinion(agent1, agent2, 0, time, itd);

		opinions.add(opinion);
	    }
	}

	return opinions;
    }

}
