package testbed.metric;

import java.util.Map;

import testbed.interfaces.IRankingMetric;

/**
 * Weighted Kendall's Tau
 * 
 * <p>
 * This metric weighs every inversion with the amount of difference between
 * elements that are inverted. The weights are computed from capabilities.
 * 
 * @author David
 * 
 */
public class WeightedKendallsTau extends AbstractMetric implements
	IRankingMetric {

    @Override
    public double evaluate(Map<Integer, Integer> rankings,
	    Map<Integer, Double> capabilities) {
	if (rankings.size() == 0) {
	    return 0;
	} else if (rankings.size() == 1) {
	    return 1;
	}

	int r1, r2;
	double c1, c2, sum = 0, result = 0;

	for (Map.Entry<Integer, Integer> rank1 : rankings.entrySet()) {
	    for (Map.Entry<Integer, Integer> rank2 : rankings.entrySet()) {
		if (rank1.getKey() < rank2.getKey()) {
		    r1 = rank1.getValue();
		    r2 = rank2.getValue();
		    c1 = capabilities.get(rank1.getKey());
		    c2 = capabilities.get(rank2.getKey());
		    sum += Math.abs(c1 - c2);

		    if ((r1 < r2 && c1 > c2) || (r1 > r2 && c1 < c2))
			result += Math.abs(c1 - c2);
		}
	    }
	}

	return result / sum;
    }
}
