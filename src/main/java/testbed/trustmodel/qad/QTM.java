/*
 * Copyright (c) 2013 David Jelenc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0
 * which accompanies this distribution, and is available at
 * http://www.gnu.org/licenses/gpl.html
 * 
 * Contributors:
 *     David Jelenc - initial API and implementation
 */
package testbed.trustmodel.qad;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import testbed.interfaces.Experience;
import testbed.interfaces.Opinion;
import testbed.interfaces.ParametersPanel;
import testbed.interfaces.RandomGenerator;
import testbed.interfaces.TrustModel;

/**
 * A Qualitative Trust Model as proposed by Jelenc and Trček. The paper is
 * currently under review in Journal of Autonomous Agents and Multi-Agent
 * Systems.
 * 
 * @author David
 * 
 */
public class QTM implements TrustModel<Omega> {

    protected static final double LOWER_CRED = 0.001;
    protected static final double TF = 0.1; // 0.01

    protected static final double[] P_T, P_PT, P_U, P_PD, P_D;

    static {
	P_T = new double[] { 0, 0, 0, 0, 1 };
	P_PT = new double[] { 0, 0, 0, 1, 1 };
	P_U = new double[] { 0, 0, 1, 1, 1 };
	P_PD = new double[] { 0, 1, 1, 1, 1 };
	P_D = new double[] { 1, 1, 1, 1, 1 };
    }

    protected static final int HISTORY_LENGTH = 10;

    protected Map<Integer, QADExp[]> local = null;

    protected QADOp[][] opinions = null;
    protected double[] credibility = null;
    protected double[] cntCorrect = null;
    protected double[] cntWrong = null;

    protected int time;

    protected RandomGenerator generator;

    @Override
    public void initialize(Object... params) {
	time = 0;
	local = new LinkedHashMap<Integer, QADExp[]>();
	opinions = new QADOp[0][0];
	credibility = new double[0];
	cntCorrect = new double[0];
	cntWrong = new double[0];
    }

    @Override
    public void processOpinions(List<Opinion> ops) {
	expandArrays(null, ops);

	// store opinions
	for (Opinion o : ops)
	    opinions[o.agent1][o.agent2] = new QADOp(o);
    }

    @Override
    public void processExperiences(List<Experience> exps) {
	expandArrays(exps, null);

	// store new experiences
	for (Experience e : exps) {
	    QADExp[] history = local.get(e.agent);

	    if (null == history) { // if no history
		history = new QADExp[HISTORY_LENGTH];
		local.put(e.agent, history);
	    } else { // shift values
		System.arraycopy(history, 0, history, 1, history.length - 1);
	    }

	    history[0] = new QADExp(e);

	    // update credibility of reporters that gave opinion about e.agent
	    final Boolean[] correct = new Boolean[opinions.length];
	    final double[] previousWeigts = new double[opinions.length];

	    for (int reporter = 0; reporter < opinions.length; reporter++) {
		final QADOp opinion = opinions[reporter][e.agent];

		// record old weight
		previousWeigts[reporter] = credibility[reporter];

		if (null != opinion) {
		    final Omega actual = Omega.normalizedNumeric(e.outcome);
		    final Omega told = opinion.itd;

		    final int diff;
		    diff = Math.abs(actual.ordinal() - told.ordinal());

		    // flag whether the opinion was correct
		    // (when no opinion is given the this remains null)
		    correct[reporter] = diff <= 1;

		    if (correct[reporter]) {
			cntCorrect[reporter] += 1;
		    } else {
			cntWrong[reporter] += 1;
		    }

		    // compute discount factor
		    final double factor = (correct[reporter] ? 1d : 0.5);

		    // assign new weight
		    credibility[reporter] *= factor;

		    // check credibility for lower bound
		    if (credibility[reporter] < LOWER_CRED)
			credibility[reporter] = LOWER_CRED;
		}
	    }

	    // normalize weights
	    double previousSum = 0, currentSum = 0;
	    for (int reporter = 0; reporter < correct.length; reporter++) {
		if (null != correct[reporter]) {
		    previousSum += previousWeigts[reporter];
		    currentSum += credibility[reporter];
		}
	    }

	    final double factor = previousSum / currentSum;

	    for (int reporter = 0; reporter < correct.length; reporter++)
		if (null != correct[reporter])
		    credibility[reporter] *= factor;
	}
    }

    @Override
    public void calculateTrust() {

    }

