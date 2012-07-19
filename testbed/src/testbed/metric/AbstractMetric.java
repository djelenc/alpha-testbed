package testbed.metric;

import testbed.interfaces.IMetric;
import testbed.interfaces.IParametersPanel;

public abstract class AbstractMetric implements IMetric {

    @Override
    public void initialize(Object... params) {

    }

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
