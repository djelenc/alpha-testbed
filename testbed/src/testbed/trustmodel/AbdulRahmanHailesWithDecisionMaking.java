package testbed.trustmodel;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;

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

    @Override
    public void initialize(Object... params) {
	super.initialize(params);
	time = 0;
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
	    final Integer best = bestFromWeights(trust);

	    if (null == best) {
		// TODO: This happens only in the first tick, where no
		// experiences exist
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
	return super.getName() + " with decisions";
    }

}
