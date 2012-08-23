package testbed.metric;

import java.util.Map;

/**
 * 
 * A pair-wise evaluation of the rankings, that evaluates only those pairs of
 * agents, in which at least one agent has capability more than 0.5.
 * 
 * @author David
 * 
 */
public class AccuracyTopHalf extends Accuracy {

    @Override
    public double evaluate(Map<Integer, Integer> rankings,
	    Map<Integer, Double> capabilities) {
	if (rankings.size() == 0) {
	    return 0;
	} else if (rankings.size() == 1) {
	    return 1;
	}

	int result = 0, cmpCount = 0, r1, r2;
	double c1, c2;

	for (Map.Entry<Integer, Integer> rank1 : rankings.entrySet()) {
	    for (Map.Entry<Integer, Integer> rank2 : rankings.entrySet()) {
		if (!rank1.equals(rank2)) {
		    r1 = rank1.getValue();
		    r2 = rank2.getValue();
		    c1 = capabilities.get(rank1.getKey());
		    c2 = capabilities.get(rank2.getKey());

		    if (c1 > 0.5 || c2 > 0.5) {
			result += evaluatePair(r1, r2, c1, c2);
			cmpCount += 1;
		    }
		}
	    }
	}

	return ((double) result) / cmpCount;
    }

    @Override
    public String getName() {
	return "Accuracy(0.5)";
    }

}