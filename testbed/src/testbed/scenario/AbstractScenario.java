package testbed.scenario;

import testbed.interfaces.ParametersPanel;
import testbed.interfaces.RandomGenerator;
import testbed.interfaces.Scenario;

public abstract class AbstractScenario implements Scenario {

    protected RandomGenerator generator;

    @Override
    public String toString() {
	return getClass().getSimpleName();
    }

    @Override
    public ParametersPanel getParametersPanel() {
	return null;
    }

    @Override
    public void setRandomGenerator(RandomGenerator generator) {
	this.generator = generator;
    }
}
