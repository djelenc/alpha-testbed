package testbed.trustmodel;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;

import testbed.common.Utils;
import testbed.interfaces.Experience;
import testbed.interfaces.ICondition;
import testbed.interfaces.IParametersPanel;
import testbed.interfaces.Opinion;

/**
 * EigenTrust model
 * 
 * <p>
 * Implementation notes
 * <ul>
 * <li>I compute localized trust values from internal trust degrees. I multiply
 * the internalTrustDegree with the OPINION_FACTOR constant and then round the
 * result to the nearest integer. This represents the number of positive
 * interactions. The number of negative interactions is computed by subtracting
 * the number of positive interactions from OPINION_FACTOR constant.
 * <li>The right-most column of matrix C describes Alpha's local experiences.
 * They are (similar to opinions) obtained by multiplying experience outcomes
 * with EXPERIENCE_FACTOR constant. Future interactions are recorded by adding
 * the number of positive interactions to this number.
 * <li>Because agents do not provide opinions about Alpha, the bottom row of
 * matrix C always contains zeros.
 * <li>Alpha is the only pre-trusted peer -- in the vector of pre-trusted peers
 * all values are set to 0, expect the vale that corresponds to the Id of Alpha
 * agent (which is always the maximal ID).
 * <li>The default weight for pre-trusted peers is 0.5.
 * </ul>
 * 
 * <p>
 * <a href='http://doi.acm.org/10.1145/775152.775242'>Sepandar D. Kamvar, Mario
 * T. Schlosser, and Hector Garcia-Molina. 2003. The Eigentrust algorithm for
 * reputation management in P2P networks. In Proceedings of the 12th
 * international conference on World Wide Web (WWW '03).</a>
 * 
 * @author David
 * 
 */
public class EigenTrust extends AbstractTrustModel {

    public Map<Integer, Double> trust;
    public double[][] C;

    // count positive experiences
    public double[] cuPosExp;

    // Alpha normalized local experiences
    public double[] p;

    public static double WEIGHT = 0.5;
    public static double EXPERIENCE = 5;
    public static double OPINION = 10;

    private static final ICondition<Double> VAL_WEIGHT, MULT_VAL;

    static {
	VAL_WEIGHT = new ICondition<Double>() {
	    @Override
	    public void eval(Double var) {
		if (var < 0 || var > 1)
		    throw new IllegalArgumentException(
			    String.format(
				    "The weight must be a between 0 and 1 inclusively, but was %.2f",
				    var));
	    }
	};

	MULT_VAL = new ICondition<Double>() {
	    @Override
	    public void eval(Double var) {
		if (var < 1 || var > 50)
		    throw new IllegalArgumentException(
			    String.format(
				    "The multiplier must be a between 1 and 50 inclusively, but was %.2f",
				    var));
	    }
	};
    }

    @Override
    public void initialize(Object... params) {
	trust = new LinkedHashMap<Integer, Double>();

	// empty matrix
	C = new double[1][1];

	// cumulative number of positive interaction outcomes
	cuPosExp = new double[1];

	// pre-trusted peers vector
	p = new double[1];

	WEIGHT = Utils.extractParameter(VAL_WEIGHT, 0, params);
	EXPERIENCE = Utils.extractParameter(MULT_VAL, 1, params);
	OPINION = Utils.extractParameter(MULT_VAL, 2, params);
    }

    @Override
    public void processExperiences(Set<Experience> experiences) {
	expandExperiences(experiences);

	// process experiences
	for (Experience e : experiences) {
	    final double pos = Math.round(EXPERIENCE * e.outcome);
	    final double neg = EXPERIENCE - pos;

	    cuPosExp[e.agent] += Math.max(pos - neg, 0);
	}

	calculateTrust();
    }

    @Override
    public void processOpinions(Set<Opinion> opinions) {
	expandOpinions(opinions);

	// process opinions by creating matrix C
	for (Opinion o : opinions) {
	    final double pos = Math.round(OPINION * o.internalTrustDegree);
	    final double neg = OPINION - pos;

	    C[o.agent2][o.agent1] = Math.max(pos - neg, 0);
	}

	calculateTrust();
    }

