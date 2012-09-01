package testbed.scenario;

import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import testbed.interfaces.Experience;
import testbed.interfaces.IPartnerSelection;

/**
 * An extension of the {@link RandomMultiService} scenario, where agent Alpha is
 * required to select interaction partners.
 * 
 * @author David
 * 
 */
public class RandomMultiServiceWithPartnerSelection extends RandomMultiService
	implements IPartnerSelection {

    protected Map<Integer, Integer> partners = null;

    @Override
    public void initialize(Object... parameters) {
	super.initialize(parameters);
	partners = null;
    }

    @Override
    public Set<Experience> generateExperiences() {
	final Set<Experience> experiences = new HashSet<Experience>();

	for (int service : getServices()) {
	    final Integer agent = partners.get(service);

	    if (null == agent || !getAgents().contains(agent))
		continue;

	    final int key = (numAgentsLarger ? pivot * agent + service : pivot
		    * service + agent);

	    // generate interaction outcome
	    final double cap = capabilities.get(key);
	    final double outcome = generator.nextDoubleFromUnitTND(cap, sd_i);

	    // create experience tuple and add it to list
	    final Experience experience;
	    experience = new Experience(agent, service, time, outcome);
	    experiences.add(experience);
	}

	return experiences;
    }

    @Override
    public void setNextInteractionPartners(Map<Integer, Integer> partners) {
	this.partners = partners;
    }

    @Override
    public String toString() {
	return "Random/multi-service with partner selection";
    }
}
