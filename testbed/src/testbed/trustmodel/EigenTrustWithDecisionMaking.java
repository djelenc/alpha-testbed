package testbed.trustmodel;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;

import testbed.interfaces.IDecisionMaking;

/**
 * Trust model on the basis of the {@link EigenTrust} trust model that supports
 * selection partners for interactions. The partner selection procedure is
 * probabilistic.
 * 
 * @author David
 * 
 */
public class EigenTrustWithDecisionMaking extends EigenTrust implements
	IDecisionMaking {

    @Override
    public Map<Integer, Integer> getNextInteractionPartners(
	    Set<Integer> services) {
	final Map<Integer, Integer> partners = new HashMap<Integer, Integer>();

	for (int service : services) {
	    final Map<Integer, Double> trust = compute();
	    final boolean selectingNewPeer = generator.nextDoubleFromTo(0, 1) < 0.1;

	    final Integer bestAgent;

	    if (selectingNewPeer) {
		final Map<Integer, Double> uniform = new HashMap<Integer, Double>();
		int count = 0;

		// add to map agents all agents with trust 0
		for (Map.Entry<Integer, Double> e : trust.entrySet()) {
		    if (e.getValue() < 0.0001) {
			uniform.put(e.getKey(), 1d);
			count++;
		    }
		}

		if (0 == count) {
		    // we decided to select a peer with trust 0, but no such
		    // peer exist -- we have to select the usual way
		    bestAgent = bestFromWeights(trust);
		} else {
		    // uniform selection amongst agents with trust 0
		    bestAgent = bestFromWeights(uniform);
		}
	    } else {
		// selecting according to the probabilities
		final Integer best = bestFromWeights(trust);

		if (null == best) {
		    bestAgent = 1;
		    // TODO: when the sum is 0 -- I should inspect why!
		    System.err.println("Null best agent -- using default.");
		} else {
		    bestAgent = best;
		}
	    }

	    partners.put(service, bestAgent);
	}

	return partners;
    }

    public Integer bestFromWeights(Map<Integer, Double> trust) {
	final TreeMap<Integer, Double> pmf = new TreeMap<Integer, Double>();

	double sum = 0;
	// remove zero values
	for (Map.Entry<Integer, Double> e : trust.entrySet()) {
	    if (e.getValue() > 0.00000001) {
		final int agent = e.getKey();
		final double prob = e.getValue();

		pmf.put(agent, prob);
		sum += prob;
	    }
	}

	// normalize to get proper PMF
	for (Map.Entry<Integer, Double> e : pmf.entrySet())
	    pmf.put(e.getKey(), e.getValue() / sum);

	// random selection
	return generator.fromWeights(pmf);
    }

    @Override
    public String getName() {
	return "EigenTrust with decisions";
    }

    public String print(Map<Integer, Double> map) {
	StringBuffer sb = new StringBuffer();

	for (Map.Entry<Integer, Double> e : map.entrySet())
	    sb.append(String.format("%d=%.2f, ", e.getKey(), e.getValue()));

	return sb.toString();
    }
}
