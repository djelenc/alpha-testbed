package testbed.metric;

import testbed.interfaces.IRankingMetric;
import testbed.interfaces.IParametersPanel;

public abstract class AbstractRankingMetric implements IRankingMetric {

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
