package testbed.metric;

import java.util.Map;

import testbed.interfaces.RankingMetric;

public class Coverage extends AbstractMetric implements RankingMetric {

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
