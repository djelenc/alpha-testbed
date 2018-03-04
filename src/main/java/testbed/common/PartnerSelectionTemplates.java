/*
 * Copyright (c) 2013 David Jelenc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0
 * which accompanies this distribution, and is available at
 * http://www.gnu.org/licenses/gpl.html
 *
 * Contributors:
 *     David Jelenc - initial API and implementation
 */
package testbed.common;

import testbed.interfaces.RandomGenerator;

import java.util.Map;
import java.util.TreeMap;

/**
 * A class that provides a few template implementations of the partner selection
 * procedure.
 *
 * @author David
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
     * <p>
     * <p>
     * The values are first raised to the power which is given as the second
     * parameter, and then normalized. Because of this, it is not required that
     * the given map of trust values is a proper PMF (i.e. the sum of trust
     * values in the given map does not need to be one -- this function will
     * normalize the weights).
     * <p>
     * <p>
     * The assigned trust values, however, have to be positive numbers.
     * <p>
     * <p>
     * If the given map of trust values is empty, the function returns null.
     *
     * @param trust Given map of trust values
     * @param power The number to which the trust values are raised to.
     * @return The selected agent.
     */
    public Integer probabilisticAndPowered(Map<Integer, Double> trust,
                                           double power) {
        final TreeMap<Integer, Double> agents = new TreeMap<Integer, Double>();
        double sum = 0;

        for (Map.Entry<Integer, Double> e : trust.entrySet()) {
            if (e.getValue() < 0)
                throw new IllegalArgumentException(
                        String.format(NEGATIVE, e.getKey(), e.getValue()));

            final double prob = Math.pow(e.getValue(), power);
            agents.put(e.getKey(), prob);
            sum += prob;
        }

        for (Map.Entry<Integer, Double> e : agents.entrySet())
            agents.put(e.getKey(), e.getValue() / sum);

        return generator.fromWeights(agents);
    }

    /**
     * Selects a random agent, while using assigned trust values as
     * probabilities.
     * <p>
     * <p>
     * It is not required that the given map of trust values is a proper PMF
     * (i.e. the sum of trust values in the given map does not need to be one --
     * this function will normalize the weights).
     * <p>
     * <p>
     * The assigned trust values, however, have to be positive numbers.
     * <p>
     * <p>
     * If the given map of trust values is empty, the function returns null.
     *
     * @param trust Given map of trust values
     * @return The selected agent.
     */
    public Integer probabilistic(Map<Integer, Double> trust) {
        return probabilisticAndPowered(trust, 1d);
    }

    /**
     * Selects an agent with the highest trust value.
     * <p>
     * <p>
     * If the given map is empty, the function returns null.
     *
     * @param trust Map of trust values.
     * @return Selected agent.
     */
    public <T extends Comparable<T>> Integer maximal(Map<Integer, T> trust) {
        Integer bestAgent = null;
        T maxTrust = null;

        for (Map.Entry<Integer, T> entry : trust.entrySet()) {
            final Integer agent = entry.getKey();
            final T value = entry.getValue();

            if (value == null)
                continue;

            if (maxTrust == null || value.compareTo(maxTrust) > 0) {
                maxTrust = value;
                bestAgent = agent;
            }
        }

        return bestAgent;
    }
}
