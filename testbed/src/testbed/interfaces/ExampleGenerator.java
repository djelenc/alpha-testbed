package testbed.interfaces;

import java.util.Map;
import java.util.Random;
import java.util.TreeMap;

import testbed.interfaces.IRandomGenerator;

public class ExampleGenerator implements IRandomGenerator {
    private static final String TOTAL_PROBABILIT_EX = "Total probabilit in pmf %s does not sum to %.2f, but is %.2f";
    private static final String INVALID_PROBABILITY_EX = "Invalid probability %.2f of element %s in pmf %s.";
    private static final String UNREACHABLE_CODE = "This part of code should be unreachable.";

    private final int seed;

    private final Random random;

    public ExampleGenerator(int seed) {
	this.seed = seed;
	this.random = new Random(seed);
    }

    @Override
    public double randomTND(double mean, double sd) {
	double number;

	do {
	    number = mean + random.nextGaussian() * sd;
	} while (number > 1 || number < 0);

	return number;
    }

    @Override
    public double randomUnif(double min, double max) {
	return random.nextDouble() * (max - min) - min;
    }

    @Override
    public int randomUnifIndex(int min, int max) {
	return min + random.nextInt(max + 1);
    }

    @Override
    public <T> T randomFromWeights(TreeMap<T, Double> pmf) {
	if (pmf == null || pmf.isEmpty()) {
	    return null;
	}

	double totalProbability = 0;
	for (Map.Entry<T, Double> e : pmf.entrySet()) {
	    final double probability = e.getValue();
	    final T element = e.getKey();

	    if (probability < 0d || probability > 1d) {
		throw new IllegalArgumentException(String.format(
			INVALID_PROBABILITY_EX, probability, element, pmf));
	    }

	    totalProbability += probability;
	}

	if (Math.abs(1d - totalProbability) > 0.00001) {
	    throw new IllegalArgumentException(String.format(
		    TOTAL_PROBABILIT_EX, pmf, 1d, totalProbability));
	}

	double rnd = randomUnif(0, 1), weight = 0;

	for (Map.Entry<T, Double> e : pmf.entrySet()) {
	    weight += e.getValue();

	    if (weight > rnd) {
		return e.getKey();
	    }
	}

	throw new Error(UNREACHABLE_CODE);
    }

    /**
     * Returns the random seed.
     * 
     * @return Random seed
     */
    public int getSeed() {
	return seed;
    }
}
