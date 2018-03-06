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

import atb.common.Utils;
import atb.interfaces.ParameterCondition;
import atb.interfaces.ParametersPanel;

import java.util.Map;

/**
 * A pair-wise evaluation of the rankings that evaluates only those pairs of
 * agents, in which capabilities of both agents fall inside the given interval.
 *
 * @author David
 */
public class KTABounded extends OldAccuracy {
    protected Double lower, upper;

    @Override
    public void initialize(Object... params) {
        ParameterCondition<Double> validator = new ParameterCondition<Double>() {
            @Override
            public void eval(Double var) {
                if (var > 1 || var < 0) {
                    throw new IllegalArgumentException(String.format(
                            "A bound has be within [0, 1], but was %.2f.",
                            var));
                }
            }
        };

        lower = Utils.extractParameter(validator, 0, params);
        upper = Utils.extractParameter(validator, 1, params);

        if (lower >= upper) {
            throw new IllegalArgumentException(String.format(
                    "The lower bound must be lower than the upper bound: lower = %.2f, upper = %.2f.",
                    lower, upper));
        }
    }

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

                    if (lower < c1 && c1 < upper && lower < c2 && c2 < upper) {
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
        return "KTA with bounds";
    }

    @Override
    public ParametersPanel getParametersPanel() {
        return new KTABoundedGUI();
    }
}
