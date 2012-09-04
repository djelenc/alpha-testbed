package testbed.metric;

import java.util.Map;
import java.util.Map.Entry;

import testbed.interfaces.RankingMetric;

/**
 * Kendall's Tau-b metric
 * 
 * @author David
 * 
 */
public class KendallsTauB extends AbstractMetric implements RankingMetric {

    @Override
    public <T extends Comparable<T>> double evaluate(Map<Integer, T> trust,
	    Map<Integer, Double> capabilities) {
	if (trust.size() == 0) {
	    return 0;
	} else if (trust.size() == 1) {
	    return 1;
	}

	int concordant = 0, discordant = 0, tiedRanks = 0, tiedCapabilities = 0;

	for (Entry<Integer, T> rank1 : trust.entrySet()) {
	    for (Entry<Integer, T> rank2 : trust.entrySet()) {
		if (rank1.getKey() < rank2.getKey()) {
		    final T r1 = rank1.getValue();
		    final T r2 = rank2.getValue();
		    final Double c1 = capabilities.get(rank1.getKey());
		    final Double c2 = capabilities.get(rank2.getKey());

		    final int rankDiff = r1.compareTo(r2);
		    final int capDiff = (Math.abs(c1 - c2) < 0.00001 ? 0 : c1
			    .compareTo(c2));

		    if (rankDiff * capDiff > 0) {
			concordant++;
		    } else if (rankDiff * capDiff < 0) {
			discordant++;
		    } else {
			if (rankDiff == 0)
			    tiedRanks++;

			if (capDiff == 0)
			    tiedCapabilities++;
		    }
		}
	    }
	}

	final double n = trust.size() * (trust.size() - 1d) / 2d;
	final double metric = (concordant - discordant)
		/ Math.sqrt((n - tiedRanks) * (n - tiedCapabilities));

	return (metric + 1d) / 2d;
    }

    @Override
    public String toString() {
	return "Kendall's tau-b";
    }
}
