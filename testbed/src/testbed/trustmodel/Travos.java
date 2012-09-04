package testbed.trustmodel;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;

import org.apache.commons.math.MathException;
import org.apache.commons.math.distribution.BetaDistribution;
import org.apache.commons.math.distribution.BetaDistributionImpl;

import testbed.common.Utils;
import testbed.interfaces.Experience;
import testbed.interfaces.ParameterCondition;
import testbed.interfaces.ParametersPanel;
import testbed.interfaces.Opinion;

/**
 * TRAVOS trust and reputation model
 * 
 * <p>
 * As proposed in <a href='http://dx.doi.org/10.1007/s10458-006-5952-x'>W. T.
 * Teacy, Jigar Patel, Nicholas R. Jennings, and Michael Luck. 2006. TRAVOS:
 * Trust and Reputation in the Context of Inaccurate Information Sources.
 * Autonomous Agents and Multi-Agent Systems 12, 2 (March 2006), 183-198.</a>
 * 
 * <p>
 * <b>Additional comment.</b> TRAVOS requires two specific data formats: a)
 * binary interactions outcomes and b) exchanged opinions in the form of
 * 2-dimensional vector of natural numbers. I solved this by:
 * <ul>
 * <li>Scaling interaction outcomes by a FACTOR and then computing a pair (m, n)
 * from this scaled number. This can be explained as if an interaction consists
 * of several sub-interactions all of which have a binary outcome and the final
 * outcome is a pair of successful and unsuccessful interactions (it is
 * basically a mapping: [0, 1] &rarr; N &times; N)
 * <li>Scaling internalTrustDegrees from opinions by the same FACTOR and
 * computing the pair in the same way as interaction outcomes. This gives all
 * opinions the same weight (m + n = FACTOR). The FACTOR also needs to be large
 * enough so that an opinion has a chance of falling into every possible bin.
 * </ul>
 * 
 * <p>
 * An alternative to this would be to program the model exactly as proposed,
 * then put larger values for sd_i and sd_o in scenarios, and finally define a
 * threshold function to determine the binary interaction outcome.
 * 
 * @author David
 * 
 */
public class Travos extends AbstractTrustModel<Double> {
    protected static final ParameterCondition<Double> VAL_MULTPLIER,
	    VAL_THRESHOLD;

    static {
	VAL_MULTPLIER = new ParameterCondition<Double>() {
	    @Override
	    public void eval(Double var) {
		if (var < 1 || var > 50)
		    throw new IllegalArgumentException(
			    String.format(
				    "The multiplier must be a between 1 and 50 inclusively, but was %.2f",
				    var));
	    }
	};

	VAL_THRESHOLD = new ParameterCondition<Double>() {
	    @Override
	    public void eval(Double var) {
		if (var < 0 || var > 1)
		    throw new IllegalArgumentException(
			    String.format(
				    "The threshold/error must be a between 0 and 1 inclusively, but was %.2f",
				    var));
	    }
	};
    }

    // experiences
    public Map<Integer, BRSPair> experiences = null;

    // opinions
    public Opinion[][] opinions = null;

    // observations about opinions
    public Map<Integer, BRSPair[]> observations = null;

    public static double ERROR = 0.2;
    public static double THRESHOLD = 0.95;
    public static double FACTOR = 5;

    protected static final BetaDistribution BETA = new BetaDistributionImpl(1,
	    1);

    @Override
    public void initialize(Object... params) {
	experiences = new LinkedHashMap<Integer, BRSPair>();
	observations = new LinkedHashMap<Integer, BRSPair[]>();
	opinions = new Opinion[0][0];

	FACTOR = Utils.extractParameter(VAL_MULTPLIER, 0, params);
	THRESHOLD = Utils.extractParameter(VAL_THRESHOLD, 1, params);
	ERROR = Utils.extractParameter(VAL_THRESHOLD, 2, params);
    }

