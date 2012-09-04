package testbed.metric;

import java.util.Map;

import testbed.interfaces.RankingMetric;

/**
 * Kendall's Tau metric
 * 
 * The same as Accuracy, but only if ranking contains no ties.
 * 
 * @author david
 * 
 */
public class KendallsTau extends AbstractMetric implements RankingMetric {

    @Override
    public <T extends Comparable<T>> double evaluate(Map<Integer, T> trust,
	    Map<Integer, Double> capabilities) {
	if (trust.size() == 0) {
	    return 0;
	} else if (trust.size() == 1) {
	    return 1;
	}

	int result = 0;

	for (Map.Entry<Integer, T> trust1 : trust.entrySet()) {
	    for (Map.Entry<Integer, T> trust2 : trust.entrySet()) {
		if (trust1.getKey() < trust2.getKey()) {
		    final T r1 = trust1.getValue();
		    final T r2 = trust2.getValue();
		    final Double c1 = capabilities.get(trust1.getKey());
		    final Double c2 = capabilities.get(trust2.getKey());

		    if ((r1.compareTo(r2) > 0 && c1.compareTo(c2) > 0)
			    || (r1.compareTo(r2) < 0 && c1.compareTo(c2) < 0)) {
			result += 1;
		    }
		}
	    }
	}

	return 2d * result / trust.size() / (trust.size() - 1);
    }

    @Override
    public String toString() {
	return "Kendall's Tau";
    }
}
