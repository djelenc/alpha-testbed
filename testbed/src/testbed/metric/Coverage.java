package testbed.metric;

import java.util.Map;

import testbed.interfaces.IRankingMetric;

public class Coverage extends AbstractMetric implements IRankingMetric {

    @Override
    public <T extends Comparable<T>> double evaluate(Map<Integer, T> trust,
	    Map<Integer, Double> capabilities) {
	if (trust.size() == 0) {
	    return 0;
	} else {
	    return ((double) trust.size()) / capabilities.size();
	}
    }
}
