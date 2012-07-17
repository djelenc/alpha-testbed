package testbed.deceptionmodel;

import testbed.interfaces.IDeceptionModel;

public class Silent extends AbstractDeceptionModel implements IDeceptionModel {

    @Override
    public double calculate(double value) {
	return Double.NaN;
    }

}
