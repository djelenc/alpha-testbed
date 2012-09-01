package testbed.trustmodel;

import testbed.interfaces.IParametersPanel;
import testbed.interfaces.IRandomGenerator;
import testbed.interfaces.ITrustModel;

public abstract class AbstractTrustModel<T extends Comparable<T>> implements
	ITrustModel<T> {

    protected IRandomGenerator generator;

    @Override
    public void setRandomGenerator(IRandomGenerator generator) {
	this.generator = generator;
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
