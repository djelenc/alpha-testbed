package testbed.deceptionmodel;

import testbed.interfaces.DeceptionModel;

public class Truthful extends AbstractDeceptionModel implements DeceptionModel {

    @Override
    public double calculate(double value) {
	return value;
    }

}
