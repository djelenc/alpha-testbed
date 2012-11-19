package testbed.trustmodel.qad;

import static java.lang.String.format;
import static testbed.trustmodel.qad.Omega.normalizedNumeric;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import testbed.interfaces.Experience;
import testbed.interfaces.Opinion;
import testbed.interfaces.ParametersPanel;
import testbed.interfaces.RandomGenerator;
import testbed.interfaces.TrustModel;

public class QTM implements TrustModel<Omega> {

    public static final int HISTORY_LENGTH = 10;

    public Map<Integer, QADExp[]> local = null;

    public QADOp[][] opinions = null;
    public double[] credibility = null;

    // temporary storage for opinions and experiences
    private List<Opinion> ops;
    private List<Experience> exps;

    @Override
    public void initialize(Object... params) {
	local = new LinkedHashMap<Integer, QADExp[]>();
	opinions = new QADOp[0][0];
	credibility = new double[0];
	ops = null;
	exps = null;
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
	expandArrays(exps, ops);

	// store opinions
	for (Opinion o : ops)
	    opinions[o.agent1][o.agent2] = new QADOp(o);

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

	    // update credibility of reporters which gave an opinion
	    final Boolean[] correct = new Boolean[opinions.length];
	    final double[] previousWeigts = new double[opinions.length];

	    for (int reporter = 0; reporter < opinions.length; reporter++) {
		final QADOp opinion = opinions[reporter][e.agent];

		// record old weight
		previousWeigts[reporter] = credibility[reporter];

		if (null != opinion) {
		    final Omega actual = normalizedNumeric(e.outcome);
		    final Omega told = opinion.itd;
		    final double diff = Math.abs(actual.ordinal()
			    - told.ordinal());

		    // flag whether the opinion was correct
		    // (when no opinion is given the place remains null)
		    correct[reporter] = Math.abs(diff) < 0.0001;

		    // compute discount factor
		    final double factor = 1 - diff * 0.125;

		    // assign new weight
		    credibility[reporter] *= factor;
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

    public Omega median(Omega[] values) {
	if (null == values)
	    throw new IllegalArgumentException(
		    format("Expected argument type %s, but got %s.",
			    Omega.class, null));

	if (0 == values.length)
	    return null;

	final float[] freq = new float[Omega.values().length];
	int total = 0;

	for (Omega omega : values) {
	    if (null != omega) {
		freq[omega.ordinal()]++;
		total++;
	    }
	}

	for (int i = 0; i < freq.length; i++)
	    freq[i] = freq[i] / total;

	for (int i = 1; i < freq.length; i++)
	    freq[i] += freq[i - 1];

	for (int i = 0; i < freq.length; i++) {
	    if (freq[i] > 0.5)
		return Omega.values()[i];
	}

	return null;
    }

    @Override
    public Map<Integer, Omega> getTrust(int service) {
	Map<Integer, Omega> trust = new LinkedHashMap<Integer, Omega>();

	for (int agent = 0; agent < opinions.length; agent++) {
	    double localRating = 0;
	    int countExp = 0;

	    final QADExp[] localExperiences = local.get(agent);

	    if (null != localExperiences) {
		for (QADExp exp : localExperiences) {
		    if (null != exp) {
			countExp += 1;
			localRating += exp.outcome.numeric;
		    }
		}
	    }

	    localRating = (countExp > 0 ? Math.round(localRating / countExp)
		    : 0);
	    final double weight = ((double) countExp) / HISTORY_LENGTH;

	    double reputation = 0;
	    double credibilitySum = 0;

	    for (int witness = 0; witness < opinions.length; witness++) {
		final QADOp o = opinions[witness][agent];

		if (null != o) {
		    credibilitySum += credibility[witness];
		    reputation += credibility[witness] * o.itd.numeric;
		}
	    }

	    if (0 != Double.compare(credibilitySum, 0d)) {
		reputation = Math.round(reputation / credibilitySum);
	    } else {
		reputation = 0;
	    }

	    final double score = Math.round(weight * localRating + (1 - weight)
		    * reputation);

	    trust.put(agent, Omega.fromNumeric(score));
	}

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
	    QADOp[][] newOp = new QADOp[max + 1][max + 1];

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

    public Map<Integer, Double> computeOld() {
	Map<Integer, Double> trust = new LinkedHashMap<Integer, Double>();

	for (int agent = 0; agent < opinions.length; agent++) {
	    double localRating = 0;
	    int countExp = 0;

	    final QADExp[] localExperiences = local.get(agent);

	    if (null != localExperiences) {
		for (QADExp exp : localExperiences) {
		    if (null != exp) {
			countExp += 1;
			localRating += exp.outcome.numeric;
		    }
		}
	    }

	    localRating = (countExp > 0 ? Math.round(localRating / countExp)
		    : 0);
	    final double weight = ((double) countExp) / HISTORY_LENGTH;

	    double reputation = 0;
	    double credibilitySum = 0;

	    for (int witness = 0; witness < opinions.length; witness++) {
		final QADOp o = opinions[witness][agent];

		if (null != o) {
		    credibilitySum += credibility[witness];
		    reputation += credibility[witness] * o.itd.numeric;
		}
	    }

	    if (0 != Double.compare(credibilitySum, 0d)) {
		reputation = Math.round(reputation / credibilitySum);
	    } else {
		reputation = 0;
	    }

	    final double score = Math.round(weight * localRating + (1 - weight)
		    * reputation);

	    trust.put(agent, score);
	}

	return trust;
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
