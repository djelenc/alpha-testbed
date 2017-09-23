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
package testbed.core;

import java.util.HashMap;
import java.util.Map;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import testbed.common.DefaultRandomGenerator;
import testbed.interfaces.Metric;
import testbed.interfaces.RandomGenerator;
import testbed.interfaces.Scenario;
import testbed.interfaces.TrustModel;
import testbed.metric.CumulativeNormalizedUtility;
import testbed.metric.DefaultOpinionCost;
import testbed.metric.KendallsTauA;
import testbed.scenario.Transitive;
import testbed.scenario.TransitiveInteractionPartnerSelection;
import testbed.scenario.TransitiveOpinionProviderSelection;
import testbed.trustmodel.Simple;
import testbed.trustmodel.SimpleSelectingInteractionPartners;
import testbed.trustmodel.SimpleSelectingOpinionProviders;

public class AlphaTestbedTest {

    private Map<Metric, Object[]> metrics;
    private TrustModel<?> tm;
    private RandomGenerator tmPRG;
    private Scenario scn;
    private RandomGenerator scnPRG;
    private Object[] scnParams = new Object[] { 100, 0.05, 0.1, 1d, 1d };

    @Before
    public void setup() {
	metrics = new HashMap<Metric, Object[]>();
	metrics.put(new KendallsTauA(), null);
	tmPRG = new DefaultRandomGenerator(0);
	scnPRG = new DefaultRandomGenerator(0);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testInvalidParameters() {
	tm = new Simple();
	scn = new TransitiveOpinionProviderSelection();

	tm.setRandomGenerator(tmPRG);
	scn.setRandomGenerator(scnPRG);

	tm.initialize();
	scn.initialize(scnParams);

	metrics.put(new CumulativeNormalizedUtility(), null);
	metrics.put(new DefaultOpinionCost(), null);

	AlphaTestbed.getProtocol(tm, scn, metrics);
    }

    @Test
    public void testDecisionsModeB() {
	tm = new SimpleSelectingOpinionProviders();
	scn = new TransitiveOpinionProviderSelection();

	tm.setRandomGenerator(tmPRG);
	scn.setRandomGenerator(scnPRG);

	tm.initialize();
	scn.initialize(scnParams);

	metrics.put(new CumulativeNormalizedUtility(), null);
	metrics.put(new DefaultOpinionCost(), null);

	Assert.assertTrue(AlphaTestbed.getProtocol(tm, scn,
		metrics) instanceof DecisionsModeB);
    }

    @Test
    public void testDecisionsModeA() {
	tm = new SimpleSelectingInteractionPartners();
	scn = new TransitiveInteractionPartnerSelection();

	tm.setRandomGenerator(tmPRG);
	scn.setRandomGenerator(scnPRG);

	tm.initialize();
	scn.initialize(scnParams);

	metrics.put(new CumulativeNormalizedUtility(), null);

	Assert.assertTrue(AlphaTestbed.getProtocol(tm, scn,
		metrics) instanceof DecisionsModeA);
    }

    @Test
    public void testNoDecisions() {
	tm = new Simple();
	scn = new Transitive();

	tm.setRandomGenerator(tmPRG);
	scn.setRandomGenerator(scnPRG);

	tm.initialize();
	scn.initialize(scnParams);

	Assert.assertTrue(AlphaTestbed.getProtocol(tm, scn,
		metrics) instanceof NoDecisions);
    }
}
