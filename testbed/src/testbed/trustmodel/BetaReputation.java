package testbed.trustmodel;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;

import testbed.common.Utils;
import testbed.interfaces.Experience;
import testbed.interfaces.ICondition;
import testbed.interfaces.IParametersPanel;
import testbed.interfaces.ITrustModel;
import testbed.interfaces.Opinion;

/**
 * Beta reputation system
 * 
 * <p>
 * As presented in <a href='http://aisel.aisnet.org/bled2002/41'>﻿Audun Jøsang
 * and Roslan Ismail. The beta reputation system. Proceedings of the 15th Bled
 * Electronic Commerce Conference (Vol. 160), 2002.</a>
 * 
 * @author David
 * 
 */
public class BetaReputation extends AbstractTrustModel implements ITrustModel {

    private int time = 0;

    // aging factors for experiences and opinions
    public double lambdaEx = 0.9;
    public double lambdaOp = 0;

    public Map<Integer, ArrayList<Experience>> experiences = null;

    public Opinion[][] opinions = null;

    @Override
    public void initialize(Object... params) {
	time = 0;
	experiences = new LinkedHashMap<Integer, ArrayList<Experience>>();
	opinions = new Opinion[0][0];

	final ICondition<Double> validator = new ICondition<Double>() {
	    @Override
	    public void eval(Double var) {
		if (var < 0 || var > 1)
		    throw new IllegalArgumentException(
			    String.format(
				    "The forgetting factor must be a between 0 and 1 inclusively, but was %.2f",
				    var));
	    }
	};

	lambdaEx = Utils.extractParameter(validator, 0, params);
	lambdaOp = Utils.extractParameter(validator, 1, params);
    }

    @Override
    public void setCurrentTime(int time) {
	this.time = time;
    }

    @Override
    public void calculateTrust(Set<Experience> exps, Set<Opinion> ops) {
	// make room for new values
	expandArrays(exps, ops);

	// store experiences
	for (Experience e : exps) {
	    ArrayList<Experience> list = experiences.get(e.agent);

	    if (null == list) {
		list = new ArrayList<Experience>();
		list.add(e);
		experiences.put(e.agent, list);
	    } else {
		list.add(e);
	    }
	}

	// store opinions
	for (Opinion o : ops)
	    opinions[o.agent1][o.agent2] = o;
    }

    public Map<Integer, BRSPair> compute() {
	final Map<Integer, BRSPair> experienceTrust = computeExperiences();
	final Map<Integer, BRSPair> opinionTrust = computeOpinions(experienceTrust);

	final Map<Integer, BRSPair> trust = new LinkedHashMap<Integer, BRSPair>();

	// combine experiences and discounted opinions
	for (Map.Entry<Integer, BRSPair> e : experienceTrust.entrySet()) {
	    final int agent = e.getKey();
	    double r = e.getValue().R;
	    double s = e.getValue().S;

	    if (opinionTrust.containsKey(agent)) {
		r += opinionTrust.get(agent).R;
		s += opinionTrust.get(agent).S;
	    }

	    trust.put(agent, new BRSPair(r, s));
	}

	// add opinions from agents with which Alpha has no experiences
	for (Map.Entry<Integer, BRSPair> o : opinionTrust.entrySet()) {
	    final int agent = o.getKey();
	    final double r = o.getValue().R;
	    final double s = o.getValue().S;

	    if (!experienceTrust.containsKey(agent))
		trust.put(agent, new BRSPair(r, s));
	}

	return trust;
    }

    @Override
    public Map<Integer, Integer> getRankings(int service) {
	final Map<Integer, Double> ev = new LinkedHashMap<Integer, Double>();
	final Map<Integer, BRSPair> trust = compute();

	for (Map.Entry<Integer, BRSPair> entry : trust.entrySet()) {
	    final double r = entry.getValue().R;
	    final double s = entry.getValue().S;
	    ev.put(entry.getKey(), (r + 1) / (r + s + 2));
	}

	return super.constructRankingsFromEstimations(ev);
    }

    /**
     * Returns a map of aggregated experience tuples. Keys represents agents and
     * their values represent an aggregated vector of their past interaction
     * outcomes.
     * 
     * @return
     */
    public Map<Integer, BRSPair> computeExperiences() {
	Map<Integer, BRSPair> experienceTrust = new LinkedHashMap<Integer, BRSPair>();

	for (Map.Entry<Integer, ArrayList<Experience>> entry : experiences
		.entrySet()) {
	    ArrayList<Experience> exps = entry.getValue();
	    final int agent = entry.getKey();

	    double r = 0, s = 0;

	    for (int i = 0; i < exps.size(); i++) {
		// discount is computed relatively: could also be absolutely
		final double discount = Math.pow(lambdaEx, exps.size() - 1 - i);
		final Experience e = exps.get(i);

		r += e.outcome * discount;
		s += (1 - e.outcome) * discount;
	    }

	    experienceTrust.put(agent, new BRSPair(r, s));
	}

	return experienceTrust;
    }

    /**
     * Returns a map of aggregated opinions. Keys represent agents and values
     * represent their reputation tuples (aggregated opinions). Values are
     * discounted with experience before they are aggregated.
     * 
     * If no experiences exist, opinions are discarded, because their
     * discounting factor is 0.
     * 
     * @param experienceTrust
     * @return
     */
    public Map<Integer, BRSPair> computeOpinions(
	    Map<Integer, BRSPair> experienceTrust) {
	Map<Integer, BRSPair> opinionTrust = new LinkedHashMap<Integer, BRSPair>();

	for (int agent = 0; agent < opinions.length; agent++) {
	    double r = 0, s = 0;

	    // only compute opinion if an experience with the provider exists
	    boolean computeOpinion = false;

	    for (int reporter = 0; reporter < opinions[agent].length; reporter++) {
		final Opinion o = opinions[reporter][agent];

		if (o != null) {
		    // opinion is discounted absolutely
		    final double discount = Math.pow(lambdaOp, time - o.time);
		    final BRSPair p = experienceTrust.get(reporter);

		    if (null != p) {
			computeOpinion = true;
			r += (2 * p.R * o.internalTrustDegree)
				/ ((p.S + 2) * 3 + 2 * p.R) * discount;
			s += (2 * p.R * (1 - o.internalTrustDegree))
				/ ((p.S + 2) * 3 + 2 * p.R) * discount;
		    }
		}
	    }

	    if (computeOpinion)
		opinionTrust.put(agent, new BRSPair(r, s));
	}

	return opinionTrust;
    }

    /**
     * Expands the supporting data structures, which contain data of past
     * interactions and received opinions.
     * 
     * @param exp
     *            Set of experiences
     * @param ops
     *            Set of opinions
     */
    private void expandArrays(Set<Experience> exp, Set<Opinion> ops) {
	final int limit = opinions.length - 1;
	int max = limit;

	for (Experience e : exp)
	    if (e.agent > max)
		max = e.agent;

	for (Opinion o : ops)
	    if (o.agent2 > max || o.agent1 > max)
		max = Math.max(o.agent1, o.agent2);

	if (max > limit) {
	    // copy opinions
	    Opinion[][] newOp = new Opinion[max + 1][max + 1];
	    for (int i = 0; i < opinions.length; i++)
		System.arraycopy(opinions[i], 0, newOp[i], 0,
			opinions[i].length);

	    opinions = newOp;
	}
    }

    @Override
    public IParametersPanel getParametersPanel() {
	return new BetaReputationGUI();
    }
}
