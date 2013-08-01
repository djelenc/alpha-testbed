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
package testbed.core;

import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import testbed.interfaces.Accuracy;
import testbed.interfaces.InteractionPartnerSelection;
import testbed.interfaces.Metric;
import testbed.interfaces.OpinionCost;
import testbed.interfaces.OpinionProviderSelection;
import testbed.interfaces.Scenario;
import testbed.interfaces.SelectingInteractionPartners;
import testbed.interfaces.SelectingOpinionProviders;
import testbed.interfaces.TrustModel;
import testbed.interfaces.Utility;

/**
 * The factory class for creating {@link EvaluationProtocol} instances. This
 * class should be modified for additional {@link EvaluationProtocol}
 * implementations.
 * 
 * <p>
 * Its only method {@link AlphaTestbed#getProtocol} takes an instance of a
 * {@link TrustModel}, an instance of a {@link Scenario} and a map, where keys
 * are instances of metrics instances and values are their initialization
 * parameters and their initialization parameters.
 * 
 * <p>
 * The method returns the suitable {@link EvaluationProtocol} instance or an
 * {@link IllegalArgumentException} if an invalid combination of parameters is
 * given.
 * 
 * @author David
 * 
 */
public class AlphaTestbed {
    /** No suitable protocol error */
    private static final String NO_PROTOCOL_ERR;

    /** Instantiation error */
    private static final String INSTATIATION_ERR;

    /** Set of available evaluation protocols */
    public static final Set<Class<? extends EvaluationProtocol>> EVALUATION_PROTOCOLS;

    /** Set of available trust model interfaces */
    public static final Set<Class<?>> TRUST_MODELS;

    /** Set of available scenario interfaces */
    public static final Set<Class<?>> SCENARIOS;

    /** Set of available metric interfaces */
    public static final Set<Class<? extends Metric>> METRICS;

    static {
	NO_PROTOCOL_ERR = "Could not find a suitable protocol "
		+ "for trust model %s, scenario %s and metrics %s.";

	INSTATIATION_ERR = "Could not instatiate evaluation protocol "
		+ "for trust model %s, scenario %s and metrics %s.";

	EVALUATION_PROTOCOLS = new HashSet<Class<? extends EvaluationProtocol>>();
	TRUST_MODELS = new HashSet<Class<?>>();
	SCENARIOS = new HashSet<Class<?>>();
	METRICS = new HashSet<Class<? extends Metric>>();

	// XXX: MODIFY IF NEW ELEMENTS ARE ADDED

	// ALL AVAILABLE EVALUATION PROTOCOLS
	EVALUATION_PROTOCOLS.add(NoDecisions.class);
	EVALUATION_PROTOCOLS.add(DecisionsModeA.class);
	EVALUATION_PROTOCOLS.add(DecisionsModeB.class);

	// ALL AVAILABLE TRUST MODEL INTERFACES
	TRUST_MODELS.add(TrustModel.class);
	TRUST_MODELS.add(SelectingInteractionPartners.class);
	TRUST_MODELS.add(SelectingOpinionProviders.class);

	// ALL AVAILABLE SCENARIO INTERFACES
	SCENARIOS.add(Scenario.class);
	SCENARIOS.add(InteractionPartnerSelection.class);
	SCENARIOS.add(OpinionProviderSelection.class);

	// ALL AVAILABLE METRIC INTERFACES
	METRICS.add(Accuracy.class);
	METRICS.add(Utility.class);
	METRICS.add(OpinionCost.class);
    }

    /**
     * Returns an evaluation protocol instance that corresponds with the given
     * combination of a trust model, a scenario and the given set of metrics.
     * 
     * @param tm
     *            Initialized instance of a trust model
     * @param scn
     *            Initialized instance of a scenario
     * @param metrics
     *            Map of Metric instances and their initialization parameters.
     * @return Evaluation protocol instance or {@link IllegalArgumentException}
     *         of no suitable protocol found.
     */
    public static EvaluationProtocol getProtocol(TrustModel<?> tm,
	    Scenario scn, Map<? extends Metric, Object[]> metrics) {

	for (Class<? extends EvaluationProtocol> clazz : EVALUATION_PROTOCOLS) {
	    try {
		final EvaluationProtocol instance = clazz.newInstance();

		if (instance.validParameters(tm, scn, metrics)) {
		    instance.initialize(tm, scn, metrics);
		    return instance;
		}
	    } catch (Exception e) {
		throw new IllegalArgumentException(String.format(
			INSTATIATION_ERR, tm, scn, metrics), e);
	    }
	}

	throw new IllegalArgumentException(String.format(NO_PROTOCOL_ERR, tm,
		scn, metrics));
    }
}
