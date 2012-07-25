package testbed.test;

import java.util.HashMap;
import java.util.Map;
import junit.framework.Assert;

import org.junit.Test;

import testbed.common.DefaultRandomGenerator;
import testbed.deceptionmodel.Truthful;
import testbed.interfaces.IDeceptionModel;
import testbed.interfaces.IScenario;
import testbed.scenario.Random;

public class RandomScenarioTest {

    @Test
    public void checkSizesOfCollections() {
	IScenario scenario = new Random();
	scenario.setRandomGenerator(new DefaultRandomGenerator(0));
	Map<IDeceptionModel, Double> deceptionModels = new HashMap<IDeceptionModel, Double>();
	deceptionModels.put(new Truthful(), 1d);

	scenario.initialize(1, 0.1, 0.05, deceptionModels, 0.25, 0.25);

	Assert.assertEquals(1, scenario.getAgents().size());
	Assert.assertEquals(1, scenario.getServices().size());

	Map<Integer, Double> capabilities = scenario.getCapabilities(0);

	Assert.assertEquals(1, capabilities.size());
	Assert.assertEquals(1, scenario.generateExperiences().size());
    }

    @Test(expected = IllegalArgumentException.class)
    public void failWhenAdditionalParametersMissing() {
	IScenario scenario = new Random();
	scenario.setRandomGenerator(new DefaultRandomGenerator(0));
	scenario.initialize(1, 1);
    }
}
