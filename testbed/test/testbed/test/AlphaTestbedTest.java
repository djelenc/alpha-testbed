package testbed.test;

import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;

import org.junit.Before;
import org.junit.Test;

import testbed.AlphaTestbed;
import testbed.interfaces.Experience;
import testbed.interfaces.IDecisionMaking;
import testbed.interfaces.IParametersPanel;
import testbed.interfaces.IPartnerSelection;
import testbed.interfaces.IRankingMetric;
import testbed.interfaces.IScenario;
import testbed.interfaces.ITrustModel;
import testbed.interfaces.IUtilityMetric;
import testbed.interfaces.Opinion;

public class AlphaTestbedTest {

    IRankingMetric ranking;
    IUtilityMetric utility;

    @Before
    public void setUp() {
	ranking = new IRankingMetric() {

	    @Override
	    public void initialize(Object... params) {
	    }

	    @Override
	    public IParametersPanel getParametersPanel() {
		return null;
	    }

	    @Override
	    public String getName() {
		return null;
	    }

	    @Override
	    public double evaluate(Map<Integer, Integer> rankings,
		    Map<Integer, Double> capabilities) {
		return 0;
	    }
	};

	utility = new IUtilityMetric() {

	    @Override
	    public void initialize(Object... params) {
	    }

	    @Override
	    public IParametersPanel getParametersPanel() {
		return null;
	    }

	    @Override
	    public String getName() {
		return null;
	    }

	    @Override
	    public double evaluate(Map<Integer, Double> capabilities, int agent) {
		// TODO Auto-generated method stub
		return 0;
	    }
	};
    }

    @Test
    public void testRankingMode() {
	ITrustModel tm = new RankingsTrustModel();
	IScenario scn = new RankingsScenario();
	new AlphaTestbed(tm, scn, ranking, utility);
    }

    @Test
    public void testUtilityMode() {
	ITrustModel tm = new DecisionMakingTrustModel();
	IScenario scn = new PartnerSelectionScenario();
	new AlphaTestbed(tm, scn, ranking, utility);
    }

    @Test(expected = IllegalArgumentException.class)
    public void decisionMakingTrustModelOnRankingsScenario() {
	ITrustModel tm = new DecisionMakingTrustModel();
	IScenario scn = new RankingsScenario();
	new AlphaTestbed(tm, scn, ranking, utility);
    }

    @Test(expected = IllegalArgumentException.class)
    public void rankingsTrustModelOnPartnerSelectionScenario() {
	ITrustModel tm = new RankingsTrustModel();
	IScenario scn = new PartnerSelectionScenario();
	new AlphaTestbed(tm, scn, ranking, utility);
    }

}

class RankingsTrustModel implements ITrustModel {

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
    public Map<Integer, Integer> getRankings(int service) {
	return null;
    }

    @Override
    public IParametersPanel getParametersPanel() {
	return null;
    }

    @Override
    public String getName() {
	return null;
    }

}

class RankingsScenario implements IScenario {

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
    public String getName() {
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
    public IParametersPanel getParametersPanel() {
	return null;
    }

}

class PartnerSelectionScenario extends RankingsScenario implements
	IPartnerSelection {

    @Override
    public void setNextInteractionPartners(Map<Integer, Integer> partners) {
    }

}

class DecisionMakingTrustModel extends RankingsTrustModel implements
	IDecisionMaking {
    @Override
    public Map<Integer, Integer> getNextInteractionPartners(
	    Set<Integer> services) {
	return null;
    }
}
