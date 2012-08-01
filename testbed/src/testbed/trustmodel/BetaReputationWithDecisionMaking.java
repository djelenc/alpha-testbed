package testbed.trustmodel;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;

import testbed.interfaces.IDecisionMaking;

/**
 * Trust model on the basis of the {@link BetaReputation} that supports
 * selection partners for interactions. The selection is probabilistic.
 * 
 * <p>
 * <b>Note that the original proposal contains no such procedure. This is for
 * experimental purposes only.</b>
 * 
 * @author David
 * 
 */
public class BetaReputationWithDecisionMaking extends BetaReputation implements
	IDecisionMaking {

    @Override
    public Map<Integer, Integer> getNextInteractionPartners(
	    Set<Integer> services) {
	final Map<Integer, Integer> partners = new HashMap<Integer, Integer>();

	for (int service : services) {
	    final Map<Integer, Double> trust = compute();
	    final Integer best = bestFromWeights(trust);

	    if (1 == time) {
		/*
		 * This happens only in the first tick, where no experiences
		 * exist -- because all opinions are discounted (and in the
		 * beginning the discount factor is 0), all opinions become 0.
		 */
		partners.put(service, 0);
	    } else {
		partners.put(service, best);
	    }
	}

	return partners;
    }

    public Integer bestFromWeights(Map<Integer, Double> trust) {
	final TreeMap<Integer, Double> agents = new TreeMap<Integer, Double>();
	double sum = 0;

	// final double power = 1d;
	final double power = Math.sqrt(1 + time);

	for (Map.Entry<Integer, Double> e : trust.entrySet()) {
	    final double prob = Math.pow(e.getValue(), power);
	    agents.put(e.getKey(), prob);
	    sum += prob;
	}

	for (Map.Entry<Integer, Double> e : agents.entrySet())
	    agents.put(e.getKey(), e.getValue() / sum);

	return generator.fromWeights(agents);
    }

    @Override
    public String getName() {
	return super.getName() + " with decision making";
    }

}
