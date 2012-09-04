package testbed.metric;

import java.util.Map;

import testbed.interfaces.UtilityMetric;

public class CumulativeNormalizedUtility extends AbstractMetric implements
	UtilityMetric {

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
