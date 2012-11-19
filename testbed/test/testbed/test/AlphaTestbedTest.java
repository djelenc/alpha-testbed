package testbed.test;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.junit.Before;
import org.junit.Test;

import testbed.AlphaTestbed;
import testbed.interfaces.Accuracy;
import testbed.interfaces.Experience;
import testbed.interfaces.InteractionPartnerSelection;
import testbed.interfaces.Opinion;
import testbed.interfaces.ParametersPanel;
import testbed.interfaces.RandomGenerator;
import testbed.interfaces.Scenario;
import testbed.interfaces.SelectingInteractionPartners;
import testbed.interfaces.TrustModel;
import testbed.interfaces.Utility;

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
	new AlphaTestbed(scn, tm, ranking, null, utility, null, null, null);
    }

    @Test
    public void testUtilityMode() {
	TrustModel<?> tm = new DecisionMakingTrustModel();
	Scenario scn = new PartnerSelectionScenario();
	new AlphaTestbed(scn, tm, ranking, null, utility, null, null, null);
    }

    @Test(expected = IllegalArgumentException.class)
    public void decisionMakingTrustModelOnRankingsScenario() {
	TrustModel<?> tm = new DecisionMakingTrustModel();
	Scenario scn = new RankingsScenario();
	new AlphaTestbed(scn, tm, ranking, null, utility, null, null, null);
    }

    @Test(expected = IllegalArgumentException.class)
    public void rankingsTrustModelOnPartnerSelectionScenario() {
	TrustModel<?> tm = new RankingsTrustModel();
	Scenario scn = new PartnerSelectionScenario();
	new AlphaTestbed(scn, tm, ranking, null, utility, null, null, null);
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
    public void processOpinions(List<Opinion> opinions) {
    }

    @Override
    public void processExperiences(List<Experience> experiences) {
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
    public List<Opinion> generateOpinions() {
	return null;
    }

    @Override
    public List<Experience> generateExperiences() {
	return null;
    }

    @Override
    public List<Integer> getAgents() {
	return null;
    }

    @Override
    public List<Integer> getServices() {
	return new ArrayList<Integer>();
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
	InteractionPartnerSelection {

    @Override
    public void setInteractionPartners(Map<Integer, Integer> partners) {
    }

}

class DecisionMakingTrustModel extends RankingsTrustModel implements
	SelectingInteractionPartners {
    @Override
    public Map<Integer, Integer> getInteractionPartners(List<Integer> services) {
	return null;
    }
}
