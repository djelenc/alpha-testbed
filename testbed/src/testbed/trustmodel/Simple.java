package testbed.trustmodel;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;

import testbed.interfaces.Experience;
import testbed.interfaces.Opinion;

public class Simple extends AbstractTrustModel<Double> {
    // cumulative interaction outcomes
    protected double[] exSum;

    // interaction count
    protected int[] exCnt;

    // received opinions
    protected double[][] op;

    // computed reputation
    protected double[] rep;

    @Override
    public void initialize(Object... params) {
	exSum = new double[0];
	exCnt = new int[0];
	op = new double[0][0];
    }

    @Override
    public void processExperiences(Set<Experience> experiences) {
	expandExperiences(experiences);

	for (Experience e : experiences) {
	    exSum[e.agent] += e.outcome;
	    exCnt[e.agent] += 1;
	}
    }

    @Override
    public void processOpinions(Set<Opinion> opinions) {
	expandOpinions(opinions);

	for (Opinion o : opinions) {
	    op[o.agent1][o.agent2] = o.internalTrustDegree;
	}
    }

    @Override
    public void calculateTrust() {
	// pass
    }

    @Override
    public Map<Integer, Double> getTrust(int service) {
	final Map<Integer, Double> trust = new LinkedHashMap<Integer, Double>();

	// compute reputations
	rep = new double[exSum.length];

	for (int target = 0; target < op.length; target++) {
	    double sum = 0;
	    int count = 0;

	    for (int reporter = 0; reporter < op.length; reporter++) {
		if (!Double.isNaN(op[reporter][target])) {
		    sum += op[reporter][target];
		    count += 1;
		}
	    }

	    if (count > 0)
		rep[target] = sum / count;
	    else
		rep[target] = Double.NaN;
	}

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

	return trust;
    }

    @Override
    public void setCurrentTime(int time) {

    }

    /**
     * Expands the supporting array that hold opinions to appropriate length.
     * 
     * @param opinions
     */
    protected void expandOpinions(Set<Opinion> opinions) {
	int max = op.length - 1;

	for (Opinion o : opinions)
	    if (o.agent2 > max || o.agent1 > max)
		max = Math.max(o.agent1, o.agent2);

	if (max > op.length - 1) {
	    expandArrays(max);
	}
    }

    /**
     * Expands the supporting array that holds experiences to appropriate
     * lengths.
     * 
     * @param experience
     */
    protected void expandExperiences(Set<Experience> experience) {
	int max = exSum.length - 1;

	for (Experience e : experience)
	    if (e.agent > max)
		max = e.agent;

	if (max > exSum.length - 1) {
	    expandArrays(max);
	}
    }

    protected void expandArrays(int max) {
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
