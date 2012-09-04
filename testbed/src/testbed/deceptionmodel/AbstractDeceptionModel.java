package testbed.deceptionmodel;

import testbed.interfaces.DeceptionModel;

public abstract class AbstractDeceptionModel implements DeceptionModel {

    @Override
    public void initialize(Object... params) {
    }

    @Override
    public String toString() {
	return getClass().getSimpleName();
    }
}
