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
 * Spearman's foot rule metric.
 * <p>
 * <p>
 * Assumes the rankings have the same length.
 *
 * @author David
 */
public class SpearmansFootRule extends AbstractMetric implements Accuracy {

    @Override
    public <T extends Comparable<T>> double evaluate(Map<Integer, T> rankings,
                                                     Map<Integer, Double> capabilities) {
        final Map<Integer, Double> data = fractionalRanking(rankings);
        final Map<Integer, Double> truth = fractionalRanking(capabilities);

        double squaredSum = 0;

        for (Map.Entry<Integer, Double> e : data.entrySet()) {
            final int agent = e.getKey();
            final double dataRank = e.getValue().doubleValue();
            final double truthRank = truth.get(agent).doubleValue();
            final double rankDiff = dataRank - truthRank;
            squaredSum += rankDiff * rankDiff;
        }

        final int n = data.size();

        return 1d - 3d * squaredSum / n / (n * n - 1d);
    }

    @Override
    public String toString() {
        return "Spearman's Foot Rule";
    }
}
