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
package testbed.metric;

import testbed.interfaces.Accuracy;

import java.util.Map;
import java.util.Map.Entry;

public class OldAccuracy extends AbstractMetric implements Accuracy {

    @Override
    public <T extends Comparable<T>> double evaluate(Map<Integer, T> trust,
                                                     Map<Integer, Double> capabilities) {
        if (trust.size() == 0) {
            return 0;
        } else if (trust.size() == 1) {
            return 1;
        }

        int result = 0;
        for (Entry<Integer, T> trust1 : trust.entrySet()) {
            for (Entry<Integer, T> trust2 : trust.entrySet()) {
                if (!trust1.equals(trust2)) {
                    final T t1 = trust1.getValue();
                    final T t2 = trust2.getValue();
                    final Double c1 = capabilities.get(trust1.getKey());
                    final Double c2 = capabilities.get(trust2.getKey());

                    result += evaluatePair(t1, t2, c1, c2);
                }
            }
        }

        return ((double) result) / (trust.size() * (trust.size() - 1));
    }

    public final <T extends Comparable<T>> int evaluatePair(T t1, T t2,
                                                            Double c1, Double c2) {

        final int rankDiff = t1.compareTo(t2);
        final int capDiff = (Math.abs(c1 - c2) < 0.00001 ? 0
                : c1.compareTo(c2));

        if ((rankDiff >= 0 && capDiff >= 0) || (rankDiff < 0 && capDiff < 0)) {
            return 1;
        } else {
            return 0;
        }
    }
}
