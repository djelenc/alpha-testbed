package testbed.test;

import java.util.TreeMap;

import org.junit.Assert;
import org.junit.Test;

import testbed.common.DefaultRandomNumberGenerator;
import testbed.common.LexiographicComparator;
import testbed.deceptionmodel.Complementary;
import testbed.deceptionmodel.Random;
import testbed.deceptionmodel.Silent;
import testbed.deceptionmodel.Truthful;
import testbed.interfaces.IDeceptionModel;
import testbed.interfaces.IRandomNumberGenerator;

public class RandomNumberImplementationsTest {

    @Test(expected = IllegalArgumentException.class)
    public void defaultImplementationInvalidProbability() {
	IRandomNumberGenerator rnd1 = new DefaultRandomNumberGenerator(1);

	TreeMap<IDeceptionModel, Double> distr = new TreeMap<IDeceptionModel, Double>(
		new LexiographicComparator());
	distr.put(new Truthful(), -0.4);
	distr.put(new Silent(), 0.3);
	distr.put(new Complementary(), 0.2);

	rnd1.randomFromWeights(distr);
    }

    @Test(expected = IllegalArgumentException.class)
    public void defaultImplementationInvalidPmf() {
	IRandomNumberGenerator rnd1 = new DefaultRandomNumberGenerator(1);

	TreeMap<IDeceptionModel, Double> distr = new TreeMap<IDeceptionModel, Double>(
		new LexiographicComparator());
	distr.put(new Truthful(), 0.4);
	distr.put(new Silent(), 0.3);
	distr.put(new Complementary(), 0.2);

	rnd1.randomFromWeights(distr);
    }

    @Test
    public void defaultImplementation() {
	IRandomNumberGenerator rnd1 = new DefaultRandomNumberGenerator(1);
	IRandomNumberGenerator rnd2 = new DefaultRandomNumberGenerator(1);

	TreeMap<IDeceptionModel, Double> distr = new TreeMap<IDeceptionModel, Double>(
		new LexiographicComparator());
	distr.put(new Truthful(), 0.4);
	distr.put(new Silent(), 0.3);
	distr.put(new Complementary(), 0.2);
	distr.put(new Random(), 0.1);

	for (int i = 0; i < 1000; i++) {
	    Assert.assertEquals(rnd1.randomTND(0.75, 0.05),
		    rnd2.randomTND(0.75, 0.05), 0.0000001);
	    Assert.assertEquals(rnd1.randomUnif(0, 1), rnd2.randomUnif(0, 1),
		    0.0000001);
	    Assert.assertEquals(rnd1.randomUnifIndex(5, 10),
		    rnd2.randomUnifIndex(5, 10));
	    Assert.assertEquals(rnd1.randomFromWeights(distr),
		    rnd2.randomFromWeights(distr));
	}

    }
}
