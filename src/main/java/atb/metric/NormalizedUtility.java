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

import atb.interfaces.Utility;

import java.util.Map;

public class NormalizedUtility extends AbstractMetric implements Utility {

    @Override
    public double evaluate(Map<Integer, Double> capabilities, int agent) {
        final double obtained = capabilities.get(agent);
        double maximum = 0d;

        for (Map.Entry<Integer, Double> cap : capabilities.entrySet()) {
            if (Double.compare(cap.getValue(), maximum) > 0) {
                maximum = cap.getValue();
            }
        }

        return obtained / maximum;
    }

    @Override
    public String toString() {
        return "Normalized utility";
    }
}
