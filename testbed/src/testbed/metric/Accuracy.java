package testbed.metric;

import java.util.Map;

import testbed.interfaces.IMetric;

public class Accuracy extends AbstractMetric implements IMetric {

    @Override
    public double evaluate(Map<Integer, Integer> rankings,
	    Map<Integer, Double> capabilities) {
	if (rankings.size() == 0) {
	    return 0;
	} else if (rankings.size() == 1) {
	    return 1;
	}

	int result = 0;
	for (Map.Entry<Integer, Integer> rank1 : rankings.entrySet()) {
	    for (Map.Entry<Integer, Integer> rank2 : rankings.entrySet()) {
		if (!rank1.equals(rank2)) {
		    result += evaluatePair(rank1.getValue(), rank2.getValue(),
			    capabilities.get(rank1.getKey()),
			    capabilities.get(rank2.getKey()));
		}
	    }
	}

	return ((double) result) / (rankings.size() * (rankings.size() - 1));
    }

    public final int evaluatePair(int r1, int r2, double c1, double c2) {
	if ((r1 <= r2 && c1 >= c2) || (r1 > r2 && c1 < c2)) {
	    return 1;
	} else {
	    return 0;
	}
    }
}
