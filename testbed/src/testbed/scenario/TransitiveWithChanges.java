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

import testbed.common.Utils;
import testbed.interfaces.ParameterCondition;
import testbed.interfaces.ParametersPanel;

/**
 * Transitive scenario where changes occur at given change intervals. At every
 * change a certain percentage of agents change behavior. Since this class
 * subclasses the {@link Transitive} scenario, it needs the parameters that are
 * required in the {@link Transitive}, plus two additional parameters:
 * <ul>
 * <li>5: (Double) The change density -- the percentage of agents that change at
 * every change interval
 * <li>6: (Double) The change interval -- time between to changes.
 * </ul>
 * 
 * 
 * @author David
 * 
 */
public class TransitiveWithChanges extends Transitive {
    protected static final ParameterCondition<Integer> VAL_INTERVAL;

    static {
	VAL_INTERVAL = new ParameterCondition<Integer>() {
	    @Override
	    public void eval(Integer var) {
		if (var < 0)
		    throw new IllegalArgumentException(String.format(
			    "The change interval must be positive integer, but was %d.",
			    var));
	    }
	};

    }

    /** Time between changes */
    protected int changeInterval;

    /** Percentage of agents that change at each change interval */
    protected double changeDens;

    @Override
    public void initialize(Object... params) {
	super.initialize(params);

	// extract opinion and interaction densities
	changeDens = Utils.extractParameter(VAL_DENS, 5, params);

	// extract opinion and interaction densities
	changeInterval = Utils.extractParameter(VAL_INTERVAL, 6, params);
    }

    @Override
    public ParametersPanel getParametersPanel() {
	return new TransitiveWithChangesGUI();
    }

    @Override
    public void setCurrentTime(int time) {
	this.time = time;

	if (0 == time % changeInterval) {
	    // reassign capabilities
	    int counter = 0;
	    final int changes = (int) Math.round(agents.size() * changeDens);

	    while (counter < changes) {
		final int agent = generator.nextIntFromTo(0, agents.size() - 1);
		capabilities.put(agent, generator.nextDoubleFromTo(0, 1));

		counter += 1;
	    }

	    // reassign deception models
	    dms = assignDeceptionModels(agents, capabilities, opDens);

	    // reassign interaction partners
	    /*
	     * counter = 0; final List<Integer> newPartners = new
	     * ArrayList<Integer>();
	     * 
	     * final List<Integer> remainingAgents = new ArrayList<Integer>();
	     * 
	     * for (int agent : agents) if (!partners.contains(agent))
	     * remainingAgents.add(agent);
	     * 
	     * newPartners.addAll(partners);
	     * 
	     * while (counter < changes) { final int agent =
	     * generator.nextIntFromTo(0, remainingAgents.size() - 1);
	     * 
	     * counter += 1; }
	     */

	    // partners.clear();
	    // partners.addAll(generator.chooseRandom(agents, interDens));
	}
    }

    @Override
    public String toString() {
	return "Transitive with changes";
    }
}
