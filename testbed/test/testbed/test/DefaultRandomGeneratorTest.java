package testbed.test;

import java.util.Map;
import java.util.TreeMap;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import testbed.common.DefaultRandomGenerator;
import testbed.common.LexiographicComparator;
import testbed.deceptionmodel.Complementary;
import testbed.deceptionmodel.RandomDeception;
import testbed.deceptionmodel.Silent;
import testbed.deceptionmodel.Truthful;
import testbed.interfaces.IDeceptionModel;
import testbed.interfaces.IRandomGenerator;

public class DefaultRandomGeneratorTest {

    IRandomGenerator rnd;

    @Before
    public void setUp() {
	rnd = new DefaultRandomGenerator(0);
    }

    @Test
    public void testRandomBorders() {
	double value1, value2;
	for (int i = 0; i < 100; i++) {
	    value1 = rnd.nextDoubleFromUnitTND(0.01, 0.1);
	    value2 = rnd.nextDoubleFromUnitTND(0.99, 0.1);
	    Assert.assertTrue(value1 <= 1 && value1 >= 0);
	    Assert.assertTrue(value2 <= 1 && value2 >= 0);
	}
    }

    @Test
    public void testTNDMean() {
	double case00 = 0, case05 = 0, case10 = 0, caseUnif = 0;
	int iterations = 10000;

	for (int i = 0; i < iterations; i++) {
	    case00 += rnd.nextDoubleFromUnitTND(0.0, 0.1);
	    case05 += rnd.nextDoubleFromUnitTND(0.5, 0.1);
	    case10 += rnd.nextDoubleFromUnitTND(1.0, 0.1);
	    caseUnif += rnd.nextDoubleFromTo(0, 1);
	}

	Assert.assertEquals(0.0797, case00 / iterations, 0.01);
	Assert.assertEquals(0.5000, case05 / iterations, 0.01);
	Assert.assertEquals(0.9202, case10 / iterations, 0.01);
	Assert.assertEquals(0.5000, caseUnif / iterations, 0.01);
    }

    @Test
    public void randomTest() {
	TreeMap<IDeceptionModel, Double> distr = new TreeMap<IDeceptionModel, Double>(
		new LexiographicComparator());
	distr.put(new Truthful(), 0.4);
	distr.put(new Silent(), 0.3);
	distr.put(new Complementary(), 0.2);
	distr.put(new RandomDeception(), 0.1);

	int iterations = 30000;

	TreeMap<IDeceptionModel, Double> emp = new TreeMap<IDeceptionModel, Double>(
		new LexiographicComparator());

	for (int i = 0; i < iterations; i++) {
	    IDeceptionModel dm = rnd.fromWeights(distr);
	    emp.put(dm, (emp.get(dm) == null ? 1d : emp.get(dm) + 1));
	}

	for (Map.Entry<IDeceptionModel, Double> e : emp.entrySet()) {
	    emp.put(e.getKey(), (e.getValue() == null ? 0d : e.getValue()
		    / iterations));
	}

	for (Map.Entry<IDeceptionModel, Double> e : distr.entrySet()) {
	    Assert.assertEquals(e.getValue(), emp.get(e.getKey()), 0.01);
	}
    }

    @Test(expected = IllegalArgumentException.class)
    public void defaultImplementationInvalidProbability() {
	IRandomGenerator rnd1 = new DefaultRandomGenerator(1);

	TreeMap<IDeceptionModel, Double> distr = new TreeMap<IDeceptionModel, Double>(
		new LexiographicComparator());
	distr.put(new Truthful(), -0.4);
	distr.put(new Silent(), 0.3);
	distr.put(new Complementary(), 0.2);

	rnd1.fromWeights(distr);
    }

    @Test(expected = IllegalArgumentException.class)
    public void defaultImplementationInvalidPmf() {
	IRandomGenerator rnd1 = new DefaultRandomGenerator(1);

	TreeMap<IDeceptionModel, Double> distr = new TreeMap<IDeceptionModel, Double>(
		new LexiographicComparator());
	distr.put(new Truthful(), 0.4);
	distr.put(new Silent(), 0.3);
	distr.put(new Complementary(), 0.2);

	rnd1.fromWeights(distr);
    }

    @Test
    public void defaultImplementation() {
	IRandomGenerator rnd1 = new DefaultRandomGenerator(1);
	IRandomGenerator rnd2 = new DefaultRandomGenerator(1);

	TreeMap<IDeceptionModel, Double> distr = new TreeMap<IDeceptionModel, Double>(
		new LexiographicComparator());
	distr.put(new Truthful(), 0.4);
	distr.put(new Silent(), 0.3);
	distr.put(new Complementary(), 0.2);
	distr.put(new RandomDeception(), 0.1);

	for (int i = 0; i < 1000; i++) {
	    Assert.assertEquals(rnd1.nextDoubleFromUnitTND(0.75, 0.05),
		    rnd2.nextDoubleFromUnitTND(0.75, 0.05), 0.0000001);
	    Assert.assertEquals(rnd1.nextDoubleFromTo(0, 1), rnd2.nextDoubleFromTo(0, 1),
		    0.0000001);
	    Assert.assertEquals(rnd1.nextIntFromTo(5, 10),
		    rnd2.nextIntFromTo(5, 10));
	    Assert.assertEquals(rnd1.fromWeights(distr),
		    rnd2.fromWeights(distr));
	}

    }
}
