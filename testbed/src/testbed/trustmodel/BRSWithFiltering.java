/*******************************************************************************
 * Copyright (c) 2013 David Jelenc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0
 * which accompanies this distribution, and is available at
 * http://www.gnu.org/licenses/gpl.html
 * 
 * Contributors:
 *     David Jelenc - initial API and implementation
 ******************************************************************************/
package testbed.trustmodel;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.math.MathException;
import org.apache.commons.math.distribution.BetaDistribution;
import org.apache.commons.math.distribution.BetaDistributionImpl;

import testbed.common.Utils;
import testbed.interfaces.Experience;
import testbed.interfaces.Opinion;
import testbed.interfaces.ParameterCondition;
import testbed.interfaces.ParametersPanel;

/**
 * Beta reputation system with filtering ratings
 * 
 * <p>
 * As presented in <a href=''>﻿TODO</a>
 * 
 * @author David
 * 
 */
public class BRSWithFiltering extends AbstractTrustModel<Double> {

    private int time = 0;

    // aging factors for experiences and opinions
    public double lambdaEx = 1;
    public double lambdaOp = 0;

    /**
     * Factor to scale opinions with (it can be larger than with TRAVOS because
     * this model does not consider the shape of PDF or the area under it)
     */
    public static double FACTOR = 10;
    private static double Q = 0.01;

    // experiences
    public Map<Integer, ArrayList<Experience>> experiences = null;

    public static final BetaDistribution BETA = new BetaDistributionImpl(1, 1);

    public Opinion[][] opinions = null;

    // temporary storage for experiences and opinions
    private List<Experience> exps;
    private List<Opinion> ops;

    @Override
    public void initialize(Object... params) {
	time = 0;
	experiences = new LinkedHashMap<Integer, ArrayList<Experience>>();
	opinions = new Opinion[0][0];

	final ParameterCondition<Double> valLambda = new ParameterCondition<Double>() {
	    @Override
	    public void eval(Double var) {
		if (var < 0 || var > 1)
		    throw new IllegalArgumentException(
			    String.format(
				    "The forgetting factor must be a between 0 and 1 inclusively, but was %.2f",
				    var));
	    }
	};

	final ParameterCondition<Double> valFactor = new ParameterCondition<Double>() {
	    @Override
	    public void eval(Double var) {
		if (var < 1 || var > 50)
		    throw new IllegalArgumentException(
			    String.format(
				    "The multiplier must be a between 1 and 50 inclusively, but was %.2f",
				    var));
	    }
	};

	final ParameterCondition<Double> valQ = new ParameterCondition<Double>() {
	    @Override
	    public void eval(Double var) {
		if (var <= 0 || var >= 0.5)
		    throw new IllegalArgumentException(
			    String.format(
				    "The percentile must be a between 0 and 0.5 exclusively, but was %.2f",
				    var));
	    }
	};

	lambdaEx = Utils.extractParameter(valLambda, 0, params);
	lambdaOp = Utils.extractParameter(valLambda, 1, params);
	Q = Utils.extractParameter(valQ, 2, params);
	FACTOR = Utils.extractParameter(valFactor, 3, params);

	exps = null;
	ops = null;
    }

    @Override
    public void setCurrentTime(int time) {
	this.time = time;
    }

    @Override
    public void processExperiences(List<Experience> experiences) {
	this.exps = experiences;
    }

    @Override
    public void processOpinions(List<Opinion> opinions) {
	this.ops = opinions;
    }

    @Override
    public void calculateTrust() {
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

    @Override
    public Map<Integer, Double> getTrust(int service) {
	final Map<Integer, BRSPair> experienceTrust = computeExperiences();
	final Map<Integer, Double> trust = new LinkedHashMap<Integer, Double>();

	for (int agent = 0; agent < opinions.length; agent++) {
	    final double reputation = filterRatings(agent, experienceTrust);

	    if (!Double.isInfinite(reputation))
		trust.put(agent, reputation);
	}

	return trust;
    }

    /**
     * Returns reputation in the form of an expected value for a particular
     * agent. The method uses iterative filtering by removing outliers from
     * opinions.
     * 
     * <p>
     * If agent Alpha is the only agent who has information about the agent
     * under consideration, then its experiences are returned as result.
     * <p>
     * If a reputation cannot be calculated, the method returns minus infinity.
     * 
     * @param agent
     *            Agent to calculate reputation for
     * @param experienceTrust
     *            The map of past experiences between Alpha agent and the agent
     *            in question.
     * @return
     */
    @SuppressWarnings("deprecation")
    public double filterRatings(int agent, Map<Integer, BRSPair> experienceTrust) {
	List<Integer> allRaters = new ArrayList<Integer>();
	double reputation = Double.NEGATIVE_INFINITY;

	// find all raters
	for (int rater = 0; rater < opinions.length; rater++)
	    if (null != opinions[rater][agent])
		allRaters.add(rater);

	int numRatersPrevious = 0;

	while (numRatersPrevious != allRaters.size()) {
	    numRatersPrevious = allRaters.size();

	    double r = 0, s = 0;

	    if (experienceTrust.containsKey(agent)) {
		r += experienceTrust.get(agent).R;
		s += experienceTrust.get(agent).S;
	    }

	    for (int rater : allRaters) {
		final Opinion o = opinions[rater][agent];
		final double discount = Math.pow(lambdaOp, time - o.time);
		final double rater_r = Math.round(FACTOR
			* o.internalTrustDegree);
		final double rater_s = FACTOR - rater_r;

		r += rater_r * discount;
		s += rater_s * discount;
	    }

	    reputation = (r + 1) / (r + s + 2);

	    final List<Integer> liars = new ArrayList<Integer>();

	    for (int rater : allRaters) {
		final Opinion o = opinions[rater][agent];
		final double discount = Math.pow(lambdaOp, time - o.time);
		final double rater_r = (Math.round(FACTOR
			* o.internalTrustDegree))
			* discount;
		final double rater_s = (FACTOR - rater_r) * discount;

		BETA.setAlpha(1 + rater_r);
		BETA.setBeta(1 + rater_s);

		double l, u;

		try {
		    l = BETA.inverseCumulativeProbability(Q);
		    u = BETA.inverseCumulativeProbability(1 - Q);
		} catch (MathException e) {
		    throw new RuntimeException(e);
		}

		if (l > reputation || u < reputation)
		    liars.add(rater);
	    }

	    // remove liars
	    for (Integer liar : liars)
		allRaters.remove(liar);
	}

	// if no-one but Alpha knows this agent
	if (Double.isInfinite(reputation) && experienceTrust.containsKey(agent)) {
	    final BRSPair p = experienceTrust.get(agent);
	    reputation = (p.R + 1) / (p.R + p.S + 2);
	}

	return reputation;
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

	    boolean experienceExists = false;

	    for (Experience e : exps) {
		experienceExists = true;
		final double discount = Math.pow(lambdaEx, time - e.time);
		final double new_r = Math.round(FACTOR * e.outcome);
		final double new_s = FACTOR - new_r;

		r += new_r * discount;
		s += new_s * discount;
	    }

	    if (experienceExists)
		experienceTrust.put(agent, new BRSPair(r, s));
	}

	return experienceTrust;
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
    private void expandArrays(List<Experience> exp, List<Opinion> ops) {
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
    public ParametersPanel getParametersPanel() {
	return new BRSWithFilteringGUI();
    }

    @Override
    public String toString() {
	return "BRS with filtering";
    }

    @Override
    public void setAgents(List<Integer> agents) {

    }

    @Override
    public void setServices(List<Integer> services) {

    }
}