    @Override
    public void processExperiences(Set<Experience> exps) {
	// expand data structures
	expandExperiences(exps);

	// store experiences
	for (Experience e : exps) {
	    BRSPair p = experiences.get(e.agent);

	    final double r = Math.round(FACTOR * e.outcome);
	    final double s = FACTOR - r;

	    if (p == null) {
		p = new BRSPair(r, s);
		experiences.put(e.agent, p);
	    } else {
		p.R += r;
		p.S += s;
	    }

	    // Update deception detection mechanism
	    for (int agent = 0; agent < opinions.length; agent++) {

		// if an opinion exists
		if (null != opinions[agent][e.agent]) {
		    final BRSPair[] obs = observations.get(agent);

		    // determine the bin
		    final double op_r = Math.round(FACTOR
			    * opinions[agent][e.agent].internalTrustDegree);
		    final double op_s = FACTOR - op_r;

		    final int bin = determineBin(op_r, op_s);

		    // store the actual value into bin
		    obs[bin].R += r;
		    obs[bin].S += s;
		}
	    }
	}
    }

    @Override
    public void processOpinions(Set<Opinion> ops) {
	// expand data structures
	expandOpinions(ops);

	// store opinions
	for (Opinion o : ops)
	    opinions[o.agent1][o.agent2] = o;
    }

    @Override
    public void calculateTrust() {
	// weird.
    }

    /**
     * Determines the bin, in which the outcome for the opinion will be stored
     * into.
     * 
     * @param r
     * @param s
     * @return
     */
    public int determineBin(double r, double s) {
	final double mean = (r + 1) / (r + s + 2);
	double border = 0.0;
	final double d = 0.2;
	int index = -1;

	do {
	    border += d;
	    index += 1;
	} while (mean > border);

	return index;
    }

    /**
     * Integrates the Beta probability density function. Parameter alpha is
     * defined by m + 1, and Beta by n + 1. The PDF is integrated between low
     * and high values, respectively.
     * 
     * @param m
     *            For setting alpha
     * @param n
     *            For setting beta
     * @param low
     *            Lower integration bound
     * @param high
     *            Higher integration bound
     * @return The integral
     */
    @SuppressWarnings("deprecation")
    public double integrate(double m, double n, double low, double high) {
	BETA.setAlpha(m + 1);
	BETA.setBeta(n + 1);

	try {
	    return BETA.cumulativeProbability(high)
		    - BETA.cumulativeProbability(low);
	} catch (MathException e) {
	    throw new RuntimeException(e);
	}
    }

    @Override
    public Map<Integer, Double> getTrust(int service) {
	// trust evaluations
	Map<Integer, Double> trust = new LinkedHashMap<Integer, Double>();

	for (Map.Entry<Integer, BRSPair> e : experiences.entrySet()) {
	    final BRSPair p = e.getValue();
	    final int agent = e.getKey();

	    final double mean = (p.R + 1) / (p.R + p.S + 2);
	    final double confidence = integrate(p.R, p.S, mean - ERROR, mean
		    + ERROR);

	    // if confidence is high enough this is the final score
	    if (confidence > THRESHOLD)
		trust.put(agent, mean);
	}

	// compute reputation towards remaining agents
	for (int agent = 0; agent < opinions.length; agent++) {
	    // flag to denote whether an agent actually exists
	    // (because some IDs may not be used)
	    boolean agentExists = false;

	    // omit the cases when trust has been computed only from experiences
	    if (!trust.containsKey(agent)) {
		final BRSPair rep = new BRSPair();

		// add experiences (if they exist)
		// this part is not clear from the paper
		if (experiences.containsKey(agent)) {
		    rep.R += experiences.get(agent).R;
		    rep.S += experiences.get(agent).S;
		    agentExists = true;
		}

		for (int reporter = 0; reporter < opinions.length; reporter++) {
		    final Opinion o = opinions[reporter][agent];

		    // if opinion exists
		    if (null != o) {
			agentExists = true;
			// compute (m, n) from the opinion
			final double m = Math.round(FACTOR
				* o.internalTrustDegree);
			final double n = FACTOR - m;

			// determine the bin of this opinion
			final int bin = determineBin(m, n);

			// get values from the bin
			final BRSPair obs = observations.get(reporter)[bin];

			// compute probability of accuracy
			double p_acc = integrate(obs.R, obs.S, bin * 0.2,
				bin * 0.2 + 0.2);

			// compute adjusted mean and adjusted standard deviation
			final double a_mean = adjustMean(m, n, p_acc);
			final double a_std = adjustSD(m, n, p_acc);

			// compute adjusted m and n
			// and add them to the reputation
			rep.R += scaleM(a_mean, a_std);
			rep.S += scaleN(a_mean, a_std);
		    }
		}

		if (agentExists)
		    trust.put(agent, (rep.R + 1) / (rep.R + rep.S + 2));
	    }
	}

	return trust;
    }

