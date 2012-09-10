package testbed.metric;

import java.util.Map;

import testbed.interfaces.RankingMetric;

/**
 * Mean Absolute Error (MAE) between calculated trust values and capabilities.
 * 
 * <p>
 * The metric calculates the the mean absolute error between the actual
 * capabilities and the calculated trust values. The trust values have to be
 * expressed in floating point numbers from [0, 1].
 * 
 * <p>
 * The metric is inverted -- smaller values mean better results.
 * 
 * @author David
 * 
 */
public class MeanAbsoluteError extends AbstractMetric implements RankingMetric {
    private static final String INCOMPATIBLE_METRIC = "Metric requires that trust is computed in floating point numbers from [0, 1].";
    private static final IllegalArgumentException UP = new IllegalArgumentException(
	    INCOMPATIBLE_METRIC);

    @Override
    public <T extends Comparable<T>> double evaluate(Map<Integer, T> rankings,
	    Map<Integer, Double> capabilities) {
	if (rankings.size() == 0) {
	    return Double.POSITIVE_INFINITY;
	}

	double sumOfErrors = 0;

	for (Map.Entry<Integer, T> e : rankings.entrySet()) {
	    if (!(e.getValue() instanceof Number))
		throw UP;

	    final Double trust = (Double) e.getValue();

	    if (trust > 1d || trust < 0d)
		throw UP;

	    final Double capability = capabilities.get(e.getKey());
	    final double difference = capability - trust;

	    sumOfErrors += Math.abs(difference);
	}

	return sumOfErrors / capabilities.size();
    }

    @Override
    public String toString() {
	return "Mean Absolute Error";
    }
}
