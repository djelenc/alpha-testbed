package testbed.trustmodel;

import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;

import testbed.common.PartnerSelectionTemplates;
import testbed.interfaces.SelectingInteractionPartners;

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
public class AbdulRahmanHailesSelectingInteractionPartners extends
	AbdulRahmanHailes implements SelectingInteractionPartners {

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
    public Map<Integer, Integer> getInteractionPartners(Set<Integer> services) {
	final Map<Integer, Integer> partners = new HashMap<Integer, Integer>();

	for (int service : services) {
	    final Map<Integer, TD> computedTrust = getTrust(service);
	    final Map<Integer, Double> trust = new LinkedHashMap<Integer, Double>();

	    for (int agent : computedTrust.keySet()) {
		trust.put(agent, computedTrust.get(agent).numeric);
	    }

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
}
