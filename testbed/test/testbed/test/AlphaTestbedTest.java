package testbed.test;

import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;

import org.junit.Before;
import org.junit.Test;

import testbed.AlphaTestbed;
import testbed.interfaces.Experience;
import testbed.interfaces.DecisionMaking;
import testbed.interfaces.ParametersPanel;
import testbed.interfaces.PartnerSelection;
import testbed.interfaces.RandomGenerator;
import testbed.interfaces.Accuracy;
import testbed.interfaces.Scenario;
import testbed.interfaces.TrustModel;
import testbed.interfaces.Utility;
import testbed.interfaces.Opinion;

public class AlphaTestbedTest {

    Accuracy ranking;
    Utility utility;

    @Before
    public void setUp() {
	ranking = new Accuracy() {

	    @Override
	    public void initialize(Object... params) {
	    }

	    @Override
	    public ParametersPanel getParametersPanel() {
		return null;
	    }

	    @Override
	    public <T extends Comparable<T>> double evaluate(
		    Map<Integer, T> rankings, Map<Integer, Double> capabilities) {
		return 0;
	    }
	};

	utility = new Utility() {

	    @Override
	    public void initialize(Object... params) {
	    }

	    @Override
	    public ParametersPanel getParametersPanel() {
		return null;
	    }

	    @Override
	    public double evaluate(Map<Integer, Double> capabilities, int agent) {
		return 0;
	    }
	};
    }

    @Test
    public void testRankingMode() {
	TrustModel<?> tm = new RankingsTrustModel();
	Scenario scn = new RankingsScenario();
	new AlphaTestbed(scn, tm, ranking, null, utility, null);
    }

    @Test
    public void testUtilityMode() {
	TrustModel<?> tm = new DecisionMakingTrustModel();
	Scenario scn = new PartnerSelectionScenario();
	new AlphaTestbed(scn, tm, ranking, null, utility, null);
    }

    @Test(expected = IllegalArgumentException.class)
    public void decisionMakingTrustModelOnRankingsScenario() {
	TrustModel<?> tm = new DecisionMakingTrustModel();
	Scenario scn = new RankingsScenario();
	new AlphaTestbed(scn, tm, ranking, null, utility, null);
    }

    @Test(expected = IllegalArgumentException.class)
    public void rankingsTrustModelOnPartnerSelectionScenario() {
	TrustModel<?> tm = new RankingsTrustModel();
	Scenario scn = new PartnerSelectionScenario();
	new AlphaTestbed(scn, tm, ranking, null, utility, null);
    }

}

class RankingsTrustModel implements TrustModel<Double> {

    @Override
    public void initialize(Object... params) {
    }

    @Override
    public void setCurrentTime(int time) {
    }

    @Override
    public void processOpinions(Set<Opinion> opinions) {
    }

    @Override
    public void processExperiences(Set<Experience> experiences) {
    }

    @Override
    public void calculateTrust() {
    }

    @Override
    public ParametersPanel getParametersPanel() {
	return null;
    }

    @Override
    public void setRandomGenerator(RandomGenerator generator) {

    }

    @Override
    public Map<Integer, Double> getTrust(int service) {
	return null;
    }

}

class RankingsScenario implements Scenario {

    @Override
    public void initialize(Object... parameters) {
    }

    @Override
    public void setCurrentTime(int time) {
    }

    @Override
    public Map<Integer, Double> getCapabilities(int service) {
	return null;
    }

    @Override
    public Set<Opinion> generateOpinions() {
	return null;
    }

    @Override
    public Set<Experience> generateExperiences() {
	return null;
    }

    @Override
    public Set<Integer> getAgents() {
	return null;
    }

    @Override
    public Set<Integer> getServices() {
	return new LinkedHashSet<Integer>();
    }

    @Override
    public ParametersPanel getParametersPanel() {
	return null;
    }

    @Override
    public void setRandomGenerator(RandomGenerator generator) {

    }

}

class PartnerSelectionScenario extends RankingsScenario implements
	PartnerSelection {

    @Override
    public void setNextInteractionPartners(Map<Integer, Integer> partners) {
    }

}

class DecisionMakingTrustModel extends RankingsTrustModel implements
	DecisionMaking {
    @Override
    public Map<Integer, Integer> getNextInteractionPartners(
	    Set<Integer> services) {
	return null;
    }
}
