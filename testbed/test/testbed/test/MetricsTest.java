package testbed.test;

import java.util.LinkedHashMap;
import java.util.Map;

import junit.framework.Assert;

import org.junit.Before;
import org.junit.Test;

import testbed.interfaces.IMetric;
import testbed.interfaces.IRankingMetric;
import testbed.interfaces.IUtilityMetric;
import testbed.metric.Accuracy;
import testbed.metric.Coverage;
import testbed.metric.CumulativeNormalizedUtility;

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

	rnks.put(1, 1);
	rnks.put(2, 2);
	rnks.put(3, 3);
	rnks.put(4, 4);
	cpbs.put(1, 1.0);
	cpbs.put(2, 0.9);
	cpbs.put(3, 0.8);
	cpbs.put(4, 0.7);
    }

    @Test
    public void metricsType() {
	IMetric utility = new CumulativeNormalizedUtility();
	IMetric accuracy = new Accuracy();
	utility.initialize();
	accuracy.initialize();

	Assert.assertTrue(utility instanceof IUtilityMetric);
	Assert.assertFalse(utility instanceof IRankingMetric);
	Assert.assertTrue(accuracy instanceof IRankingMetric);
	Assert.assertFalse(accuracy instanceof IUtilityMetric);
    }

    @Test
    public void normalizedUtility() {
	IUtilityMetric utility = new CumulativeNormalizedUtility();

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
	IRankingMetric accuracy = new Accuracy();

	rnks.put(1, 1);
	rnks.put(2, 1);
	rnks.put(3, 1);
	rnks.put(4, 1);
	Assert.assertEquals(0.5, accuracy.evaluate(rnks, cpbs), 0.0001);
    }

    @Test
    public void accuracyAllcorrectIncompleteCoverage() {
	IRankingMetric accuracy = new Accuracy();
	IRankingMetric coverage = new Coverage();

	rnks.remove(3);
	Assert.assertEquals(1.0, accuracy.evaluate(rnks, cpbs), 0.0001);
	Assert.assertEquals(0.75, coverage.evaluate(rnks, cpbs), 0.0001);
    }

    @Test
    public void accuracy2WrongCoverageFull() {
	IRankingMetric accuracy = new Accuracy();
	IRankingMetric coverage = new Coverage();

	cpbs.put(4, 0.8);
	cpbs.put(3, 0.7);
	Assert.assertEquals(10.0 / 12.0, accuracy.evaluate(rnks, cpbs), 0.0001);
	Assert.assertEquals(1.0, coverage.evaluate(rnks, cpbs), 0.0001);
    }

    @Test
    public void accuracy1Wrong() {
	IRankingMetric accuracy = new Accuracy();

	cpbs.put(4, 0.8);
	Assert.assertEquals(11.0 / 12.0, accuracy.evaluate(rnks, cpbs), 0.0001);
    }

    @Test
    public void accuracyAllCorrect() {
	IRankingMetric accuracy = new Accuracy();
	Assert.assertEquals(1.0, accuracy.evaluate(rnks, cpbs), 0.0001);
    }

    @Test
    public void evaluatePair() {
	Accuracy accuracy = new Accuracy();

	Assert.assertEquals(1, accuracy.evaluatePair(1, 1, 0.7, 0.7));
	Assert.assertEquals(1, accuracy.evaluatePair(1, 2, 0.7, 0.7));
	Assert.assertEquals(0, accuracy.evaluatePair(2, 1, 0.7, 0.7));
	Assert.assertEquals(1, accuracy.evaluatePair(2, 2, 0.7, 0.7));
	Assert.assertEquals(1, accuracy.evaluatePair(1, 1, 0.9, 0.7));
	Assert.assertEquals(1, accuracy.evaluatePair(1, 2, 0.9, 0.7));
	Assert.assertEquals(0, accuracy.evaluatePair(2, 1, 0.9, 0.7));
	Assert.assertEquals(1, accuracy.evaluatePair(2, 2, 0.9, 0.7));
	Assert.assertEquals(0, accuracy.evaluatePair(1, 1, 0.7, 0.9));
	Assert.assertEquals(0, accuracy.evaluatePair(1, 2, 0.7, 0.9));
	Assert.assertEquals(1, accuracy.evaluatePair(2, 1, 0.7, 0.9));
	Assert.assertEquals(0, accuracy.evaluatePair(2, 2, 0.7, 0.9));
    }

}
