package testbed.metric;

import java.util.Map;

import testbed.interfaces.IRankingMetric;

/**
 * Numeric difference between estimations and actual capabilities.
 * 
 * @author David
 * 
 */
public class NumericDifference extends AbstractMetric implements IRankingMetric {

    @Override
    public <T extends Comparable<T>> double evaluate(Map<Integer, T> rankings,
	    Map<Integer, Double> capabilities) {
	if (rankings.size() == 0) {
	    return 0;
	} else if (rankings.size() == 1) {
	    return 1;
	}

	double sumofSquares = 0;

	for (Map.Entry<Integer, T> rank1 : rankings.entrySet()) {
	    final T r1 = rank1.getValue();
	    final Double c1 = capabilities.get(rank1.getKey());

	    if (!(r1 instanceof Number)) {
		return 0d;
	    }

	    final double difference = c1 - ((Number) r1).doubleValue();

	    sumofSquares += difference * difference;
	}

	return Math.sqrt(sumofSquares);
    }

    @Override
    public String toString() {
	return "Numeric difference";
    }
}
