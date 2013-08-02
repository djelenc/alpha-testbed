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

import testbed.interfaces.Utility;

public class CumulativeNormalizedUtility extends AbstractMetric implements
	Utility {

    private double total, maximal;

    @Override
    public void initialize(Object... params) {
	total = 0;
	maximal = 0;
    }

    @Override
    public double evaluate(Map<Integer, Double> capabilities, int agent) {
	total += capabilities.get(agent);

	double max = Double.MIN_VALUE;

	for (Map.Entry<Integer, Double> cap : capabilities.entrySet()) {
	    if (Double.compare(cap.getValue(), max) > 0) {
		max = cap.getValue();
	    }
	}

	maximal += max;

	return total / maximal;
    }

    @Override
    public String toString() {
	return "Cumulative normalized utility";
    }
}
