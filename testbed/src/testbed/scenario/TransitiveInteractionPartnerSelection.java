package testbed.scenario;

import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import testbed.interfaces.Experience;
import testbed.interfaces.ParametersPanel;
import testbed.interfaces.InteractionPartnerSelection;

/**
 * A transitive scenario that allows an agent to select interaction partners.
 * 
 * @author David
 * 
 */
public class TransitiveInteractionPartnerSelection extends Transitive implements
	InteractionPartnerSelection {

    protected static final String PARTNER_NOT_SET = "No partner set for service %d.";
    protected Map<Integer, Integer> partners;

    @Override
    public Set<Experience> generateExperiences() {
	final Set<Experience> experiences = new HashSet<Experience>();

	for (int service : getServices()) {
	    final Integer agent = partners.get(service);

	    if (null == agent) {
		throw new IllegalArgumentException(String.format(
			PARTNER_NOT_SET, service));
	    }

	    // generate interaction outcome
	    final double cap = capabilities.get(agent);
	    final double outcome = generator.nextDoubleFromUnitTND(cap, sd_i);

	    // create experience tuple and add it to list
	    final Experience experience;
	    experience = new Experience(agent, service, time, outcome);
	    experiences.add(experience);
	}

	return experiences;
    }

    @Override
    public void setInteractionPartners(Map<Integer, Integer> partners) {
	this.partners = partners;
    }

    @Override
    public String toString() {
	return "Transitive with partner selection";
    }

    @Override
    public ParametersPanel getParametersPanel() {
	return new TransitiveInteractionPartnerSelectionGUI();
    }
}
