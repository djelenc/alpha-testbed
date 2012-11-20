package testbed.test;

import java.util.HashMap;
import java.util.Map;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import testbed.DecisionsModeA;
import testbed.EvaluationProtocol;
import testbed.NoDecisions;
import testbed.common.DefaultRandomGenerator;
import testbed.interfaces.Metric;
import testbed.interfaces.Scenario;
import testbed.interfaces.TrustModel;
import testbed.metric.CumulativeNormalizedUtility;
import testbed.metric.KendallsTauA;
import testbed.scenario.Transitive;
import testbed.scenario.TransitiveInteractionPartnerSelection;
import testbed.trustmodel.YuSinghSycara;
import testbed.trustmodel.YuSinghSycaraSelectingInteractionPartners;

public class EvaluationProtocolTests {

    EvaluationProtocol epND, epA;
    TrustModel<?> tm, tmdm;
    Scenario scn, scndm;
    Metric acc, cnu;
    Map<Metric, Object[]> mtrcs;

    @Before
    public void setUp() {
	epND = new NoDecisions();
	epA = new DecisionsModeA();

	tm = new YuSinghSycara();
	tmdm = new YuSinghSycaraSelectingInteractionPartners();

	scn = new Transitive();
	scndm = new TransitiveInteractionPartnerSelection();

	acc = new KendallsTauA();
	cnu = new CumulativeNormalizedUtility();
	mtrcs = new HashMap<Metric, Object[]>();

	tm.setRandomGenerator(new DefaultRandomGenerator(0));
	tmdm.setRandomGenerator(new DefaultRandomGenerator(0));

	tm.initialize();
	tmdm.initialize();

	scn.setRandomGenerator(new DefaultRandomGenerator(0));
	scndm.setRandomGenerator(new DefaultRandomGenerator(0));

	scn.initialize(100, 0.05, 0.1, 1d, 1d);
	scndm.initialize(100, 0.05, 0.1, 1d, 1d);

	mtrcs.put(acc, null);
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
