package testbed.test;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.TreeMap;

import org.junit.Assert;
import org.junit.Test;

import testbed.common.LexiographicComparator;
import testbed.common.Utils;
import testbed.deceptionmodel.Complementary;
import testbed.deceptionmodel.Random;
import testbed.deceptionmodel.Silent;
import testbed.deceptionmodel.Truthful;
import testbed.interfaces.ICondition;
import testbed.interfaces.IDeceptionModel;

public class UtilsTest {

    @Test
    public void orderedMapTest() {
	Map<Integer, Integer> map = new LinkedHashMap<Integer, Integer>();
	map.put(3, 10);
	map.put(4, 10);
	map.put(1, 30);
	map.put(2, 50);
	map.put(5, 30);

	Map<Integer, Integer> map2 = Utils.orderedMap(map);

	int prev = Integer.MIN_VALUE;

	for (Map.Entry<Integer, Integer> entry : map2.entrySet()) {
	    Assert.assertTrue(prev < entry.getKey());
	    prev = entry.getKey();
	}
    }

    @Test
    public void randomTest() {
	TreeMap<IDeceptionModel, Double> distr = new TreeMap<IDeceptionModel, Double>(
		new LexiographicComparator());
	distr.put(new Truthful(), 0.4);
	distr.put(new Silent(), 0.3);
	distr.put(new Complementary(), 0.2);
	distr.put(new Random(), 0.1);

	int iterations = 30000;

	TreeMap<IDeceptionModel, Double> emp = new TreeMap<IDeceptionModel, Double>(
		new LexiographicComparator());

	for (int i = 0; i < iterations; i++) {
	    IDeceptionModel dm = Utils.randomFromWeights(distr);
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

    @Test
    public void extractParameters() {
	ICondition<Double> cond = new ICondition<Double>() {
	    @Override
	    public void eval(Double var) {
		if (var < 1d)
		    throw new IllegalArgumentException(String.format(
			    "Var should be >=1, but was: %.2f", var));
	    }
	};

	Object[] params = new Object[] { 1d, "aa", 0d, 1, null };
	double var = Utils.extractParameter(cond, 0, params);

	Assert.assertEquals(1d, var, 0.01);

	try {
	    Utils.extractParameter(cond, 4, params);
	    Assert.fail();
	} catch (Exception e) {
	    Assert.assertTrue(true);
	}

	try {
	    Utils.extractParameter(cond, 10, params);
	    Assert.fail();
	} catch (Exception e) {
	    Assert.assertTrue(true);
	}

	try {
	    Utils.extractParameter(cond, -1, params);
	    Assert.fail();
	} catch (Exception e) {
	    Assert.assertTrue(true);
	}

	try {
	    Utils.extractParameter(cond, 1, params);
	    Assert.fail();
	} catch (Exception e) {
	    Assert.assertTrue(true);
	}

	try {
	    Utils.extractParameter(cond, 2, params);
	    Assert.fail();
	} catch (Exception e) {
	    Assert.assertTrue(true);
	}
    }

    @Test
    public void testRandomBorders() {
	double value1, value2;
	for (int i = 0; i < 100; i++) {
	    value1 = Utils.randomTND(0.01, 0.1);
	    value2 = Utils.randomTND(0.99, 0.1);
	    Assert.assertTrue(value1 <= 1 && value1 >= 0);
	    Assert.assertTrue(value2 <= 1 && value2 >= 0);
	}
    }

    @Test
    public void testTNDMean() {
	double case00 = 0, case05 = 0, case10 = 0, caseUnif = 0;
	int iterations = 10000;

	for (int i = 0; i < iterations; i++) {
	    case00 += Utils.randomTND(0.0, 0.1);
	    case05 += Utils.randomTND(0.5, 0.1);
	    case10 += Utils.randomTND(1.0, 0.1);
	    caseUnif += Utils.randomUnif(0, 1);
	}

	Assert.assertEquals(0.0797, case00 / iterations, 0.01);
	Assert.assertEquals(0.5000, case05 / iterations, 0.01);
	Assert.assertEquals(0.9202, case10 / iterations, 0.01);
	Assert.assertEquals(0.5000, caseUnif / iterations, 0.01);
    }
}