    @Override
    public Map<Integer, Omega> getTrust(int service) {
	final Map<Integer, Omega> trust = new LinkedHashMap<Integer, Omega>();

	for (int agent = 0; agent < opinions.length; agent++) {
	    // local experiences and their weights
	    final double[] experiences = new double[5];
	    final QADExp[] localExperiences = local.get(agent);
	    double expWeight = 0d;

	    if (null != localExperiences) {
		for (QADExp exp : localExperiences) {
		    if (null != exp) {
			final double weight = Math.exp(-TF * (time - exp.time));
			expWeight += weight;
			experiences[exp.outcome.ordinal()] += weight;
		    }
		}
	    }

	    // reputation of the selected agent
	    final double[] reputation = new double[5];

	    for (int witness = 0; witness < opinions.length; witness++) {
		final QADOp o = opinions[witness][agent];

		if (null != o) {
		    // CONNECTEDNESS
		    // number of mutual acquaintances
		    int mutual = 0;

		    // number of combined acquaintances
		    int combined = 0;

		    for (int i = 0; i < opinions.length; i++) {
			// skip mutual opinions
			if (i == agent || i == witness)
			    continue;

			final QADOp o1 = opinions[witness][i];
			final QADOp o2 = opinions[agent][i];

			if (o1 != null && o2 != null) {
			    // agent and witness know this agent
			    mutual += 1;
			    combined += 1;
			} else if ((o1 == null && o2 != null)
				|| (o1 != null && o2 == null)) {
			    // only one of them knows it
			    combined += 1;
			} else if (o1 == null && o2 == null) {
			    // neither of them knows this agent
			} else {
			    // this should never execute
			    throw new Error("Unreachable code.");
			}
		    }

		    // TODO: increase connectedness for mutual opinions
		    final double connectedness, recency, weight,
			    providerTrustworthiness, pastAccuracy;

		    pastAccuracy = 1d / (1d + Math
			    .exp(cntWrong[witness] - cntCorrect[witness]));

		    providerTrustworthiness = Math
			    .sqrt(credibility[witness] * pastAccuracy);

		    connectedness = (mutual + 0d) / combined;

		    recency = Math.exp(-TF * (time - o.time));

		    weight = Math.min(Math.min(connectedness, recency),
			    providerTrustworthiness);

		    reputation[o.itd.ordinal()] += weight;
		}
	    }

	    double[] normalizedExp = normalize(experiences);

	    if (null == normalizedExp)
		normalizedExp = experiences;

	    double[] normalizedRep = normalize(reputation);

	    if (null == normalizedRep) {
		normalizedRep = reputation;
	    }

	    // weight of local experiences
	    final double confidence = expWeight / (1 + expWeight);

	    // common vector
	    final double[] common = new double[5];
	    for (int i = 0; i < reputation.length; i++) {
		common[i] = confidence * normalizedExp[i]
			+ (1 - confidence) * normalizedRep[i];
	    }

	    final Omega td = qualtitativeAverage(common);

	    if (null != td) {
		trust.put(agent, td);
	    }
	}

	return trust;
    }

    public double[] normalize(double[] freq) {
	final double[] result = new double[freq.length];
	double sum = 0;

	for (int i = 0; i < freq.length; i++)
	    sum += freq[i];

	if (Math.abs(sum) < 0.00001)
	    return null;

	for (int i = 0; i < freq.length; i++)
	    result[i] = freq[i] / sum;

	return result;
    }

    public Omega qualtitativeAverage(double[] freq) {
	double[] normalized = normalize(freq);

	if (null == normalized)
	    return null;

	// make it cumulative
	normalized[1] += normalized[0];
	normalized[2] += normalized[1];
	normalized[3] += normalized[2];
	normalized[4] = 1d;

	double[] distances = new double[] { distance(normalized, P_D),
		distance(normalized, P_PD), distance(normalized, P_U),
		distance(normalized, P_PT), distance(normalized, P_T) };

	int minIndx = 0;
	double minValue = Double.MAX_VALUE;

	for (int i = 0; i < distances.length; i++) {
	    if (distances[i] <= minValue) {
		minIndx = i;
		minValue = distances[i];
	    }
	}

	return Omega.values()[minIndx];
    }

    public double distance(double[] vector1, double[] vector2) {
	double sum = 0;

	for (int i = 0; i < vector2.length; i++) {
	    sum += (vector1[i] - vector2[i]) * (vector1[i] - vector2[i]);
	}

	return sum;
    }

    @Override
    public void setCurrentTime(int time) {
	this.time = time;

	for (int i = 0; i < cntCorrect.length; i++) {
	    cntCorrect[i] *= Math.exp(-TF);
	    cntWrong[i] *= Math.exp(-TF);
	}
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

	if (null != exp)
	    for (Experience e : exp)
		if (e.agent > max)
		    max = e.agent;

	if (null != ops)
	    for (Opinion o : ops)
		if (o.agent2 > max || o.agent1 > max)
		    max = Math.max(o.agent1, o.agent2);

	if (max > limit) {
	    // copy opinions
	    final QADOp[][] newOp = new QADOp[max + 1][max + 1];
	    for (int i = 0; i < opinions.length; i++)
		System.arraycopy(opinions[i], 0, newOp[i], 0,
			opinions[i].length);

	    opinions = newOp;

	    // credibility weights
	    final double[] newWeights = new double[max + 1];

	    for (int i = 0; i < newWeights.length; i++)
		newWeights[i] = 1d;

	    System.arraycopy(credibility, 0, newWeights, 0, credibility.length);
	    credibility = newWeights;

	    // correct / wrong
	    final double[] newCntCorrect = new double[max + 1];
	    System.arraycopy(cntCorrect, 0, newCntCorrect, 0,
		    cntCorrect.length);
	    cntCorrect = newCntCorrect;

	    final double[] newCntWrong = new double[max + 1];
	    System.arraycopy(cntWrong, 0, newCntWrong, 0, cntWrong.length);
	    cntWrong = newCntWrong;
	}
    }

    @Override
    public void setAgents(List<Integer> agents) {
    }

    @Override
    public void setServices(List<Integer> services) {
    }

    @Override
    public void setRandomGenerator(RandomGenerator generator) {
	this.generator = generator;
    }

    @Override
    public ParametersPanel getParametersPanel() {
	return null;
    }

    @Override
    public String toString() {
	return "Qualitative model";
    }
}
