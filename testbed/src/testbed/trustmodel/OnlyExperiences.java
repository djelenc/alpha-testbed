package testbed.trustmodel;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;

import testbed.interfaces.Experience;
import testbed.interfaces.Opinion;

/**
 * Trust model that only uses experiences and completely ignores opinions.
 * 
 * <p>
 * This model has bad coverage.
 * 
 * @author David
 * 
 */
public class OnlyExperiences extends AbstractTrustModel {
    // agent => cumulative interaction outcomes
    private Map<Integer, Double> exSum;

    // agent => interaction count
    private Map<Integer, Integer> exCnt;

    @Override
    public void initialize(Object... params) {
	exSum = new LinkedHashMap<Integer, Double>();
	exCnt = new LinkedHashMap<Integer, Integer>();
    }

    @Override
    public void processExperiences(Set<Experience> experiences) {
	for (Experience e : experiences) {
	    final int agent = e.agent;
	    final double outcome = e.outcome;

	    final Double soFar = exSum.get(agent);
	    final Integer soFarCount = exCnt.get(agent);

	    if (soFar == null) {
		exSum.put(agent, outcome);
		exCnt.put(agent, 1);
	    } else {
		exSum.put(agent, soFar + outcome);
		exCnt.put(agent, soFarCount + 1);
	    }
	}
    }

    @Override
    public void processOpinions(Set<Opinion> opinions) {
	// pass
    }

    @Override
    public void calculateTrust() {
	// pass
    }

    @Override
    public String getName() {
	return "Experiences only";
    }

    @Override
    public Map<Integer, Double> getRankings(int service) {
	final Map<Integer, Double> trust = new LinkedHashMap<Integer, Double>();

	for (int agent : exSum.keySet()) {
	    final double sum = exSum.get(agent);
	    final int count = exCnt.get(agent);

	    trust.put(agent, sum / count);
	}

	return trust;
    }

    @Override
    public void setCurrentTime(int time) {
    }
}
