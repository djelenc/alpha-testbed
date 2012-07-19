package testbed.metric;

import java.util.Map;

import testbed.interfaces.IParametersPanel;
import testbed.interfaces.IUtilityMetric;

public class NormalizedUtility implements IUtilityMetric {

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
    public String getName() {
	return "Normalized utility";
    }

    @Override
    public IParametersPanel getParametersPanel() {
	return null;
    }
}