    @Override
    public void calculateTrust() {
	// update normalized local trust values
	double totalPositive = 0;

	for (int i = 0; i < cuPosExp.length; i++)
	    totalPositive += cuPosExp[i];

	if (Math.abs(totalPositive) > 0.0001) {
	    // some local experiences
	    for (int i = 0; i < cuPosExp.length; i++)
		p[i] = cuPosExp[i] / totalPositive;
	} else {
	    // no local data -- uniform distribution
	    for (int i = 0; i < cuPosExp.length; i++)
		p[i] = 1d / cuPosExp.length;
	}

	// normalize matrix column-wise
	for (int col = 0; col < C.length; col++) {
	    double sum = 0;

	    for (int row = 0; row < C[col].length; row++)
		sum += C[row][col];

	    if (Math.abs(sum) > 0.0001)
		for (int row = 0; row < C[col].length; row++)
		    C[row][col] /= sum;
	    else
		// if a peer trusts/knows no-one
		for (int row = 0; row < C[col].length; row++)
		    C[row][col] = p[row];
	}

	// execute algorithm
	final double[] t_new = new double[p.length];
	final double[] t_old = new double[p.length];

	// t_new = p
	System.arraycopy(p, 0, t_new, 0, p.length);

	do {
	    // t_old = t_new
	    System.arraycopy(t_new, 0, t_old, 0, t_new.length);

	    // t_new = C * t_old
	    for (int row = 0; row < t_old.length; row++) {
		double sum = 0;

		for (int col = 0; col < t_old.length; col++)
		    sum += C[row][col] * t_old[col];

		t_new[row] = sum;
	    }

	    // t_new = (1 - weight) * t_new + weight * p
	    for (int i = 0; i < t_old.length; i++)
		t_new[i] = (1 - WEIGHT) * t_new[i] + WEIGHT * p[i];

	} while (!hasConverged(t_new, t_old));

	trust.clear();

	for (int i = 0; i < t_old.length; i++)
	    trust.put(i, t_new[i]);
    }

    public boolean hasConverged(double[] t_new, double[] t_old) {
	double sum = 0;

	for (int i = 0; i < t_old.length; i++)
	    sum += (t_new[i] - t_old[i]) * (t_new[i] - t_old[i]);

	return Math.sqrt(sum) < 0.01;
    }

    @Override
    public Map<Integer, Integer> getRankings(int service) {
	return constructRankingsFromEstimations(trust);
    }

    @Override
    public void setCurrentTime(int time) {

    }

    @Override
    public IParametersPanel getParametersPanel() {
	return new EigenTrustGUI();
    }

    /**
     * The method also expands the underlying (C, pCnt, pSum, p) arrays.
     * 
     * @param experience
     * @param opinions
     */
    protected void expandArrays(int maxAgent) {
	// resize C
	double[][] newC = new double[maxAgent + 1][maxAgent + 1];

	for (int i = 0; i < C.length; i++)
	    for (int j = 0; j < C.length; j++)
		newC[i][j] = C[i][j];

	for (int i = 0; i < C.length; i++)
	    newC[i][newC.length - 1] = C[i][C.length - 1];

	C = newC;

	// resize positive experiences
	double[] newPosExp = new double[maxAgent + 1];
	for (int i = 0; i < cuPosExp.length - 1; i++)
	    newPosExp[i] = cuPosExp[i];

	cuPosExp = newPosExp;

	// resize normalized local trust values
	double[] newR = new double[maxAgent + 1];
	for (int i = 0; i < p.length - 1; i++)
	    newR[i] = p[i];

	p = newR;

    }

    protected void expandOpinions(Set<Opinion> opinions) {
	final int limit = C.length - 1;
	int max = limit;

	for (Opinion o : opinions)
	    if (o.agent2 > max || o.agent1 > max)
		max = Math.max(o.agent1, o.agent2);

	if (max > limit) {
	    expandArrays(max);
	}
    }

    protected void expandExperiences(Set<Experience> experience) {
	final int limit = C.length - 1;
	int max = limit;

	for (Experience e : experience)
	    if (e.agent > max)
		max = e.agent;

	if (max > limit) {
	    expandArrays(max);
	}
    }
}
