package testbed.metric;

import java.util.Map;

public class Coverage extends AbstractRankingMetric {

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
