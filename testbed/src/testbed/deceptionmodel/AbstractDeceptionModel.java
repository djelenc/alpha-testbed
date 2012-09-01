package testbed.deceptionmodel;

import testbed.interfaces.IDeceptionModel;

public abstract class AbstractDeceptionModel implements IDeceptionModel {

    @Override
    public void initialize(Object... params) {
    }

    @Override
    public String toString() {
	return getClass().getSimpleName();
    }
}
