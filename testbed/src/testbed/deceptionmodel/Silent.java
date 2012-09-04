package testbed.deceptionmodel;

import testbed.interfaces.DeceptionModel;

public class Silent extends AbstractDeceptionModel implements DeceptionModel {

    @Override
    public double calculate(double value) {
	return Double.NaN;
    }

}
