package testbed.trustmodel;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;

import testbed.interfaces.Experience;
import testbed.interfaces.ITrustModel;
import testbed.interfaces.Opinion;

/**
 * Trust model that only uses opinions. The opinions are cached, thus the model
 * considers opinions from previous ticks. If new opinions are obtained, the old
 * ones are discarded.
 * 
 * @author David
 * 
 */
public class OnlyOpinions extends AbstractTrustModel implements ITrustModel {
    private Map<Integer, Double> trust;

    // opinions
    private double[][] op;

    private Set<Opinion> opinions;

    @Override
    public void initialize(Object... params) {
	op = new double[0][0];
	trust = new LinkedHashMap<Integer, Double>();
	opinions = null;
    }

    @Override
    public void processExperiences(Set<Experience> experiences) {

    }

    @Override
    public void processOpinions(Set<Opinion> opinions) {
	this.opinions = opinions;
    }

    @Override
    public void calculateTrust() {
	trust.clear();

	expandArray(opinions);

	for (Opinion o : opinions)
	    op[o.agent1][o.agent2] = o.internalTrustDegree;

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
		trust.put(agent2, sum / count);
	}

    }

    @Override
    public Map<Integer, Integer> getRankings(int service) {
	return super.constructRankingsFromEstimations(trust);
    }

    @Override
    public String getName() {
	return "Opinions only";
    }

    @Override
    public void setCurrentTime(int time) {
    }

    private void expandArray(Set<Opinion> opinions) {
	int max = op.length - 1;

	for (Opinion o : opinions)
	    if (o.agent2 > max || o.agent1 > max)
		max = Math.max(o.agent1, o.agent2);

	// resize arrays
	if (max > op.length - 1) {
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
