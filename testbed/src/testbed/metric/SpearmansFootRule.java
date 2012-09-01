package testbed.metric;

import java.util.Map;

import testbed.interfaces.IRankingMetric;

/**
 * Spearman's foot rule metric.
 * 
 * TODO: what if the length of given estimations differs from the length of
 * actuals?
 * 
 * @author David
 * 
 */
public class SpearmansFootRule extends AbstractMetric implements IRankingMetric {

    @Override
    public <T extends Comparable<T>> double evaluate(Map<Integer, T> rankings,
	    Map<Integer, Double> capabilities) {
	final Map<Integer, Number> data = fractionalRanking(rankings);
	final Map<Integer, Number> truth = fractionalRanking(capabilities);

	double squaredSum = 0;

	for (Map.Entry<Integer, Number> e : data.entrySet()) {
	    final int agent = e.getKey();
	    final double dataRank = e.getValue().doubleValue();
	    final double truthRank = truth.get(agent).doubleValue();
	    final double rankDiff = dataRank - truthRank;
	    squaredSum += rankDiff * rankDiff;
	}

	final int n = data.size();

	return (2d - 6 * squaredSum / n / (n * n - 1d)) / 2d;
    }

    @Override
    public String toString() {
	return "Spearman's Foot Rule";
    }
}
