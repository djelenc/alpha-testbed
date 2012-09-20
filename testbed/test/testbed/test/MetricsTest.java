package testbed.test;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.Map;

import junit.framework.Assert;

import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;

import testbed.interfaces.Metric;
import testbed.interfaces.RankingMetric;
import testbed.interfaces.UtilityMetric;
import testbed.metric.Accuracy;
import testbed.metric.Coverage;
import testbed.metric.CumulativeNormalizedUtility;
import testbed.metric.KendallsTauB;
import testbed.metric.SpearmansFootRule;

public class MetricsTest {

    private Map<Integer, Integer> rnks = null;
    private Map<Integer, Double> cpbs = null;

    @Before
    public void initialize() {
	rnks = new LinkedHashMap<Integer, Integer>();
	cpbs = new LinkedHashMap<Integer, Double>();

	// Available agents: 1, 2, 3, 4
	// Rankings: ID => Rank
	// Capabilities: ID => Capability

	rnks.put(1, 10);
	rnks.put(2, 9);
	rnks.put(3, 8);
	rnks.put(4, 7);
	cpbs.put(1, 1.0);
	cpbs.put(2, 0.9);
	cpbs.put(3, 0.8);
	cpbs.put(4, 0.7);
    }

    @Ignore
    @Test
    public void pairwiseInversionsWithTies() {
	RankingMetric acc = new Accuracy();
	RankingMetric ktb = new KendallsTauB();
	RankingMetric sfr = new SpearmansFootRule();

	cpbs.clear();
	cpbs.put(1, 1d);
	cpbs.put(2, 2d);
	cpbs.put(3, 3d);

	Map<Integer, Integer> trust = new LinkedHashMap<Integer, Integer>();

	ArrayList<Double> listAcc = new ArrayList<Double>();
	ArrayList<Double> listKtb = new ArrayList<Double>();
	ArrayList<Double> listSfr = new ArrayList<Double>();

	for (int i = 1; i <= 3; i++) {
	    for (int j = 1; j <= 3; j++) {
		for (int k = 1; k <= 3; k++) {
		    trust.put(1, i);
		    trust.put(2, j);
		    trust.put(3, k);

		    final double m_acc = acc.evaluate(trust, cpbs);
		    final double m_ktb = ktb.evaluate(trust, cpbs);
		    final double m_sfr = sfr.evaluate(trust, cpbs);

		    if (i != j || j != k) {
			listAcc.add(m_acc);
			listKtb.add(m_ktb);
			listSfr.add(m_sfr);
		    }

		    System.out.printf("123:%d%d%d -> Acc: %.2f, KTB: %.2f\n",
			    i, j, k, m_acc, m_ktb, m_sfr);
		}
	    }
	}

	System.out.println(listAcc);
	System.out.println(listKtb);
	System.out.println(listSfr);
    }

    @Test
    public void pairwiseInversionsNoTies() {
	RankingMetric acc = new Accuracy();
	RankingMetric ktb = new KendallsTauB();

	Map<Integer, Double> trust = new LinkedHashMap<Integer, Double>();

	cpbs.clear();

	trust.clear();
	trust.put(1, 0.1);
	trust.put(2, 0.2);
	trust.put(3, 0.3);

	cpbs.clear();
	cpbs.put(1, 0.00);
	cpbs.put(2, 0.25);
	cpbs.put(3, 0.50);

	Assert.assertEquals(acc.evaluate(trust, cpbs),
		ktb.evaluate(trust, cpbs), 0.001);

	trust.clear();
	trust.put(1, 0.1);
	trust.put(3, 0.2);
	trust.put(2, 0.3);
	Assert.assertEquals(acc.evaluate(trust, cpbs),
		ktb.evaluate(trust, cpbs), 0.001);

	trust.clear();
	trust.put(2, 0.1);
	trust.put(1, 0.2);
	trust.put(3, 0.3);
	Assert.assertEquals(acc.evaluate(trust, cpbs),
		ktb.evaluate(trust, cpbs), 0.001);

	trust.clear();
	trust.put(2, 0.1);
	trust.put(3, 0.2);
	trust.put(1, 0.3);
	Assert.assertEquals(acc.evaluate(trust, cpbs),
		ktb.evaluate(trust, cpbs), 0.001);

	trust.clear();
	trust.put(3, 0.1);
	trust.put(1, 0.2);
	trust.put(2, 0.3);
	Assert.assertEquals(acc.evaluate(trust, cpbs),
		ktb.evaluate(trust, cpbs), 0.001);

	trust.clear();
	trust.put(3, 0.1);
	trust.put(2, 0.2);
	trust.put(1, 0.3);
	Assert.assertEquals(acc.evaluate(trust, cpbs),
		ktb.evaluate(trust, cpbs), 0.001);
    }

