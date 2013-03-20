package testbed.trustmodel;

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
 * 2-dimensional vector of natural numbers. This is implemented by:
 * <ul>
 * <li>Thresholding interaction outcomes against a threshold given as a
 * parameter SATISFACTORY_THRESHOLD. If the outcome reaches this threshold the
 * interaction is considered to be positive, otherwise it is considered to be
 * negative.
 * <li>When TRAVOS obtains an opinion, it creates a (r, s) pair by sampling with
 * truncated normal distribution. The mean is set to the internalTrustDegree
 * from the obtained opinion, while the standard deviation is given as a
 * parameter: OPINION_SAMPLE_SD. The number of drawn samples is also given as a
 * parameter: OPINION_SAMPLE_NUM. Such sampling gives all opinions the same
 * weight (r + s = OPINION_SAMPLE_NUM). The parameter OPINION_SAMPLE_NUM also
 * needs to be large enough so that an opinion has a chance of falling into
 * every possible bin.
 * </ul>
 * 
 * @author David
 * 
 */
public class Travos extends AbstractTrustModel<Double> {
    protected static final ParameterCondition<Double> VAL_THRESHOLD;
    protected static final ParameterCondition<Integer> VAL_SAMPLE_NUM;
    static {
	VAL_SAMPLE_NUM = new ParameterCondition<Integer>() {
	    @Override
	    public void eval(Integer var) {
		if (var < 1)
		    throw new IllegalArgumentException(
			    String.format(
				    "The number of samples must non-negative, but was %d",
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
    public BRSPair[][] opinions = null;

    // observations about opinions
    public Map<Integer, BRSPair[]> observations = null;

    // parameters
    public static double SATISFACTORY_THRESHOLD = 0.5;
    public static double OPINION_SAMPLE_NUM = 10;
    public static double OPINION_SAMPLE_SD = 0.25;
    public static double CONFIDENCE_THRESHOLD = 0.95;
    public static double ERROR = 0.2;

    protected static final BetaDistribution BETA = new BetaDistributionImpl(1,
	    1);

    @Override
    public void initialize(Object... params) {
	experiences = new LinkedHashMap<Integer, BRSPair>();
	observations = new LinkedHashMap<Integer, BRSPair[]>();
	opinions = new BRSPair[0][0];

	SATISFACTORY_THRESHOLD = Utils.extractParameter(VAL_THRESHOLD, 0,
		params);
	OPINION_SAMPLE_NUM = Utils.extractParameter(VAL_SAMPLE_NUM, 1, params);
	OPINION_SAMPLE_SD = Utils.extractParameter(VAL_THRESHOLD, 2, params);
	CONFIDENCE_THRESHOLD = Utils.extractParameter(VAL_THRESHOLD, 3, params);
	ERROR = Utils.extractParameter(VAL_THRESHOLD, 4, params);
    }

    @Override
    public void processExperiences(List<Experience> exps) {
	// expand data structures
	expandExperiences(exps);

	// store experiences
	for (Experience e : exps) {
	    BRSPair p = experiences.get(e.agent);

	    final int r = (e.outcome >= SATISFACTORY_THRESHOLD ? 1 : 0);
	    final int s = 1 - r;

	    if (p == null) {
		p = new BRSPair(r, s);
		experiences.put(e.agent, p);
	    } else {
		p.R += r;
		p.S += s;
	    }

	    // Update deception detection mechanism
	    for (int reporter = 0; reporter < opinions.length; reporter++) {

		// if an opinion exists
		if (null != opinions[reporter][e.agent]) {
		    final double op_r = opinions[reporter][e.agent].R;
		    final double op_s = opinions[reporter][e.agent].S;

		    // determine the bin
		    final int bin = determineBin(op_r, op_s);

		    // store the actual value into bin
		    final BRSPair[] obs = observations.get(reporter);
		    obs[bin].R += r;
		    obs[bin].S += s;
		}
	    }
	}
    }

    @Override
    public void processOpinions(List<Opinion> ops) {
	// expand data structures
	expandOpinions(ops);

	// store opinions
	for (Opinion o : ops) {
	    // sample opinions to obtain (r, s) pair
	    int op_r = 0, op_s = 0;

	    for (int i = 0; i < OPINION_SAMPLE_NUM; i++) {
		if (generator.nextDoubleFromUnitTND(o.internalTrustDegree,
			OPINION_SAMPLE_SD) > SATISFACTORY_THRESHOLD) {
		    op_r += 1;
		} else {
		    op_s += 1;
		}
	    }

	    opinions[o.agent1][o.agent2] = new BRSPair(op_r, op_s);
	}
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
	    if (confidence > CONFIDENCE_THRESHOLD)
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
		    final BRSPair o = opinions[reporter][agent];

		    // if opinion exists
		    if (null != o) {
			agentExists = true;
			// compute (m, n) from the opinion
			final double m = o.R;
			final double n = o.S;

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
			final double adjusted_m = scaleM(a_mean, a_std);
			final double adjusted_s = scaleN(a_mean, a_std);

			// and add them to the reputation
			rep.R += adjusted_m;
			rep.S += adjusted_s;
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
    protected void expandOpinions(List<Opinion> ops) {
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
    protected void expandExperiences(List<Experience> experience) {
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
	BRSPair[][] newOp = new BRSPair[max + 1][max + 1];
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
