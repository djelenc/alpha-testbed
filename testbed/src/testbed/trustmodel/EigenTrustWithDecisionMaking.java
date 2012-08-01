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
	    final boolean selectingNewPeer;
	    final Integer bestAgent;
	    selectingNewPeer = generator.nextDoubleFromTo(0, 1) < 0.1;

	    TreeMap<Integer, Double> agents = new TreeMap<Integer, Double>();

	    if (selectingNewPeer) {
		int count = 0;
		for (Map.Entry<Integer, Double> e : trust.entrySet()) {
		    if (e.getValue() < 0.0001) {
			agents.put(e.getKey(), 1d);
			count++;
		    }
		}
		for (Map.Entry<Integer, Double> e : agents.entrySet())
		    agents.put(e.getKey(), 1d / count);

		if (agents.isEmpty()) {
		    // case when an agent with score 0 was
		    // selected but no such agent exists
		    bestAgent = bestFromWeights();
		} else {
		    bestAgent = generator.fromWeights(agents);
		}
	    } else {
		final Integer best = bestFromWeights();

		if (null == best) {
		    bestAgent = 1;
		    // TODO: when the sum is 0 -- I should inspect why!
		    // System.err.println("Null best agents.");
		    // System.out.printf("Trust: %s\n", print(trust));
		    // System.out.printf("Proba: %s\n", print(agents));
		} else {
		    bestAgent = best;
		}
	    }

	    partners.put(service, bestAgent);
	}

	return partners;
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

    public Integer bestFromWeights() {
	final TreeMap<Integer, Double> agents = new TreeMap<Integer, Double>();
	double sum = 0;

	for (Map.Entry<Integer, Double> e : trust.entrySet()) {
	    if (e.getValue() > 0.00000001) {
		agents.put(e.getKey(), e.getValue());
		sum += e.getValue();
	    }
	}

	for (Map.Entry<Integer, Double> e : agents.entrySet())
	    agents.put(e.getKey(), e.getValue() / sum);

	return generator.fromWeights(agents);
    }
}