    @Test
    public void kendallTauB() {
	RankingMetric taub = new KendallsTauB();
	taub.initialize();

	rnks.clear();
	rnks.put(1, -1);
	rnks.put(2, 1);
	rnks.put(3, 1);
	rnks.put(4, 0);
	rnks.put(5, 1);
	rnks.put(6, 0);
	rnks.put(7, -1);
	rnks.put(8, 1);
	rnks.put(9, 0);
	rnks.put(10, 1);

	cpbs.clear();
	cpbs.put(1, -1d);
	cpbs.put(2, 1d);
	cpbs.put(3, 0d);
	cpbs.put(4, -1d);
	cpbs.put(5, 1d);
	cpbs.put(6, 0d);
	cpbs.put(7, 0d);
	cpbs.put(8, 1d);
	cpbs.put(9, 0d);
	cpbs.put(10, 1d);

	final double score = taub.evaluate(rnks, cpbs) * 2 - 1;

	Assert.assertEquals(0.730251, score, 0.0001);
    }

    @Test
    public void metricsType() {
	Metric utility = new CumulativeNormalizedUtility();
	Metric accuracy = new Accuracy();
	utility.initialize();
	accuracy.initialize();

	Assert.assertTrue(utility instanceof UtilityMetric);
	Assert.assertFalse(utility instanceof RankingMetric);
	Assert.assertTrue(accuracy instanceof RankingMetric);
	Assert.assertFalse(accuracy instanceof UtilityMetric);
    }

    @Test
    public void normalizedUtility() {
	UtilityMetric utility = new CumulativeNormalizedUtility();

	utility.initialize();

	Assert.assertEquals(1, utility.evaluate(cpbs, 1), 0.001); // total 1
	Assert.assertEquals(1, utility.evaluate(cpbs, 1), 0.001); // total 2
	Assert.assertEquals(0.9, utility.evaluate(cpbs, 4), 0.001); // total 2.7
	Assert.assertEquals(0.9, utility.evaluate(cpbs, 2), 0.001); // total 3.6
	Assert.assertEquals(0.88, utility.evaluate(cpbs, 3), 0.001); // total
								     // 4.4
	Assert.assertEquals(0.9, utility.evaluate(cpbs, 1), 0.001); // total 5.4
    }

    @Test
    public void accuracyTheSameRankings() {
	RankingMetric accuracy = new Accuracy();

	rnks.put(1, 1);
	rnks.put(2, 1);
	rnks.put(3, 1);
	rnks.put(4, 1);
	Assert.assertEquals(0.5, accuracy.evaluate(rnks, cpbs), 0.0001);
    }

    @Test
    public void accuracyAllcorrectIncompleteCoverage() {
	RankingMetric accuracy = new Accuracy();
	RankingMetric coverage = new Coverage();

	rnks.remove(3);
	Assert.assertEquals(1.0, accuracy.evaluate(rnks, cpbs), 0.0001);
	Assert.assertEquals(0.75, coverage.evaluate(rnks, cpbs), 0.0001);
    }

    @Test
    public void accuracy2WrongCoverageFull() {
	RankingMetric accuracy = new Accuracy();
	RankingMetric coverage = new Coverage();

	cpbs.put(4, 0.8);
	cpbs.put(3, 0.7);
	Assert.assertEquals(10.0 / 12.0, accuracy.evaluate(rnks, cpbs), 0.0001);
	Assert.assertEquals(1.0, coverage.evaluate(rnks, cpbs), 0.0001);
    }

    @Test
    public void accuracy1Wrong() {
	RankingMetric accuracy = new Accuracy();

	cpbs.put(4, 0.8);
	Assert.assertEquals(11.0 / 12.0, accuracy.evaluate(rnks, cpbs), 0.0001);
    }

    @Test
    public void accuracyAllCorrect() {
	RankingMetric accuracy = new Accuracy();
	Assert.assertEquals(1.0, accuracy.evaluate(rnks, cpbs), 0.0001);
    }

    @Test
    public void evaluatePair() {
	Accuracy accuracy = new Accuracy();

	Assert.assertEquals(1, accuracy.evaluatePair(1, 1, 0.7, 0.7));
	Assert.assertEquals(1, accuracy.evaluatePair(2, 1, 0.7, 0.7));
	Assert.assertEquals(0, accuracy.evaluatePair(1, 2, 0.7, 0.7));
	Assert.assertEquals(1, accuracy.evaluatePair(2, 2, 0.7, 0.7));
	Assert.assertEquals(1, accuracy.evaluatePair(1, 1, 0.9, 0.7));
	Assert.assertEquals(1, accuracy.evaluatePair(2, 1, 0.9, 0.7));
	Assert.assertEquals(0, accuracy.evaluatePair(1, 2, 0.9, 0.7));
	Assert.assertEquals(1, accuracy.evaluatePair(2, 2, 0.9, 0.7));
	Assert.assertEquals(0, accuracy.evaluatePair(1, 1, 0.7, 0.9));
	Assert.assertEquals(0, accuracy.evaluatePair(2, 1, 0.7, 0.9));
	Assert.assertEquals(1, accuracy.evaluatePair(1, 2, 0.7, 0.9));
	Assert.assertEquals(0, accuracy.evaluatePair(2, 2, 0.7, 0.9));
    }

}
