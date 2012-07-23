package testbed.scenario;

import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import testbed.common.Utils;
import testbed.interfaces.Experience;
import testbed.interfaces.IPartnerSelection;

/**
 * An extension of the {@link Random} scenario, where agent Alpha is required to
 * select interaction partners.
 * 
 * @author David
 * 
 */
public class RandomWithPartnerSelection extends Random implements
	IPartnerSelection {

    private static final String PARTNER_NOT_SET = "No partner set for service %d.";
    private Map<Integer, Integer> partners;

    @Override
    public Set<Experience> generateExperiences() {
	final Set<Experience> experiences = new HashSet<Experience>();

	for (int service : SERVICES) {
	    final Integer agent = partners.get(service);

	    if (null == agent) {
		throw new IllegalArgumentException(String.format(
			PARTNER_NOT_SET, service));
	    }

	    // generate interaction outcome
	    final double cap = capabilities.get(agent);
	    final double outcome = Utils.randomTND(cap, sd_i);

	    // create experience tuple and add it to list
	    final Experience experience = new Experience(agent, service, time,
		    outcome);
	    experiences.add(experience);
	}

	return experiences;
    }

    @Override
    public void setNextInteractionPartners(Map<Integer, Integer> partners) {
	this.partners = partners;
    }

    @Override
    public String getName() {
	return "Random with partner selection";
    }
}
