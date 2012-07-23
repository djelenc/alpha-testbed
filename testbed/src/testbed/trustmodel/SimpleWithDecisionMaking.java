package testbed.trustmodel;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import testbed.interfaces.IDecisionMaking;

/**
 * Trust model on the basis of the {@link Simple} trust model that supports
 * selection partners for interactions.
 * 
 * @author David
 * 
 */
public class SimpleWithDecisionMaking extends Simple implements IDecisionMaking {

    @Override
    public Map<Integer, Integer> getNextInteractionPartners(
	    Set<Integer> services) {
	final Map<Integer, Integer> partners = new HashMap<Integer, Integer>();

	for (int service : services) {
	    int bestAgent = Integer.MIN_VALUE;
	    double maxTrust = Double.MIN_VALUE;

	    for (Map.Entry<Integer, Double> entry : trust.entrySet()) {
		if (maxTrust < entry.getValue()) {
		    maxTrust = entry.getValue();
		    bestAgent = entry.getKey();
		}
	    }

	    if (bestAgent == Integer.MIN_VALUE)
		bestAgent = 0; // TODO: A bad default.

	    partners.put(service, bestAgent);
	}

	return partners;
    }

    @Override
    public String getName() {
	return "Simple with decisions";
    }

}
