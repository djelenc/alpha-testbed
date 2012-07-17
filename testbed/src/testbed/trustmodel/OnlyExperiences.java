package testbed.trustmodel;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;

import testbed.interfaces.Experience;
import testbed.interfaces.ITrustModel;
import testbed.interfaces.Opinion;

public class OnlyExperiences extends AbstractTrustModel implements ITrustModel {
    private Map<Integer, Double> trust;

    // cumulative interaction outcomes
    private double[] exSum;

    // interaction count
    private int[] exCnt;

    @Override
    public void initialize(Object... params) {
	trust = new LinkedHashMap<Integer, Double>();
	exSum = new double[0];
	exCnt = new int[0];
    }

    private void expandArray(Set<Experience> experience) {
	int max = exSum.length - 1;

	for (Experience e : experience) {
	    if (e.agent > max) {
		max = e.agent;
	    }
	}

	if (max != exSum.length - 1) {
	    double[] newSum = new double[max + 1];
	    System.arraycopy(exSum, 0, newSum, 0, exSum.length);
	    exSum = newSum;

	    int[] newCnt = new int[max + 1];
	    System.arraycopy(exCnt, 0, newCnt, 0, exCnt.length);
	    exCnt = newCnt;
	}
    }

    @Override
    public void calculateTrust(Set<Experience> experience, Set<Opinion> opinions) {
	expandArray(experience);

	for (Experience e : experience) {
	    exSum[e.agent] += e.outcome;
	    exCnt[e.agent] += 1;
	}

	for (int i = 0; i < exCnt.length; i++) {
	    if (exCnt[i] > 0) {
		trust.put(i, exSum[i] / exCnt[i]);
	    }
	}
    }

    @Override
    public String getName() {
	return "Experiences only";
    }

    @Override
    public Map<Integer, Integer> getRankings(int service) {
	return super.constructRankingsFromEstimations(trust);
    }

    @Override
    public void setCurrentTime(int time) {

    }
}
