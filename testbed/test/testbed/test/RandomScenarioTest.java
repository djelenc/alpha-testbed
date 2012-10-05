package testbed.test;

import java.util.HashMap;
import java.util.Map;
import junit.framework.Assert;

import org.junit.Test;

import testbed.common.DefaultRandomGenerator;
import testbed.deceptionmodel.Truthful;
import testbed.interfaces.DeceptionModel;
import testbed.interfaces.Scenario;
import testbed.scenario.Random;

public class RandomScenarioTest {

    @Test
    public void checkSizesOfCollections() {
	Scenario scenario = new Random();
	scenario.setRandomGenerator(new DefaultRandomGenerator(0));
	Map<DeceptionModel, Double> deceptionModels = new HashMap<DeceptionModel, Double>();
	deceptionModels.put(new Truthful(), 1d);

	scenario.initialize(1, 0.1, 0.05, deceptionModels, 0.25, 0.25, 1d);

	Assert.assertEquals(1, scenario.getAgents().size());
	Assert.assertEquals(1, scenario.getServices().size());

	Map<Integer, Double> capabilities = scenario.getCapabilities(0);

	Assert.assertEquals(1, capabilities.size());
	Assert.assertEquals(1, scenario.generateExperiences().size());
    }

    @Test(expected = IllegalArgumentException.class)
    public void failWhenAdditionalParametersMissing() {
	Scenario scenario = new Random();
	scenario.setRandomGenerator(new DefaultRandomGenerator(0));
	scenario.initialize(1, 1);
    }
}
