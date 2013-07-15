package testbed.core;

import java.util.HashMap;
import java.util.Map;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import testbed.common.DefaultRandomGenerator;
import testbed.core.DecisionsModeA;
import testbed.core.DecisionsModeB;
import testbed.core.EvaluationProtocol;
import testbed.core.NoDecisions;
import testbed.interfaces.Metric;
import testbed.interfaces.Scenario;
import testbed.interfaces.TrustModel;
import testbed.metric.CumulativeNormalizedUtility;
import testbed.metric.DefaultOpinionCost;
import testbed.metric.KendallsTauA;
import testbed.scenario.Transitive;
import testbed.scenario.TransitiveInteractionPartnerSelection;
import testbed.scenario.TransitiveOpinionProviderSelection;
import testbed.trustmodel.SimpleSelectingOpinionProviders;
import testbed.trustmodel.YuSinghSycara;
import testbed.trustmodel.YuSinghSycaraSelectingInteractionPartners;

public class EvaluationProtocolTests {

    EvaluationProtocol epND, epA, epB;
    TrustModel<?> tm, tmdm, tmdm2;
    Scenario scn, scndm, scndm2;
    Metric acc, cnu, oc;
    Map<Metric, Object[]> mtrcs;

    @Before
    public void setUp() {
	epND = new NoDecisions();
	epA = new DecisionsModeA();
	epB = new DecisionsModeB();

	tm = new YuSinghSycara();
	tmdm = new YuSinghSycaraSelectingInteractionPartners();
	tmdm2 = new SimpleSelectingOpinionProviders();

	scn = new Transitive();
	scndm = new TransitiveInteractionPartnerSelection();
	scndm2 = new TransitiveOpinionProviderSelection();

	acc = new KendallsTauA();
	cnu = new CumulativeNormalizedUtility();
	oc = new DefaultOpinionCost();

	mtrcs = new HashMap<Metric, Object[]>();

	tm.setRandomGenerator(new DefaultRandomGenerator(0));
	tmdm.setRandomGenerator(new DefaultRandomGenerator(0));
	tmdm2.setRandomGenerator(new DefaultRandomGenerator(0));

	tm.initialize();
	tmdm.initialize();
	tmdm2.initialize();

	scn.setRandomGenerator(new DefaultRandomGenerator(0));
	scndm.setRandomGenerator(new DefaultRandomGenerator(0));
	scndm2.setRandomGenerator(new DefaultRandomGenerator(0));

	scn.initialize(100, 0.05, 0.1, 1d, 1d);
	scndm.initialize(100, 0.05, 0.1, 1d, 1d);
	scndm2.initialize(100, 0.05, 0.1, 1d, 1d);

	mtrcs.put(acc, null);
    }

    @Test
    public void testInitializeModeB() {
	mtrcs.put(cnu, null);
	Assert.assertFalse(epB.validParameters(tmdm2, scndm2, mtrcs));
	mtrcs.put(oc, null);
	epB.initialize(tmdm2, scndm2, mtrcs);
    }

    @Test
    public void testInitializeModeA() {
	Assert.assertFalse(epA.validParameters(tmdm, scndm, mtrcs));
	mtrcs.put(cnu, null);
	epA.initialize(tmdm, scndm, mtrcs);
    }

    @Test
    public void testInitializeNoDM() {
	Assert.assertTrue(epND.validParameters(tm, scn, mtrcs));

	epND.initialize(tm, scn, mtrcs);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testInitializeWrongTM() {
	tm = new YuSinghSycaraSelectingInteractionPartners();

	Assert.assertFalse(epND.validParameters(tm, scn, mtrcs));
	epND.initialize(tm, scn, mtrcs);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testInitializeWrongScn() {
	scn = new TransitiveInteractionPartnerSelection();

	Assert.assertFalse(epND.validParameters(tm, scn, mtrcs));
	epND.initialize(tm, scn, mtrcs);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testInitializeWrongMetrics() {
	mtrcs.clear();
	mtrcs.put(new CumulativeNormalizedUtility(), new Object[0]);

	Assert.assertFalse(epND.validParameters(tm, scn, mtrcs));
	epND.initialize(tm, scn, mtrcs);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testInitializeTooManyMetrics() {
	mtrcs.put(new CumulativeNormalizedUtility(), new Object[0]);

	Assert.assertFalse(epND.validParameters(tm, scn, mtrcs));
	epND.initialize(tm, scn, mtrcs);
    }
}
