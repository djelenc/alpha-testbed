package testbed.trustmodel;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import testbed.common.PartnerSelectionTemplates;
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
	    final Map<Integer, Double> trust = compute();
	    final Integer best = selector.probabilisticAndPowered(trust, 1d);

	    if (null == best) {
		/*
		 * This happens only in the first tick, where no experiences
		 * exist -- because all opinions are discounted and in the
		 * beginning the discount factor is 0, all opinions are 0.
		 */
		partners.put(service, 0);
	    } else {
		partners.put(service, best);
	    }
	}

	return partners;
    }
}
