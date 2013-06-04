package testbed.trustmodel;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import testbed.interfaces.Experience;
import testbed.interfaces.Opinion;

/**
 * Trust model of Yu, Singh and Sycara
 * 
 * 
 * <p>
 * As proposed in <a href=
 * "http://ieeexplore.ieee.org/stamp/stamp.jsp?tp=&arnumber=1368412&isnumber=29953"
 * > Bin Yu, Munidar P. Singh, Katia Sycara, Developing trust in large-scale
 * peer-to-peer systems, Multi-Agent Security and Survivability, 2004 IEEE First
 * Symposium on, 2004</a>
 * 
 * @author David
 * 
 */
public class YuSinghSycara extends AbstractTrustModel<Double> {

    // discount factor for liars
    public static final double BETA = 0.5;
    public static final int HISTORY_LENGTH = 10;

    public Map<Integer, double[]> local = null;

    public Opinion[][] opinions = null;
    public double[] credibility = null;

    @Override
    public void initialize(Object... params) {
	local = new LinkedHashMap<Integer, double[]>();
	opinions = new Opinion[0][0];
	credibility = new double[0];
    }

    @Override
    public void processExperiences(List<Experience> exps) {
	expandArrays(exps, null);

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

    @Override
    public void processOpinions(List<Opinion> ops) {
	expandArrays(null, ops);

	for (Opinion o : ops)
	    opinions[o.agent1][o.agent2] = o;
    }

    @Override
    public void calculateTrust() {
	// weirdly empty.
    }

    public Map<Integer, Double> getTrust(int service) {
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

	/*
	 * StringBuffer sb = new StringBuffer(); sb.append("credibility = {");
	 * 
	 * for (int i = 0; i < credibility.length; i++) {
	 * sb.append(String.format("%d:%.7f, ", i, credibility[i])); }
	 * sb.append("}");
	 * 
	 * System.out.println(sb.toString());
	 */

	return trust;
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
    protected void expandArrays(List<Experience> exp, List<Opinion> ops) {
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

    @Override
    public String toString() {
	return "Yu, Singh, Sycara";
    }
}
