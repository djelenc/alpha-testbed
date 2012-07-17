package testbed.metric;

import java.util.Map;

import testbed.interfaces.IMetric;

/**
 * Kendall's Tau metric
 * 
 * The same as Accuracy, but only if ranking contains no ties.
 * 
 * @author david
 * 
 */
public class WeightedKendallsTau extends AbstractMetric implements IMetric {

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
