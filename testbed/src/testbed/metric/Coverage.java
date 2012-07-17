package testbed.metric;

import java.util.Map;

import testbed.interfaces.IMetric;

public class Coverage extends AbstractMetric implements IMetric {

    @Override
    public double evaluate(Map<Integer, Integer> rankings,
	    Map<Integer, Double> capabilities) {
	if (rankings.size() == 0) {
	    return 0;
	} else {
	    return ((double) rankings.size()) / capabilities.size();
	}
    }
}
