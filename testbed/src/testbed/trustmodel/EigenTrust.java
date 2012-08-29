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
 * <li>The interactions outcomes are recorded in the <b>cntExp</b> array. Values
 * in this array represent the cumulative number of positive interactions of
 * agent Alpha with this agent -- s<sub>j</sub>. Such numbers are obtained in
 * the following procedure:
 * <ol>
 * <li>Obtain the number of positive interaction outcomes<br/>
 * <br/>
 * <center>pos<sub>i</sub> = Round(factor<sub>experience</sub> &times;
 * interaction_outcome<sub>i</sub>)</center><br/>
 * where factor<sub>experience</sub> is a user specified parameter.
 * <li>Obtain the number of negative interaction outcomes <br/>
 * <center>neg<sub>i</sub> = factor<sub>experience</sub> -
 * pos<sub>i</sub></center><br/>
 * <li>Compute the local trust value:<br/>
 * <center>s<sub>j</sub> = Max(pos<sub>i</sub> - neg<sub>i</sub>, 0)</center>
 * <br/>
 * <li>Add the computed local trust value to its corresponding position in the
 * <b>cntExp</b> array.
 * </ol>
 * <li>The pre-trust vector represents Alpha's normalized local experiences. The
 * pre-trust vector is computed by normalizing the values in the cntExp array.
 * <li>Obtained opinions are stored in the cntOp array. This array is used to
 * compute normalized local trust values that constitute the matrix C. The
 * computation is analogous to the computation of values in cntExp array and to
 * the computation of values in the pre-trusted peers vector.
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
    private static final ICondition<Double> VAL_WEIGHT, MULT_VAL;

    // holds positive opinions count
    public int[][] cntOp;

    // count positive experiences
    public int[] cntExp;

    public double weightFactor = 0.5;
    public double experienceFactor = 5;
    public double opinionFactor = 10;

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
	// cumulative number of positive interaction outcomes
	cntExp = new int[1];

	// cumulative number of positive opinions
	// this number gets rewritten each time a new opinion arrives
	cntOp = new int[1][1];

	weightFactor = Utils.extractParameter(VAL_WEIGHT, 0, params);
	experienceFactor = Utils.extractParameter(MULT_VAL, 1, params);
	opinionFactor = Utils.extractParameter(MULT_VAL, 2, params);
    }

    @Override
    public void processExperiences(Set<Experience> experiences) {
	expandArrays(experiences, null);

	// process experiences
	for (Experience e : experiences) {
	    final double pos = Math.round(experienceFactor * e.outcome);
	    final double neg = experienceFactor - pos;

	    cntExp[e.agent] += Math.max(pos - neg, 0);
	}
    }

    @Override
    public void processOpinions(Set<Opinion> opinions) {
	expandArrays(null, opinions);

	// process opinions by creating matrix C
	for (Opinion o : opinions) {
	    final double pos = Math
		    .round(opinionFactor * o.internalTrustDegree);
	    final double neg = opinionFactor - pos;

	    cntOp[o.agent2][o.agent1] = (int) Math.max(pos - neg, 0);
	}
    }

    @Override
    public void calculateTrust() {

    }

    @SuppressWarnings("unchecked")
    @Override
    public Map<Integer, Double> getRankings(int service) {
	// pre-trust vector
	double[] p = computePretrustVector(cntExp);

	// matrix C
	double[][] C = computeMatrix(cntOp, p);

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
		t_new[i] = (1 - weightFactor) * t_new[i] + weightFactor * p[i];

	} while (!hasConverged(t_new, t_old));

	final Map<Integer, Double> trust = new LinkedHashMap<Integer, Double>();

	for (int i = 0; i < t_old.length; i++)
	    trust.put(i, t_new[i]);

	return trust;
    }

    /**
     * Returns a pre-trust vector from an array of positive experiences counts.
     * 
     * @param experienceCount
     *            Array in which indexes represent agents and values represent
     *            the number of positive experiences with that agents.
     * @return Array of doubles representing the pre-trust vector
     */
    public double[] computePretrustVector(final int[] experienceCount) {
	final double[] p = new double[experienceCount.length];
	int sumPosExp = 0;

	for (int i = 0; i < experienceCount.length; i++)
	    sumPosExp += experienceCount[i];

	if (sumPosExp > 0) {
	    // at least some local experiences exist
	    final double normalization = sumPosExp;

	    for (int i = 0; i < experienceCount.length; i++)
		p[i] = experienceCount[i] / normalization;
	} else {
	    // no local data -- uniform distribution
	    for (int i = 0; i < experienceCount.length; i++)
		p[i] = 1d / experienceCount.length;
	}

	return p;
    }

    /**
     * Computes a matrix from an integer matrix of positive opinions
     * 
     * @param opinionCount
     *            Array of positive opinion counts
     * @param p
     *            Pre-trust vector (needed when no-one has an opinion about a
     *            particular agent)
     * @return A matrix normalized column-wise
     */
    public double[][] computeMatrix(final int[][] opinionCount, final double[] p) {
	final double[][] C = new double[opinionCount.length][opinionCount.length];

	// normalize matrix column-wise
	for (int col = 0; col < C.length; col++) {
	    int colSum = 0;

	    for (int row = 0; row < C[col].length; row++)
		colSum += opinionCount[row][col];

	    if (colSum > 0) {
		// at least someone has an opinion
		final double normalization = colSum;

		for (int row = 0; row < C[col].length; row++)
		    C[row][col] = opinionCount[row][col] / normalization;
	    } else {
		// if a peer trusts/knows no-one
		for (int row = 0; row < C[col].length; row++)
		    C[row][col] = p[row];
	    }
	}

	return C;
    }

    /**
     * Determines whether the Euclidean distance between two vectors is small
     * enough in order for the EigenTrust algorithm to stop.
     * 
     * @param t_new
     *            The first vector
     * @param t_old
     *            The second vector
     * @return True, when the difference is small enough, fase otherwise
     */
    public boolean hasConverged(double[] t_new, double[] t_old) {
	double sum = 0;

	for (int i = 0; i < t_old.length; i++)
	    sum += (t_new[i] - t_old[i]) * (t_new[i] - t_old[i]);

	return Math.sqrt(sum) < 0.01;
    }

    @Override
    public void setCurrentTime(int time) {

    }

    @Override
    public IParametersPanel getParametersPanel() {
	return new EigenTrustGUI();
    }

    /**
     * Expands the underlying array that hold experiences and opinions
     * 
     * @param experiences
     *            Set of experiences (can be null)
     * @param opinions
     *            Set of opinions (can be null)
     */
    protected void expandArrays(Set<Experience> experiences,
	    Set<Opinion> opinions) {
	final int limit = cntOp.length - 1;
	int max = limit;

	if (null != experiences)
	    for (Experience e : experiences)
		if (e.agent > max)
		    max = e.agent;

	if (null != opinions)
	    for (Opinion o : opinions)
		if (o.agent2 > max || o.agent1 > max)
		    max = Math.max(o.agent1, o.agent2);

	if (max > limit) {
	    // resize opinions' array
	    int[][] newCntOp = new int[max + 1][max + 1];

	    for (int i = 0; i < cntOp.length; i++)
		for (int j = 0; j < cntOp.length; j++)
		    newCntOp[i][j] = cntOp[i][j];

	    cntOp = newCntOp;

	    // resize positive experiences array
	    int[] newPosExp = new int[max + 1];
	    for (int i = 0; i < cntExp.length; i++)
		newPosExp[i] = cntExp[i];

	    cntExp = newPosExp;
	}
    }
}