    /**
     * @param m
     * @param n
     * @param p_acc
     * @param std0
     * @return
     */
    public double adjustSD(final double m, final double n, double p_acc) {
	return 0.288675 + p_acc * (standardDeviation(m, n) - 0.288675);
    }

    /**
     * Computes standard deviation of a Beta probability function. Parameters
     * alpha and beta are computed by adding 1 to m and n, respectively.
     * 
     * @param m
     *            For setting alpha
     * @param n
     *            For setting beta
     * @return Standard deviation
     */
    public double standardDeviation(double m, double n) {
	final double var = (m + 1) * (n + 1) / (m + 1 + n + 1)
		/ (m + 1 + n + 1) / (m + 1 + n + 1 + 1);

	return Math.sqrt(var);
    }

    /**
     * Adjusts mean
     * 
     * @param m
     *            Number of successful interactions
     * @param n
     *            Number of unsuccessful interactions
     * @param p_acc
     *            Probability of accuracy
     * @return Adjusted mean
     */
    public double adjustMean(double m, double n, double p_acc) {
	return 0.5 + p_acc * ((m + 1) / (m + n + 2) - 0.5);
    }

    /**
     * Computes scaled number of successful interactions.
     * 
     * @param a_mean
     * @param a_std
     * @return
     */
    public double scaleM(double a_mean, double a_std) {
	return (a_mean * a_mean - a_mean * a_mean * a_mean) / (a_std * a_std)
		- a_mean - 1;
    }

    /**
     * Computes scaled number of unsuccessful interactions.
     * 
     * @param a_mean
     * @param a_std
     * @return
     */
    public double scaleN(double a_mean, double a_std) {
	return ((1 - a_mean) * (1 - a_mean) - (1 - a_mean) * (1 - a_mean)
		* (1 - a_mean))
		/ (a_std * a_std) - (1 - a_mean) - 1;
    }

    /**
     * Expands the supporting array that holds opinions to appropriate lengths.
     * 
     * @param ops
     *            Set of opinion tuples
     */
    protected void expandOpinions(Set<Opinion> ops) {
	int max = opinions.length - 1;
	final int limit = max;

	for (Opinion o : ops) {
	    if (o.agent2 > max || o.agent1 > max) {
		max = Math.max(o.agent1, o.agent2);
	    }
	}

	if (max > limit) {
	    expandArrays(max);
	}
    }

    /**
     * Expands the supporting array that holds experiences to appropriate
     * lengths.
     * 
     * @param experience
     *            Set of experience tuples
     */
    protected void expandExperiences(Set<Experience> experience) {
	int max = opinions.length - 1;
	final int limit = max;

	for (Experience e : experience)
	    if (e.agent > max)
		max = e.agent;

	if (max > limit) {
	    expandArrays(max);
	}
    }

    /**
     * Expands the supporting data structures.
     * 
     * @param max
     *            The next limit of the data structures
     */
    protected void expandArrays(int max) {
	for (int agent = 0; agent <= max; agent++) {
	    if (!observations.containsKey(agent)) {
		observations.put(agent, new BRSPair[] { new BRSPair(),
			new BRSPair(), new BRSPair(), new BRSPair(),
			new BRSPair() });
	    }
	}

	// copy opinions
	Opinion[][] newOp = new Opinion[max + 1][max + 1];
	for (int i = 0; i < opinions.length; i++) {
	    System.arraycopy(opinions[i], 0, newOp[i], 0, opinions[i].length);
	}

	opinions = newOp;
    }

    @Override
    public ParametersPanel getParametersPanel() {
	return new TravosGUI();
    }

    @Override
    public void setCurrentTime(int time) {

    }
}
