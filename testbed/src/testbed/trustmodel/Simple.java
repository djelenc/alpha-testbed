package testbed.trustmodel;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;

import testbed.interfaces.Experience;
import testbed.interfaces.ITrustModel;
import testbed.interfaces.Opinion;

public class Simple extends AbstractTrustModel implements ITrustModel {
    // trust estimations
    private Map<Integer, Double> trust;

    // cumulative interaction outcomes
    private double[] exSum;

    // interaction count
    private int[] exCnt;

    // received opinions
    private double[][] op;

    // computed reputation
    private double[] rep;

    @Override
    public void initialize(Object... params) {
	trust = new LinkedHashMap<Integer, Double>();
	exSum = new double[0];
	exCnt = new int[0];
	op = new double[0][0];
    }

    @Override
    public void calculateTrust(Set<Experience> experience, Set<Opinion> opinions) {
	trust.clear();

	// expand supporting arrays
	expandArray(experience, opinions);

	// process new experiences
	for (Experience e : experience) {
	    exSum[e.agent] += e.outcome;
	    exCnt[e.agent] += 1;
	}

	// process opinions
	for (Opinion o : opinions)
	    op[o.agent1][o.agent2] = o.internalTrustDegree;

	// compute reputations
	rep = new double[exSum.length];

	for (int agent2 = 0; agent2 < op.length; agent2++) {
	    double sum = 0;
	    int count = 0;

	    for (int agent1 = 0; agent1 < op.length; agent1++) {
		if (!Double.isNaN(op[agent1][agent2])) {
		    sum += op[agent1][agent2];
		    count += 1;
		}
	    }

	    if (count > 0)
		rep[agent2] = sum / count;
	    else
		rep[agent2] = Double.NaN;
	}
    }

    @Override
    public Map<Integer, Integer> getRankings(int service) {

	// combine experiences and reputation into trust
	for (int agent = 0; agent < exCnt.length; agent++) {
	    double w_e = 0, w_r = 0, t = 0;

	    // compute weights
	    w_e = Math.min(exCnt[agent], 3) / 3d;
	    w_r = (Double.isNaN(rep[agent]) ? 0 : 1 - w_e);

	    // aggregate data
	    if (w_e > 0 || w_r > 0) {
		if (w_e > 0 && w_r > 0) { // experience & opinions
		    t = w_e * exSum[agent] / exCnt[agent] + w_r * rep[agent];
		} else if (w_r > 0) { // opinions only
		    t = rep[agent];
		} else { // only experiences
		    t = exSum[agent] / exCnt[agent];
		}

		trust.put(agent, t);
	    }
	}

	return super.constructRankingsFromEstimations(trust);
    }

    @Override
    public void setCurrentTime(int time) {

    }

    /**
     * Expands the supporting arrays (experienceCount, experienceSum and
     * opinions) to the appropriate lengths.
     * 
     * @param experience
     * @param opinions
     */
    private void expandArray(Set<Experience> experience, Set<Opinion> opinions) {
	int max = exSum.length - 1;

	for (Experience e : experience)
	    if (e.agent > max)
		max = e.agent;

	for (Opinion o : opinions)
	    if (o.agent2 > max || o.agent1 > max)
		max = Math.max(o.agent1, o.agent2);

	if (max > exSum.length - 1) {
	    double[] newExSum = new double[max + 1];
	    System.arraycopy(exSum, 0, newExSum, 0, exSum.length);
	    exSum = newExSum;

	    int[] newExCnt = new int[max + 1];
	    System.arraycopy(exCnt, 0, newExCnt, 0, exCnt.length);
	    exCnt = newExCnt;

	    double[][] newOp = new double[max + 1][max + 1];

	    for (int i = 0; i < newOp.length; i++)
		for (int j = 0; j < newOp.length; j++)
		    newOp[i][j] = Double.NaN;

	    for (int i = 0; i < op.length; i++)
		System.arraycopy(op[i], 0, newOp[i], 0, op.length);

	    op = newOp;
	}
    }
}
