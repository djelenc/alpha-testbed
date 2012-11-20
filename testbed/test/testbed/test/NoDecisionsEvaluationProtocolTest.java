package testbed.test;

import java.util.HashMap;
import java.util.Map;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import testbed.EvaluationProtocol;
import testbed.NoDecisionsEvaluationProtocol;
import testbed.common.DefaultRandomGenerator;
import testbed.interfaces.Accuracy;
import testbed.interfaces.Metric;
import testbed.interfaces.Scenario;
import testbed.interfaces.TrustModel;
import testbed.metric.CumulativeNormalizedUtility;
import testbed.metric.KendallsTauA;
import testbed.scenario.Transitive;
import testbed.scenario.TransitiveInteractionPartnerSelection;
import testbed.trustmodel.YuSinghSycara;
import testbed.trustmodel.YuSinghSycaraSelectingInteractionPartners;

public class NoDecisionsEvaluationProtocolTest {

    EvaluationProtocol epND;
    TrustModel<?> tm;
    Scenario scn;
    Accuracy acc;
    Map<Metric, Object[]> mtrcs;

    @Before
    public void setUp() {
	epND = new NoDecisionsEvaluationProtocol();
	tm = new YuSinghSycara();
	scn = new Transitive();
	acc = new KendallsTauA();
	mtrcs = new HashMap<Metric, Object[]>();

	tm.initialize();
	scn.setRandomGenerator(new DefaultRandomGenerator(0));
	scn.initialize(100, 0.05, 0.1, 1d, 1d);

	mtrcs.put(acc, new Object[] {});
    }

    @Test
    public void testInitialize() {
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
