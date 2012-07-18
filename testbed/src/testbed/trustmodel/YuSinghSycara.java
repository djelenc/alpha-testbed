package testbed.trustmodel;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;

import testbed.interfaces.Experience;
import testbed.interfaces.ITrustModel;
import testbed.interfaces.Opinion;

public class YuSinghSycara extends AbstractTrustModel implements ITrustModel {

    // discount factor for liars
    public static final double BETA = 0.5;
    public static final int HISTORY_LENGTH = 10;

    public Map<Integer, double[]> local = null;

    public Opinion[][] opinions = null;
    public double[] credibility = null;

    // temporary storage for experiences and opinions
    private Set<Experience> exps;
    private Set<Opinion> ops;

    @Override
    public void initialize(Object... params) {
	local = new LinkedHashMap<Integer, double[]>();
	opinions = new Opinion[0][0];
	credibility = new double[0];

	exps = null;
	ops = null;
    }

    @Override
    public void processExperiences(Set<Experience> experiences) {
	this.exps = experiences;
    }

    @Override
    public void processOpinions(Set<Opinion> opinions) {
	this.ops = opinions;
    }

    @Override
    public void calculateTrust() {
	expandArrays(exps, ops);

	// store opinions
	for (Opinion o : ops)
	    opinions[o.agent1][o.agent2] = o;

	// store new experiences
	for (Experience e : exps) {
	    double[] history = local.get(e.agent);

	    if (null == history) { // if no history
		history = new double[HISTORY_LENGTH];
		local.put(e.agent, history);
	    } else { // shift values
		System.arraycopy(history, 0, history, 1, history.length - 1);
	    }

	    history[0] = e.outcome;

	    // update credibility of agents who gave an opinion about this agent
	    for (int reporter = 0; reporter < opinions.length; reporter++) {
		final Opinion opinion = opinions[reporter][e.agent];

		if (null != opinion) {
		    final double factor = 1 - (1 - BETA)
			    * Math.abs(opinion.internalTrustDegree - e.outcome);

		    credibility[reporter] *= factor;
		}
	    }
	}

    }

    public Map<Integer, Double> compute() {
	Map<Integer, Double> trust = new LinkedHashMap<Integer, Double>();

	for (int agent = 0; agent < opinions.length; agent++) {
	    double localRating = 0;
	    int countExp = 0;

	    final double[] localExperiences = local.get(agent);

	    if (null != localExperiences) {
		for (double exp : localExperiences) {
		    if (0 != Double.compare(exp, 0d)) {
			countExp += 1;
			localRating += exp;
		    }
		}
	    }

	    localRating = (countExp > 0 ? localRating / countExp : 0);
	    final double weight = ((double) countExp) / HISTORY_LENGTH;

	    double reputation = 0;
	    double credibilitySum = 0;

	    for (int witness = 0; witness < opinions.length; witness++) {
		final Opinion o = opinions[witness][agent];

		if (null != o) {
		    credibilitySum += credibility[witness];
		    reputation += credibility[witness] * o.internalTrustDegree;
		}
	    }

	    // This should originally be like this:
	    // if (0 != Double.compare(numberOfWitnesses, 0d)) {
	    // reputation = reputation / numberOfWitnesses;
	    // but it works horribly -- it's a mistake

	    if (0 != Double.compare(credibilitySum, 0d)) {
		reputation = reputation / credibilitySum;
	    } else {
		reputation = 0.5;
	    }

	    final double score = weight * localRating + (1 - weight)
		    * reputation;

	    trust.put(agent, score);
	}

	return trust;
    }

    @Override
    public Map<Integer, Integer> getRankings(int service) {
	return constructRankingsFromEstimations(compute());
    }

    @Override
    public void setCurrentTime(int time) {

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

	    // copy opinion weights
	    double[] newWeights = new double[max + 1];

	    for (int i = 0; i < newWeights.length; i++)
		newWeights[i] = 1d;

	    System.arraycopy(credibility, 0, newWeights, 0, credibility.length);
	    credibility = newWeights;
	}
    }
}
