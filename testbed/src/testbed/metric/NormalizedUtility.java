package testbed.metric;

import java.util.Map;

import testbed.interfaces.Utility;

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
