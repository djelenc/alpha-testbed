package testbed.trustmodel;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;

import testbed.interfaces.Experience;
import testbed.interfaces.ITrustModel;
import testbed.interfaces.Opinion;

import static testbed.trustmodel.TD.fromDouble;
import static testbed.trustmodel.TD.fromIndex;
import static testbed.trustmodel.TD.values;

/**
 * Trust model of Alfarez Abdul-Rahman and Stephen Hailes
 * 
 * <p>
 * <a href='http://dx.doi.org/10.1109/HICSS.2000.926814'>Alfarez Abdul-Rahman
 * and Stephen Hailes. Supporting trust in virtual communities. In Proceedings
 * of the 33rd Annual Hawaii International Conference on System Sciences,
 * 2000.</a>
 * 
 * @author David
 * 
 */
public class AbdulRahmanHailes extends AbstractTrustModel implements
	ITrustModel {

    /**
     * Direct trust
     * 
     * <p>
     * Q[Agent][Service][TrustDegree] => Frequency
     */
    private int[][][] Q;

    /**
     * Trusted recommender agents
     * 
     * <p>
     * R[Agent][Service][TrustDegree] => {Semantic differences}
     */
    private ArrayList<Integer>[][][] R;

    /**
     * Received recommendations
     * 
     * <p>
     * REC[Agent][Agent][Service] => TrustDegree
     */
    private TD[][][] REC;

    // temporary variables for efficiency
    private Map<Integer, Double> trust;
    private ArrayList<Integer> union;

    @SuppressWarnings("unchecked")
    @Override
    public void initialize(Object... params) {
	Q = new int[1][1][values().length];
	REC = new TD[1][1][1];

	R = (ArrayList<Integer>[][][]) new ArrayList[1][1][values().length];

	for (int i = 0; i < R.length; i++)
	    for (int j = 0; j < R[i].length; j++)
		for (int k = 0; k < R[i][j].length; k++)
		    R[i][j][k] = new ArrayList<Integer>();

	trust = new LinkedHashMap<Integer, Double>();
	union = new ArrayList<Integer>();
    }

    @Override
    public void calculateTrust(Set<Experience> experience, Set<Opinion> opinions) {
	// expand arrays if necessary
	expandArray(experience, opinions);

	// store opinions as recommendations
	for (Opinion o : opinions)
	    REC[o.agent1][o.agent2][o.service] = TD
		    .fromDouble(o.internalTrustDegree);

	// process experiences
	TD actual, recommended;
	int diff;

	for (Experience e : experience) {
	    // record experience as direct trust
	    actual = fromDouble(e.outcome);
	    Q[e.agent][e.service][actual.ordinal()] += 1;

	    // compare new experience with so far obtained opinions
	    for (int recommender = 0; recommender < REC.length; recommender++) {
		if (REC[recommender][e.agent][e.service] != null) {
		    // obtain the recommended value
		    recommended = REC[recommender][e.agent][e.service];

		    // compute the difference between recommended and actual
		    diff = recommended.ordinal() - actual.ordinal();

		    // record the difference
		    R[recommender][e.service][recommended.ordinal()].add(diff);
		}
	    }
	}
    }

    @Override
    public Map<Integer, Integer> getRankings(int service) {
	// clear temporary variables
	trust.clear();
	union.clear();

	int rtd, weight, sd;
	TD experience, recommended, corrected, aggregated;

	for (int agent = 0; agent < Q.length; agent++) {
	    // mode from experiences
	    experience = modeTD(Q[agent][service]);

	    if (experience != null) {
		// if experiences exist
		trust.put(agent, experience.numeric);
	    } else {
		// if no experience exist, compute from recommendations

		int[] opinions = new int[4];

		for (int recommender = 0; recommender < REC.length; recommender++) {
		    recommended = REC[recommender][agent][service];

		    if (recommended != null) {
			// compute RTD
			union.clear();
			for (int j = 0; j < R[recommender][service].length; j++)
			    union.addAll(R[recommender][service][j]);

			rtd = modeSDAbs(union);

			// compute weight from RTD
			weight = getWeight(rtd);

			// obtain semantic distance
			sd = modeSD(R[recommender][service][recommended
				.ordinal()]);

			// correct recommendation with semantic distance
			corrected = fromIndex(recommended.ordinal() - sd);

			// record correct recommendation and its weight
			opinions[corrected.ordinal()] += weight;
		    }
		}

		// aggregate opinions
		aggregated = modeTD(opinions);

		if (aggregated != null)
		    trust.put(agent, aggregated.numeric);
	    }
	}

	return super.constructRankingsFromEstimations(trust);
    }

    /**
     * Returns the weight for given RTD. (See Table 3 in the paper.)
     * 
     * @param rtd
     * @return
     */
    public static int getWeight(int rtd) {
	if (rtd == 0)
	    return 9;
	else if (rtd == 1)
	    return 5;
	else if (rtd == 2)
	    return 3;
	else if (rtd == 3)
	    return 1;
	else
	    return 0;
    }

    /**
     * Returns the mode from a given list of semantic distances. The values in
     * the list must be within [-3, 3] or an {@link IllegalArgumentException} is
     * thrown.
     * 
     * <ul>
     * <li>If the list multi-modal, the method returns 0. [<font color='blue'>I
     * would prefer the lowest mode in such case.</font>]
     * <li><font color='red'>The method also returns 0, if the list is empty.
     * This case is not covered in the paper.</font>
     * </ul>
     * 
     * @param list
     *            List of values
     * @return The mode of the list
     */
    public static int modeSD(ArrayList<Integer> list) {
	if (list.isEmpty())
	    return 0; // my add-on

	// construct vector of frequencies
	int[] vect = new int[7];
	int value;

	for (int i = 0; i < list.size(); i++) {
	    value = list.get(i);

	    if (value > 3 || value < -3)
		throw new IllegalArgumentException(String.format(
			"Invalid value %d. Should be within [-3, 3].",
			list.get(i)));

	    // the values are shifted to the right by 3
	    // (because indices must be positive)
	    vect[value + 3] += 1;
	}

	// find the mode in vector
	int index = -1, max = 0;
	boolean multiModal = false;

	for (int i = 0; i < vect.length; i++) {
	    if (vect[i] > max) {
		index = i;
		max = vect[i];
		multiModal = false;
	    } else if (vect[i] == max) {
		multiModal = true;
	    }
	}

	return (multiModal ? 0 : index - 3);
    }

    /**
     * Returns a value that corresponds to the mode of given list. The method
     * operates with the absolute values (for instance 2 and -2 are regarded as
     * the same value). The values in given list must be within [-3, 3] or an
     * {@link IllegalArgumentException} is thrown.
     * 
     * <ul>
     * <li>If the list is empty, the method returns -1.
     * <li><font color='red'>If the list is multi-modal, the method returns the
     * highest value. This is not covered in the paper.</font>
     * </ul>
     * 
     * @param list
     *            The list from which mode is computed.
     * @return The mode value of the list.
     */
    public static int modeSDAbs(ArrayList<Integer> list) {
	if (list.isEmpty())
	    return -1;

	// construct statistical vector of absolute values
	int[] absVector = new int[4];
	int value;

	for (int i = 0; i < list.size(); i++) {
	    value = Math.abs(list.get(i));

	    if (value > 3)
		throw new IllegalArgumentException(String.format(
			"Invalid value %d. Should be within [-3, 3].",
			list.get(i)));

	    absVector[value] += 1;
	}

	// find mode of the statistical vector
	int max = 0, iMax = -1;
	for (int i = 0; i < absVector.length; i++) {
	    if (absVector[i] >= max) {
		max = absVector[i];
		iMax = i;
	    }
	}

	return iMax;
    }

    /**
     * Returns a {@link TD Trust degree} that corresponds to the mode value of
     * the given statistical vector. The vector must be an array of integers,
     * where indices correspond to the trust degrees and the values to their
     * respective frequencies.
     * 
     * <ul>
     * <li>If vector is empty (all frequencies are 0), the method returns null.
     * <li><font color='red'>When the vector is multi-modal, the method should
     * return uncertainty values. However, the paper does not provide any
     * description on how to handle such values. Thus in case of multi-modality,
     * the method returns <b>the trust degree that corresponds to the lowest
     * mode.</b></font>
     * 
     * @param stats
     *            Statistical vector, in which keys represent the indices of
     *            trust degrees, and the values their respective frequencies.
     * @return
     */
    public static TD modeTD(int[] stats) {
	int index = -1, max = 0;

	for (int i = 0; i < stats.length; i++) {
	    if (stats[i] >= max) {
		index = i;
		max = stats[i];
	    }
	}

	return (max == 0 ? null : fromIndex(index));
    }

    @Override
    public void setCurrentTime(int time) {

    }

    /**
     * Checks if experiences and opinions contain values that require expansion
     * of arrays, and if they do, the method also expands the underlying (Q, R
     * and REC) arrays.
     * 
     * @param experience
     * @param opinions
     */
    private void expandArray(Set<Experience> experience, Set<Opinion> opinions) {
	// Q, R, REC -- same length
	int maxAgent = Q.length - 1;
	int maxService = Q[0].length - 1;

	for (Experience e : experience) {
	    if (e.agent > maxAgent)
		maxAgent = e.agent;

	    if (e.service > maxService)
		maxService = e.service;
	}

	for (Opinion o : opinions) {
	    if (o.agent2 > maxAgent || o.agent1 > maxAgent)
		maxAgent = Math.max(o.agent1, o.agent2);

	    if (o.service > maxService)
		maxService = o.service;
	}

	if (maxAgent > Q.length - 1 || maxService > Q[0].length - 1) {
	    // resize Q
	    int[][][] newQ = new int[maxAgent + 1][maxService + 1][values().length];

	    for (int a = 0; a < Q.length; a++)
		for (int s = 0; s < Q[a].length; s++)
		    System.arraycopy(Q[a][s], 0, newQ[a][s], 0, Q[a][s].length);

	    Q = newQ;

	    // resize REC
	    TD[][][] newREC = new TD[maxAgent + 1][maxAgent + 1][maxService + 1];

	    for (int a1 = 0; a1 < REC.length; a1++)
		for (int a2 = 0; a2 < REC[a1].length; a2++)
		    System.arraycopy(REC[a1][a2], 0, newREC[a1][a2], 0,
			    REC[a1][a2].length);

	    REC = newREC;

	    // resize R

	    @SuppressWarnings("unchecked")
	    ArrayList<Integer>[][][] newR = (ArrayList<Integer>[][][]) new ArrayList[maxAgent + 1][maxService + 1][TD
		    .values().length];

	    // copy existing
	    for (int i = 0; i < R.length; i++)
		for (int j = 0; j < R[i].length; j++)
		    System.arraycopy(R[i][j], 0, newR[i][j], 0, R[i][j].length);

	    // initialize for new values
	    for (int i = 0; i < newR.length; i++)
		for (int j = 0; j < newR[i].length; j++)
		    for (int k = 0; k < newR[i][j].length; k++)
			if (newR[i][j][k] == null)
			    newR[i][j][k] = new ArrayList<Integer>();

	    R = newR;
	}
    }
}