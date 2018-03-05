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
package atb.metric;

import atb.interfaces.Accuracy;

import java.util.Map;

/**
 * Euclidean difference between calculated trust values and capabilities.
 * <p>
 * <p>
 * The metric calculates the Euclidean distance between the actual capabilities
 * and the calculated trust values. The trust values have to be expressed in
 * floating point numbers from [0, 1].
 * <p>
 * <p>
 * This metric is different from other metrics in several aspects:
 * <ul>
 * <li>It requires that trust is expressed in floating point numbers between 0
 * and 1, inclusively.
 * <li>It has a minimum value, but no maximum value.
 * <li>It is inverted -- small values mean better results. Consequently, the
 * minimum value also represents the perfect score (a trust model that would
 * compute trust values that are exactly the same as the actual capabilities
 * would achieve the score of 0.)
 * </ul>
 *
 * @author David
 */
public class Euclidean extends AbstractMetric implements Accuracy {
    private static final String INCOMPATIBLE_METRIC = "Metric requires that trust is computed in floating point numbers from [0, 1].";
    private static final IllegalArgumentException UP = new IllegalArgumentException(
            INCOMPATIBLE_METRIC);

    @Override
    public <T extends Comparable<T>> double evaluate(Map<Integer, T> rankings,
                                                     Map<Integer, Double> capabilities) {
        if (rankings.size() == 0) {
            return Double.POSITIVE_INFINITY;
        }

        double sumofSquares = 0;

        for (Map.Entry<Integer, T> e : rankings.entrySet()) {
            if (!(e.getValue() instanceof Number))
                throw UP;

            final Double trust = (Double) e.getValue();

            if (trust > 1d || trust < 0d)
                throw UP;

            final Double capability = capabilities.get(e.getKey());
            final double difference = capability - trust;

            sumofSquares += difference * difference;
        }

        return Math.sqrt(sumofSquares);
    }

    @Override
    public String toString() {
        return "Euclidean difference";
    }
}
