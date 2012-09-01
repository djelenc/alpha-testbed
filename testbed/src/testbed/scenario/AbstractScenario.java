package testbed.scenario;

import testbed.interfaces.IParametersPanel;
import testbed.interfaces.IRandomGenerator;
import testbed.interfaces.IScenario;

public abstract class AbstractScenario implements IScenario {

    protected IRandomGenerator generator;

    @Override
    public String toString() {
	return getClass().getSimpleName();
    }

    @Override
    public IParametersPanel getParametersPanel() {
	return null;
    }

    @Override
    public void setRandomGenerator(IRandomGenerator generator) {
	this.generator = generator;
    }
}
