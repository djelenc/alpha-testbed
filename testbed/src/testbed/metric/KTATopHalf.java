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
public class KTATopHalf extends Accuracy {

    @Override
    public <T extends Comparable<T>> double evaluate(Map<Integer, T> trust,
	    Map<Integer, Double> capabilities) {
	if (trust.size() == 0) {
	    return 0;
	} else if (trust.size() == 1) {
	    return 1;
	}

	int result = 0, cmpCount = 0;

	for (Map.Entry<Integer, T> trust1 : trust.entrySet()) {
	    for (Map.Entry<Integer, T> trust2 : trust.entrySet()) {
		if (!trust1.equals(trust2)) {
		    final T r1 = trust1.getValue();
		    final T r2 = trust2.getValue();
		    final Double c1 = capabilities.get(trust1.getKey());
		    final Double c2 = capabilities.get(trust2.getKey());

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
    public String toString() {
	return "Kendall's Tau-A (>0.5)";
    }

}