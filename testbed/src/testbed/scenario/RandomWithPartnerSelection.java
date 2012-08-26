package testbed.scenario;

import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import testbed.interfaces.Experience;
import testbed.interfaces.IParametersPanel;
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

    private Map<Integer, Integer> partners;

    @Override
    public Set<Experience> generateExperiences() {
	final Set<Experience> experiences = new HashSet<Experience>();

	for (int service : getServices()) {
	    final Integer agent = partners.get(service);

	    if (null == agent || !getAgents().contains(agent)) {
		continue;
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
    public void setNextInteractionPartners(Map<Integer, Integer> partners) {
	this.partners = partners;
    }

    @Override
    public String getName() {
	return "Random with partner selection";
    }
    
    @Override
    public IParametersPanel getParametersPanel() {
	return new RandomWithPartnerSelectionGUI();
    }
}
