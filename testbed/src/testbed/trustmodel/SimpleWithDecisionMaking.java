package testbed.trustmodel;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import testbed.common.PartnerSelectionTemplates;
import testbed.interfaces.IDecisionMaking;

/**
 * Trust model on the basis of the {@link Simple} trust model that supports
 * selection partners for interactions.
 * 
 * @author David
 * 
 */
public class SimpleWithDecisionMaking extends Simple implements IDecisionMaking {

    protected PartnerSelectionTemplates selector;

    @Override
    public void initialize(Object... params) {
	super.initialize(params);
	selector = new PartnerSelectionTemplates(generator);
    }

    @Override
    public Map<Integer, Integer> getNextInteractionPartners(
	    Set<Integer> services) {
	final Map<Integer, Integer> partners = new HashMap<Integer, Integer>();

	for (int service : services) {
	    final Map<Integer, Double> trust = getTrust(service);
	    final Integer bestAgent = selector.probabilisticAndPowered(trust,
		    1d);

	    // only in the the first tick
	    if (null == bestAgent) {
		partners.put(service, 0);
	    } else {
		partners.put(service, bestAgent);
	    }
	}

	return partners;
    }

    @Override
    public String toString() {
	return "Simple";
    }
}
