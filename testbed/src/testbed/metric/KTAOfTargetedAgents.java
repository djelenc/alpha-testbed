package testbed.metric;

import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import testbed.interfaces.Accuracy;
import testbed.scenario.TargetedAttack;

/**
 * Kendall's Tau-a metric
 * 
 * @author David
 * 
 */
public class KTAOfTargetedAgents extends AbstractMetric implements Accuracy {

    @Override
    public <T extends Comparable<T>> double evaluate(Map<Integer, T> trust,
	    Map<Integer, Double> capabilities) {
	int concordant = 0, discordant = 0;

	final List<Integer> targets = TargetedAttack.getTargets();
	final List<Integer> neutrals = TargetedAttack.getNeutrals();
	double n = 0;

	for (Entry<Integer, Double> cap1 : capabilities.entrySet()) {
	    for (Entry<Integer, Double> cap2 : capabilities.entrySet()) {
		if (cap1.getKey() < cap2.getKey()) {
		    final Double c1 = cap1.getValue();
		    final Double c2 = cap2.getValue();
		    final T r1 = trust.get(cap1.getKey());
		    final T r2 = trust.get(cap2.getKey());
		    final Integer a1 = cap1.getKey();
		    final Integer a2 = cap2.getKey();

		    final boolean shouldEvaluate = (targets.contains(a1) || targets
			    .contains(a2))
			    && (neutrals.contains(a1) || neutrals.contains(a2))
			    && !(neutrals.contains(a1) && neutrals.contains(a2));

		    if (shouldEvaluate)
			n++;

		    if (r1 != null && r2 != null && shouldEvaluate) {
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

	final double metric = (concordant - discordant) / n;

	return (metric + 1d) / 2d;
    }

    @Override
    public String toString() {
	return "KTA (Targeted agents)";
    }
}
