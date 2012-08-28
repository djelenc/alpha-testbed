package testbed.metric;

import java.util.Map;

import testbed.interfaces.IRankingMetric;

/**
 * Kendall's Tau-b metric
 * 
 * @author David
 * 
 */
public class KendallsTauB extends AbstractMetric implements IRankingMetric {

    @Override
    public double evaluate(Map<Integer, Integer> rankings,
	    Map<Integer, Double> capabilities) {
	if (rankings.size() == 0) {
	    return 0;
	} else if (rankings.size() == 1) {
	    return 1;
	}

	int concordant = 0, discordant = 0, tiedRanks = 0, tiedCapabilities = 0;

	for (Map.Entry<Integer, Integer> rank1 : rankings.entrySet()) {
	    for (Map.Entry<Integer, Integer> rank2 : rankings.entrySet()) {
		if (rank1.getKey() < rank2.getKey()) {
		    final int r1 = rank1.getValue();
		    final int r2 = rank2.getValue();
		    final double c1 = capabilities.get(rank1.getKey());
		    final double c2 = capabilities.get(rank2.getKey());

		    if ((r1 < r2 && c1 > c2) || (r1 > r2 && c1 < c2)) {
			concordant++;
		    } else if ((r1 < r2 && c1 < c2) || (r1 > r2 && c1 > c2)) {
			discordant++;
		    } else {
			if (r1 == r2)
			    tiedRanks++;

			if (Math.abs(c1 - c2) < 0.00001)
			    tiedCapabilities++;
		    }
		}
	    }
	}

	final double totalComparisons = rankings.size()
		* (rankings.size() - 1d) / 2d;

	final double metric = (concordant - discordant)
		/ Math.sqrt((totalComparisons - tiedRanks)
			* (totalComparisons - tiedCapabilities));

	return (metric + 1d) / 2d;

    }

    @Override
    public String getName() {
	return "Kendall's tau-b";
    }
}
