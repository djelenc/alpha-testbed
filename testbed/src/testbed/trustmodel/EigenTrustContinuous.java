package testbed.trustmodel;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;

import testbed.interfaces.Experience;
import testbed.interfaces.ITrustModel;
import testbed.interfaces.Opinion;

/**
 * EigenTrust model
 * 
 * <p>
 * Implementation notes
 * <ul>
 * <li>I compute localized trust values from internal trust degrees. I simply
 * put them in the matrix and normalize them column-wise.
 * <li>The right-most column of matrix C describes Alpha's local experiences.
 * They are (similar as opinions) simply experience outcomes which I normalize
 * column-wise. I track statistics to compute average interaction outcomes.
 * Because agents do not provide opinions about Alpha, the bottom row of matrix
 * C always contains zeros.
 * <li>Alpha is the only pre-trusted peer.
 * <li>The default weight for pre-trusted peers is 0.5.
 * </ul>
 * 
 * <font color='red'>Important distinctions.
 * <ul>
 * <li>I do not use the original formula (s_ij = sat(i, j) - unsat(i, j)) to
 * compute localized trust values. It would be interesting to see, if (and how)
 * data changes if I simulate satisfactory and unsatisfactory transactions by
 * using, for instance, a threshold function.
 * <li>When processing experiences outcomes (and also internalTrustDegrees from
 * opinions) I probably ought to use this formula (s = Math.max(e.outcome - 0.5,
 * 0))
 * </ul>
 * </font>
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
public class EigenTrustContinuous extends AbstractTrustModel implements ITrustModel {

    private Map<Integer, Double> trust;
    private double[][] C;
    private double[] p;

    private double[] pSum;
    private int[] pCnt;

    private double weight = 0.5;

    @Override
    public void initialize(Object... params) {
	trust = new LinkedHashMap<Integer, Double>();

	// empty matrix
	C = new double[1][1];

	// pre-trusted nodes
	p = new double[1];

	// sum interaction outcomes
	pSum = new double[1];

	// count interaction outcomes
	pCnt = new int[1];

	// Alpha is the only pre-trusted peer
	p[p.length - 1] = 1d;
    }

    /**
     * Checks if experiences and opinions contain values that require expansion
     * of arrays, and if they do, the method also expands the underlying (C,
     * pCnt, pSum, p) arrays.
     * 
     * @param experience
     * @param opinions
     */
    private void expandArray(Set<Experience> experience, Set<Opinion> opinions) {
	final int limit = C.length - 2;
	int maxAgent = limit;

	for (Experience e : experience)
	    if (e.agent > maxAgent)
		maxAgent = e.agent;

	for (Opinion o : opinions)
	    if (o.agent2 > maxAgent || o.agent1 > maxAgent)
		maxAgent = Math.max(o.agent1, o.agent2);

	if (maxAgent > limit) {
	    // resize C
	    double[][] newC = new double[maxAgent + 2][maxAgent + 2];

	    for (int i = 0; i < C.length - 1; i++)
		for (int j = 0; j < C.length - 1; j++)
		    newC[i][j] = C[i][j];

	    for (int i = 0; i < C.length - 1; i++)
		newC[i][newC.length - 1] = C[i][C.length - 1];

	    C = newC;

	    // resize p
	    p = new double[maxAgent + 2];
	    p[p.length - 1] = 1d;

	    // resize pSum
	    double[] newPSum = new double[maxAgent + 2];
	    for (int i = 0; i < pSum.length - 1; i++)
		newPSum[i] = pSum[i];

	    pSum = newPSum;

	    // resize pCnt
	    int[] newPCnt = new int[maxAgent + 2];
	    for (int i = 0; i < pCnt.length - 1; i++)
		newPCnt[i] = pCnt[i];

	    pCnt = newPCnt;
	}
    }

    @Override
    public void calculateTrust(Set<Experience> experience, Set<Opinion> opinions) {
	// expand arrays
	expandArray(experience, opinions);

	// process experiences
	for (Experience e : experience) {
	    pSum[e.agent] += e.outcome;
	    pCnt[e.agent] += 1;
	}

	// update Alpha's local experience vector with new data
	double sum = 0;
	for (int i = 0; i < p.length - 1; i++) {
	    if (pCnt[i] > 0)
		C[i][p.length - 1] = pSum[i] / pCnt[i];

	    sum += C[i][p.length - 1];
	}

	// process opinions by creating matrix C
	for (Opinion o : opinions)
	    C[o.agent2][o.agent1] = o.internalTrustDegree;

	// normalize matrix column wise
	sum = 0;
	for (int col = 0; col < C.length; col++) {
	    sum = 0;

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
	double[] t_new = new double[p.length];
	double[] t_old = new double[p.length];

	// t_new = p
	System.arraycopy(p, 0, t_new, 0, p.length);

	do {
	    // t_old = t_new
	    System.arraycopy(t_new, 0, t_old, 0, t_new.length);

	    // t_new = C * t_old
	    for (int row = 0; row < t_old.length; row++) {
		sum = 0;

		for (int col = 0; col < t_old.length; col++)
		    sum += C[row][col] * t_old[col];

		t_new[row] = sum;
	    }

	    // t_new = (1 - weight) * t_new + weight * p
	    for (int i = 0; i < t_old.length; i++)
		t_new[i] = (1 - weight) * t_new[i] + weight * p[i];

	} while (!hasConverged(t_new, t_old));

	trust.clear();

	for (int i = 0; i < t_old.length - 1; i++)
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
	return super.constructRankingsFromEstimations(trust);
    }

    @Override
    public void setCurrentTime(int time) {

    }
}
