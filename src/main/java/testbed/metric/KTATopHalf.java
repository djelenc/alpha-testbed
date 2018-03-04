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

import java.util.Map;

/**
 * A pair-wise evaluation of the rankings, that evaluates only those pairs of
 * agents, in which at least one agent has capability more than 0.5.
 *
 * @author David
 */
public class KTATopHalf extends OldAccuracy {

    @Override
    public <T extends Comparable<T>> double evaluate(Map<Integer, T> trust,
                                                     Map<Integer, Double> capabilities) {
        int result = 0, cmpCount = 0;

        for (Map.Entry<Integer, Double> cap1 : capabilities.entrySet()) {
            for (Map.Entry<Integer, Double> cap2 : capabilities.entrySet()) {
                if (cap1.getKey() < cap2.getKey()) {
                    final T r1 = trust.get(cap1.getKey());
                    final T r2 = trust.get(cap2.getKey());
                    final Double c1 = cap1.getValue();
                    final Double c2 = cap2.getValue();

                    if (c1 > 0.5 || c2 > 0.5) {
                        result += evaluatePair(r1, r2, c1, c2);
                        cmpCount += 1;
                    }
                }
            }
        }

        return ((double) result) / cmpCount;
    }

    @Override
    public String toString() {
        return "KTA (>0.5)";
    }

}
