/*******************************************************************************
 * Copyright (c) 2013 David Jelenc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0
 * which accompanies this distribution, and is available at
 * http://www.gnu.org/licenses/gpl.html
 * 
 * Contributors:
 *     David Jelenc - initial API and implementation
 ******************************************************************************/
package testbed.trustmodel;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import testbed.common.PartnerSelectionTemplates;
import testbed.common.Utils;
import testbed.interfaces.ParameterCondition;
import testbed.interfaces.ParametersPanel;
import testbed.interfaces.SelectingInteractionPartners;

/**
 * Trust model on the basis of the {@link EigenTrust} trust model that supports
 * selection partners for interactions.
 * 
 * <p>
 * The proposal contains two partner selection procedures
 * <ol>
 * <li>Selecting the most trustworthy agent as the collaborator
 * <li>Selecting a random agent as the collaborator, where the PMf of the random
 * selection is created from the computed trust values.
 * </ol>
 * 
 * @author David
 * 
 */
public class EigenTrustSelectingInteractionPartners extends EigenTrust
	implements SelectingInteractionPartners {

    protected static final ParameterCondition<Double> VAL_THRESHOLD;
    protected static final ParameterCondition<Boolean> VAL_PROCEDURE;

    static {
	VAL_PROCEDURE = new ParameterCondition<Boolean>() {
	    @Override
	    public void eval(Boolean var) throws IllegalArgumentException {

	    }
	};

	VAL_THRESHOLD = new ParameterCondition<Double>() {
	    @Override
	    public void eval(Double var) throws IllegalArgumentException {
		if (var < 0 || var > 1)
		    throw new IllegalArgumentException(
			    String.format(
				    "The threshold must be a between 0 and 1 inclusively, but was %.2f",
				    var));
	    }
	};
    }

    protected PartnerSelectionTemplates selector;
    protected boolean probSelection;
    protected double zeroThreshold;

    @Override
    public void initialize(Object... params) {
	super.initialize(params);
	selector = new PartnerSelectionTemplates(generator);
	probSelection = Utils.extractParameter(VAL_PROCEDURE,
		params.length - 2, params);
	zeroThreshold = Utils.extractParameter(VAL_THRESHOLD,
		params.length - 1, params);
    }

    @Override
    public Map<Integer, Integer> getInteractionPartners(List<Integer> services) {
	final Map<Integer, Integer> partners = new HashMap<Integer, Integer>();

	for (int service : services) {
	    final Map<Integer, Double> trust = getTrust(service);
	    final Integer bestAgent;

	    if (probSelection) {
		// selecting from peers with trust 0
		if (generator.nextDoubleFromTo(0, 1) < zeroThreshold) {
		    final Map<Integer, Double> uniform = new HashMap<Integer, Double>();
		    int count = 0;

		    // add to map agents all agents with trust 0
		    for (Map.Entry<Integer, Double> e : trust.entrySet()) {
			if (e.getValue() < 0.0001) {
			    uniform.put(e.getKey(), 1d);
			    count++;
			}
		    }

		    if (0 == count) {
			// there are no peers with trust 0
			// we have to select in the usual way
			bestAgent = selector.probabilisticAndPowered(trust, 1d);
			// bestAgent = bestFromWeights(trust);
		    } else {
			// uniform selection amongst agents with trust 0
			bestAgent = selector.probabilisticAndPowered(uniform,
				1d);
			// bestAgent = bestFromWeights(uniform);
		    }
		} else {
		    // selecting according to the probabilities
		    final Integer best = selector.probabilisticAndPowered(
			    trust, 1d);

		    if (null == best) {
			bestAgent = 0;
			System.err.println("Null best agent -- using default.");
		    } else {
			bestAgent = best;
		    }
		}
	    } else {
		bestAgent = selector.maximal(trust);
	    }

	    partners.put(service, bestAgent);
	}

	return partners;
    }

    @Override
    public String toString() {
	return "EigenTrust";
    }

    @Override
    public ParametersPanel getParametersPanel() {
	return new EigenTrustSelectingInteractionPartnersGUI();
    }
}
