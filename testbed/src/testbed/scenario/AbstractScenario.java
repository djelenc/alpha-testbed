package testbed.scenario;

import testbed.interfaces.IParametersPanel;
import testbed.interfaces.IScenario;

public abstract class AbstractScenario implements IScenario {

    @Override
    public String getName() {
	return getClass().getSimpleName();
    }

    @Override
    public String toString() {
	return getName();
    }

    @Override
    public IParametersPanel getParametersPanel() {
	return null;
    }
}
