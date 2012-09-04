package testbed.trustmodel;

import testbed.interfaces.ParametersPanel;
import testbed.interfaces.RandomGenerator;
import testbed.interfaces.TrustModel;

public abstract class AbstractTrustModel<T extends Comparable<T>> implements
	TrustModel<T> {

    protected RandomGenerator generator;

    @Override
    public void setRandomGenerator(RandomGenerator generator) {
	this.generator = generator;
    }

    @Override
    public String toString() {
	return getClass().getSimpleName();
    }

    @Override
    public ParametersPanel getParametersPanel() {
	return null;
    }
}
