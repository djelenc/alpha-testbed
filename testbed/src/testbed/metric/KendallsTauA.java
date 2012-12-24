package testbed.metric;

import java.util.Map;
import java.util.Map.Entry;

import testbed.interfaces.Accuracy;

/**
 * Kendall's Tau-a metric
 * 
 * @author David
 * 
 */
public class KendallsTauA extends AbstractMetric implements Accuracy {

    @Override
    public <T extends Comparable<T>> double evaluate(Map<Integer, T> trust,
	    Map<Integer, Double> capabilities) {
	int concordant = 0, discordant = 0;

	for (Entry<Integer, Double> cap1 : capabilities.entrySet()) {
	    for (Entry<Integer, Double> cap2 : capabilities.entrySet()) {
		if (cap1.getKey() < cap2.getKey()) {
		    final Double c1 = cap1.getValue();
		    final Double c2 = cap2.getValue();
		    final T r1 = trust.get(cap1.getKey());
		    final T r2 = trust.get(cap2.getKey());

		    if (r1 != null && r2 != null) {
			final int rankDiff = r1.compareTo(r2);
			final int capDiff = c1.compareTo(c2);

			if (rankDiff * capDiff > 0) {
			    concordant++;
			} else if (rankDiff * capDiff < 0) {
			    discordant++;
			}
		    }
		}
	    }
	}

	final double n = capabilities.size() * (capabilities.size() - 1d) / 2d;
	final double metric = (concordant - discordant) / n;

	return (metric + 1d) / 2d;
    }

    @Override
    public String toString() {
	return "Kendall's Tau-A";
    }
}
