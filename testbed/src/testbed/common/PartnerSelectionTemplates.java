package testbed.common;

import java.util.Map;
import java.util.TreeMap;

import testbed.interfaces.RandomGenerator;

/**
 * A class that provides a few template implementations of the partner selection
 * procedure.
 * 
 * @author David
 * 
 */
public class PartnerSelectionTemplates {
    private static final String NEGATIVE = "Negative value at %d => %.2f. Only positive values are allowed.";

    final private RandomGenerator generator;

    public PartnerSelectionTemplates(RandomGenerator generator) {
	this.generator = generator;
    }

    /**
     * Selects a random agent, while using assigned trust values as
     * probabilities.
     * 
     * <p>
     * The values are first raised to the power which is given as the second
     * parameter, and then normalized. Because of this, it is not required that
     * the given map of trust values is a proper PMF (i.e. the sum of trust
     * values in the given map does not need to be one -- this function will
     * normalize the weights).
     * 
     * <p>
     * The assigned trust values, however, have to be positive numbers.
     * 
     * <p>
     * If the given map of trust values is empty, the function returns null.
     * 
     * @param trust
     *            Given map of trust values
     * @param power
     *            The number to which the trust values are raised to.
     * @return The selected agent.
     */
    public Integer probabilisticAndPowered(Map<Integer, Double> trust,
	    double power) {
	final TreeMap<Integer, Double> agents = new TreeMap<Integer, Double>();
	double sum = 0;

	for (Map.Entry<Integer, Double> e : trust.entrySet()) {
	    if (e.getValue() < 0)
		throw new IllegalArgumentException(String.format(NEGATIVE,
			e.getKey(), e.getValue()));

	    final double prob = Math.pow(e.getValue(), power);
	    agents.put(e.getKey(), prob);
	    sum += prob;
	}

	for (Map.Entry<Integer, Double> e : agents.entrySet())
	    agents.put(e.getKey(), e.getValue() / sum);

	return generator.fromWeights(agents);
    }

    /**
     * Selects an agent with the highest trust value.
     * 
     * <p>
     * If the given map is empty, the function returns null.
     * 
     * @param trust
     *            Map of trust values.
     * @return Selected agent.
     */
    public Integer maximal(Map<Integer, Double> trust) {
	int bestAgent = Integer.MIN_VALUE;
	double maxTrust = Double.MIN_VALUE;

	for (Map.Entry<Integer, Double> entry : trust.entrySet()) {
	    if (maxTrust < entry.getValue()) {
		maxTrust = entry.getValue();
		bestAgent = entry.getKey();
	    }
	}

	if (bestAgent == Integer.MIN_VALUE) {
	    return null;
	} else {
	    return bestAgent;
	}
    }
}
