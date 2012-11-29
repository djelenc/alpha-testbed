package testbed.trustmodel.qad;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import testbed.interfaces.Experience;
import testbed.interfaces.Opinion;
import testbed.interfaces.ParametersPanel;
import testbed.interfaces.RandomGenerator;
import testbed.interfaces.TrustModel;

public class QTM implements TrustModel<Omega> {

    protected static final double LOWER_CRED = 0.01;
    protected static final double FACTOR_CRED = 0.125;
    protected static final double TF = 0.01;

    protected static final double[] P_T, P_PT, P_U, P_PD, P_D;

    static {
	P_T = new double[] { 0, 0, 0, 0, 1 };
	P_PT = new double[] { 0, 0, 0, 1, 0 };
	P_U = new double[] { 0, 0, 1, 0, 0 };
	P_PD = new double[] { 0, 1, 0, 0, 0 };
	P_D = new double[] { 1, 0, 0, 0, 0 };
    }

    protected static final int HISTORY_LENGTH = 10;

    protected Map<Integer, QADExp[]> local = null;

    protected QADOp[][] opinions = null;
    protected double[] credibility = null;

    protected int time;

    @Override
    public void initialize(Object... params) {
	time = 0;
	local = new LinkedHashMap<Integer, QADExp[]>();
	opinions = new QADOp[0][0];
	credibility = new double[0];
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
		    final int diff = Math
			    .abs(actual.ordinal() - told.ordinal());

		    // flag whether the opinion was correct
		    // (when no opinion is given the this remains null)
		    correct[reporter] = diff <= 1;

		    // compute discount factor
		    // OLD: final double factor = 1 - diff * FACTOR_CRED;
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
	    double totalWeight = 0d;

	    if (null != localExperiences) {
		for (QADExp exp : localExperiences) {
		    if (null != exp) {
			final double weight = Math.exp(-TF * (time - exp.time));
			totalWeight += weight;
			experiences[exp.outcome.ordinal()] += weight;
		    }
		}
	    }

	    // reputation of the selected agent
	    final double[] reputation = new double[5];
	    for (int witness = 0; witness < opinions.length; witness++) {
		final QADOp o = opinions[witness][agent];

		if (null != o) {
		    final double age = Math.exp(-TF * (time - o.time));
		    final double weight = Math.sqrt(credibility[witness] * age);
		    // OLD: reputation[o.itd.ordinal()] += weight;

		    if (weight > 1.01) {
			reputation[o.itd.ordinal()] += 1d;
		    }
		}
	    }

	    double[] normalizedExp = normalize(experiences);

	    if (null == normalizedExp)
		normalizedExp = experiences;

	    double[] normalizedRep = normalize(reputation);

	    if (null == normalizedRep)
		normalizedRep = reputation;

	    // weight of local experiences
	    final double weight = totalWeight / (1 + totalWeight);
	    // OLD: final double weight = totalWeight * totalWeight
	    // / (1 + totalWeight * totalWeight);

	    // common vector
	    final double[] common = new double[5];
	    for (int i = 0; i < reputation.length; i++) {
		common[i] = weight * normalizedExp[i] + (1 - weight)
			* normalizedRep[i];
	    }

	    final Omega td = qualtitativeAverage(common);

	    if (null != td) {
		trust.put(agent, td);
	    }
	}

	return trust;
    }

    protected double[] normalize(double[] freq) {
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

    protected Omega qualtitativeAverage(double[] freq) {
	double[] normalized = normalize(freq);

	if (null == normalized)
	    return null;

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

    protected double distance(double[] vector1, double[] vector2) {
	double sum = 0;

	for (int i = 0; i < vector2.length; i++) {
	    sum += (vector1[i] - vector2[i]) * (vector1[i] - vector2[i]);
	}

	return sum;
    }

    @Override
    public void setCurrentTime(int time) {
	this.time = time;
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
	}
    }

    protected RandomGenerator generator;

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
