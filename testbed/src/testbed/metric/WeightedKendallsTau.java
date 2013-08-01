/*******************************************************************************
 * Copyright (c) 2013 David Jelenc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0
 * which accompanies this distribution, and is available at
 * http://www.gnu.org/licenses/gpl.html
 * 
 * Contributors:
 *     David Jelenc - initial API and implementation
 ******************************************************************************/
package testbed.metric;

import java.util.Map;

import testbed.interfaces.Accuracy;

/**
 * Weighted Kendall's Tau
 * 
 * <p>
 * This metric weighs every inversion with the amount of difference between
 * elements that are inverted. The weights are computed from capabilities.
 * 
 * @author David
 * 
 */
public class WeightedKendallsTau extends AbstractMetric implements Accuracy {

    @Override
    public <T extends Comparable<T>> double evaluate(Map<Integer, T> rankings,
	    Map<Integer, Double> capabilities) {
	if (rankings.size() == 0) {
	    return 0;
	} else if (rankings.size() == 1) {
	    return 1;
	}

	double sum = 0, result = 0;

	for (Map.Entry<Integer, T> rank1 : rankings.entrySet()) {
	    for (Map.Entry<Integer, T> rank2 : rankings.entrySet()) {
		if (rank1.getKey() < rank2.getKey()) {
		    final T r1 = rank1.getValue();
		    final T r2 = rank2.getValue();
		    final Double c1 = capabilities.get(rank1.getKey());
		    final Double c2 = capabilities.get(rank2.getKey());

		    final double difference = Math.abs(c1 - c2);

		    sum += difference;

		    if ((r1.compareTo(r2) > 0 && c1.compareTo(c2) > 0)
			    || (r1.compareTo(r2) < 0 && c1.compareTo(c2) < 0)) {
			result += difference;
		    }
		}
	    }
	}

	return result / sum;
    }

    @Override
    public String toString() {
	return "Weighted Kendall's Tau";
    }
}
