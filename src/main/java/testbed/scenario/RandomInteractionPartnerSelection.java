/*
 * Copyright (c) 2013 David Jelenc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0
 * which accompanies this distribution, and is available at
 * http://www.gnu.org/licenses/gpl.html
 * 
 * Contributors:
 *     David Jelenc - initial API and implementation
 */
package testbed.scenario;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import testbed.interfaces.Experience;
import testbed.interfaces.InteractionPartnerSelection;
import testbed.interfaces.ParametersPanel;

/**
 * An extension of the {@link Random} scenario, where agent Alpha is required to
 * select interaction partners.
 * 
 * @author David
 * 
 */
public class RandomInteractionPartnerSelection extends Random
	implements InteractionPartnerSelection {

    private Map<Integer, Integer> partners;

    @Override
    public List<Experience> generateExperiences() {
	final List<Experience> experiences = new ArrayList<Experience>();

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
    public void setInteractionPartners(Map<Integer, Integer> partners) {
	this.partners = partners;
    }

    @Override
    public String toString() {
	return "Random with partner selection";
    }

    @Override
    public ParametersPanel getParametersPanel() {
	return new RandomInteractionPartnerSelectionGUI();
    }
}
