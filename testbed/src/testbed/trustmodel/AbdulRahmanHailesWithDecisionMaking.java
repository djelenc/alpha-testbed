package testbed.trustmodel;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import testbed.common.PartnerSelectionTemplates;
import testbed.interfaces.IDecisionMaking;

/**
 * Trust model on the basis of the {@link AbdulRahmanHailes} that supports
 * selection partners for interactions. The selection is probabilistic.
 * 
 * <p>
 * <b>Note that the original proposal contains no such procedure. This is for
 * experimental purposes only.</b>
 * 
 * @author David
 * 
 */
public class AbdulRahmanHailesWithDecisionMaking extends AbdulRahmanHailes
	implements IDecisionMaking {

    protected int time;
    protected PartnerSelectionTemplates selector;

    @Override
    public void initialize(Object... params) {
	super.initialize(params);
	time = 0;
	selector = new PartnerSelectionTemplates(generator);
    }

    @Override
    public void setCurrentTime(int time) {
	this.time = time;
    }

    @Override
    public Map<Integer, Integer> getNextInteractionPartners(
	    Set<Integer> services) {
	final Map<Integer, Integer> partners = new HashMap<Integer, Integer>();

	for (int service : services) {
	    final Map<Integer, Double> trust = compute(service);
	    final Integer best = selector.probabilisticAndPowered(trust, 1d);

	    // This happens only in the first tick, where no experiences exist
	    if (null == best) {
		partners.put(service, 0);
	    } else {
		partners.put(service, best);
	    }
	}

	return partners;
    }

    @Override
    public String getName() {
	return super.getName() + " with decisions";
    }

}
