package testbed.metric;

import java.util.Map;

import testbed.interfaces.IRankingMetric;

/**
 * Kendall's Tau metric
 * 
 * The same as Accuracy, but only if ranking contains no ties.
 * 
 * @author david
 * 
 */
public class KendallsTau extends AbstractMetric implements IRankingMetric {

    @Override
    public double evaluate(Map<Integer, Integer> rankings,
	    Map<Integer, Double> capabilities) {
	if (rankings.size() == 0) {
	    return 0;
	} else if (rankings.size() == 1) {
	    return 1;
	}

	int result = 0, r1, r2;
	double c1, c2;

	for (Map.Entry<Integer, Integer> rank1 : rankings.entrySet()) {
	    for (Map.Entry<Integer, Integer> rank2 : rankings.entrySet()) {
		if (rank1.getKey() < rank2.getKey()) {
		    r1 = rank1.getValue();
		    r2 = rank2.getValue();
		    c1 = capabilities.get(rank1.getKey());
		    c2 = capabilities.get(rank2.getKey());

		    if ((r1 < r2 && c1 > c2) || (r1 > r2 && c1 < c2))
			result += 1;
		}
	    }
	}

	return 2d * result / rankings.size() / (rankings.size() - 1);
    }
}
