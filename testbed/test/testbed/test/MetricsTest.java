package testbed.test;

import java.util.LinkedHashMap;
import java.util.Map;

import junit.framework.Assert;

import org.junit.Before;
import org.junit.Test;

import testbed.interfaces.IMetric;
import testbed.metric.Accuracy;
import testbed.metric.Coverage;

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
    public void accuracyTheSameRankings() {
	IMetric accuracy = new Accuracy();

	rnks.put(1, 1);
	rnks.put(2, 1);
	rnks.put(3, 1);
	rnks.put(4, 1);
	Assert.assertEquals(0.5, accuracy.evaluate(rnks, cpbs), 0.0001);
    }

    @Test
    public void accuracyAllcorrectIncompleteCoverage() {
	IMetric accuracy = new Accuracy();
	IMetric coverage = new Coverage();

	rnks.remove(3);
	Assert.assertEquals(1.0, accuracy.evaluate(rnks, cpbs), 0.0001);
	Assert.assertEquals(0.75, coverage.evaluate(rnks, cpbs), 0.0001);
    }

    @Test
    public void accuracy2WrongCoverageFull() {
	IMetric accuracy = new Accuracy();
	IMetric coverage = new Coverage();

	cpbs.put(4, 0.8);
	cpbs.put(3, 0.7);
	Assert.assertEquals(10.0 / 12.0, accuracy.evaluate(rnks, cpbs), 0.0001);
	Assert.assertEquals(1.0, coverage.evaluate(rnks, cpbs), 0.0001);
    }

    @Test
    public void accuracy1Wrong() {
	IMetric accuracy = new Accuracy();

	cpbs.put(4, 0.8);
	Assert.assertEquals(11.0 / 12.0, accuracy.evaluate(rnks, cpbs), 0.0001);
    }

    @Test
    public void accuracyAllCorrect() {
	IMetric accuracy = new Accuracy();
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
